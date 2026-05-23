package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.GenerationConfig
import com.example.api.Part
import com.example.api.RetrofitClient
import com.example.data.EmergencyContact
import com.example.data.NepaleseData
import com.example.data.VegetableItem
import com.example.utils.NepaliCalendar
import com.example.utils.NepaliDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.UUID

enum class AppTab {
    Dashboard,
    Vegetables,
    Calendar,
    Remittance,
    Emergency,
    SabalAI
}

// --- Chat Models ---

enum class MessageSender {
    User,
    AI,
    System
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class SabalNepalViewModel : ViewModel() {

    // --- Tab Navigation ---
    private val _currentTab = MutableStateFlow(AppTab.Dashboard)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // --- Weather & General Live Note ---
    val todayAdDate: LocalDate = LocalDate.now()
    val todayBsDate: NepaliDate = NepaliCalendar.convertAdToBs(todayAdDate)

    // --- Vegetable Prices Search & Filter ---
    private val _vegSearchQuery = MutableStateFlow("")
    val vegSearchQuery: StateFlow<String> = _vegSearchQuery.asStateFlow()

    private val _selectedVegCategory = MutableStateFlow("All") // "All", "Root", "Greens", "Fruits&Pods", "Spices", "Mushroom"
    val selectedVegCategory: StateFlow<String> = _selectedVegCategory.asStateFlow()

    private val _vegetablesList = MutableStateFlow(NepaleseData.vegetables)
    val filteredVegetables: StateFlow<List<VegetableItem>> = combine(
        _vegetablesList,
        _vegSearchQuery,
        _selectedVegCategory
    ) { list, query, category ->
        list.filter { item ->
            val matchesQuery = item.nameEn.contains(query, ignoreCase = true) ||
                    item.nameNp.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || item.category == category
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NepaleseData.vegetables)

    fun onVegSearchChange(query: String) {
        _vegSearchQuery.value = query
    }

    fun selectVegCategory(category: String) {
        _selectedVegCategory.value = category
    }

    // --- Currency Conversion ---
    private val _currencyAmount = MutableStateFlow("100")
    val currencyAmount: StateFlow<String> = _currencyAmount.asStateFlow()

    private val _selectedCurrencyCode = MutableStateFlow("USD")
    val selectedCurrencyCode: StateFlow<String> = _selectedCurrencyCode.asStateFlow()

    fun onCurrencyAmountChange(amount: String) {
        _currencyAmount.value = amount
    }

    fun selectCurrency(code: String) {
        _selectedCurrencyCode.value = code
    }

    val currencyConversionRatesState = combine(
        _currencyAmount,
        _selectedCurrencyCode
    ) { amountText, code ->
        val amount = amountText.toDoubleOrNull() ?: 0.0
        val selectedRate = NepaleseData.currencies.find { it.code == code }?.rateToNpr ?: 1.0
        val finalNpr = amount * selectedRate

        val convertedList = NepaleseData.currencies.map { rate ->
            val otherAmount = if (rate.rateToNpr > 0.0) finalNpr / rate.rateToNpr else 0.0
            rate.code to otherAmount
        }.toMap()

        Triple(amount, finalNpr, convertedList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Triple(100.0, 13345.0, emptyMap()))


    // --- Custom Date Converter (AD to BS / BS to AD) ---
    private val _calculatorAdDate = MutableStateFlow(LocalDate.now())
    val calculatorAdDate: StateFlow<LocalDate> = _calculatorAdDate.asStateFlow()

    val calculatorBsDate: StateFlow<NepaliDate> = _calculatorAdDate.combine(MutableStateFlow(Unit)) { date, _ ->
        NepaliCalendar.convertAdToBs(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), todayBsDate)

    fun onAdDateSelected(date: LocalDate) {
        _calculatorAdDate.value = date
    }

    // BS to AD Calculator states
    private val _bsYearInput = MutableStateFlow("2083")
    val bsYearInput: StateFlow<String> = _bsYearInput.asStateFlow()

    private val _bsMonthInputIndex = MutableStateFlow(0) // 0 to 11
    val bsMonthInputIndex: StateFlow<Int> = _bsMonthInputIndex.asStateFlow()

    private val _bsDayInput = MutableStateFlow("15")
    val bsDayInput: StateFlow<String> = _bsDayInput.asStateFlow()

    private val _convertedAdResult = MutableStateFlow<String>("")
    val convertedAdResult: StateFlow<String> = _convertedAdResult.asStateFlow()

    fun onBsYearChange(year: String) {
        _bsYearInput.value = year
        triggerBsToAdConversion()
    }

    fun onBsMonthSelect(index: Int) {
        _bsMonthInputIndex.value = index
        triggerBsToAdConversion()
    }

    fun onBsDayChange(day: String) {
        _bsDayInput.value = day
        triggerBsToAdConversion()
    }

    init {
        triggerBsToAdConversion()
    }

    private fun triggerBsToAdConversion() {
        val y = _bsYearInput.value.toIntOrNull() ?: 2083
        val m = _bsMonthInputIndex.value + 1
        val d = _bsDayInput.value.toIntOrNull() ?: 15
        
        val adDate = NepaliCalendar.convertBsToAd(y, m, d)
        _convertedAdResult.value = "${adDate.year} ${adDate.month} ${adDate.dayOfMonth} (${adDate.dayOfWeek.name})"
    }


    // --- Emergency Hotline Search ---
    private val _emergencyCategory = MutableStateFlow("All") // "All", "Security", "Medical", "Fire", "Disaster", "Social"
    val emergencyCategory: StateFlow<String> = _emergencyCategory.asStateFlow()

    val activeEmergencyContactsState: StateFlow<List<EmergencyContact>> = _emergencyCategory.combine(MutableStateFlow(NepaleseData.emergencyContacts)) { category, list ->
        if (category == "All") list else list.filter { it.category == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NepaleseData.emergencyContacts)

    fun selectEmergencyCategory(category: String) {
        _emergencyCategory.value = category
    }


    // --- Sabal AI Chat State ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.AI,
                text = "नमस्ते हजुर! म सबल नेपाल एआई एन्ड्रोइड साथी (Sabal AI Guide) हुँ। म तपाईंलाई सरकारी सेवाहरू (नागरिकता, राहदानी, लाइसेन्स), दैनिक बजार मूल्य, कृषि सल्लाह वा कुनै पनि जानकारी नेपाली वा अंग्रेजी भाषामा बुझ्न मद्दत गर्न सक्छु। मलाई केहि सोध्नुहोस्!"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _aiInputText = MutableStateFlow("")
    val aiInputText: StateFlow<String> = _aiInputText.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    fun onAiInputChange(text: String) {
        _aiInputText.value = text
    }

    fun sendChatMessage(promptText: String = _aiInputText.value) {
        if (promptText.isBlank()) return

        val userMsg = ChatMessage(sender = MessageSender.User, text = promptText)
        _chatMessages.value = _chatMessages.value + userMsg
        _aiInputText.value = "" // clear input

        _isAiLoading.value = true

        viewModelScope.launch {
            val responseText = try {
                callGeminiApi(promptText)
            } catch (e: Exception) {
                "सुविधा उपलब्ध हुन सकेन। कृपया तपाईंको इन्टरनेट जडान सक्रिय छ र एआई स्टुडियो सेक्रेट्स (Secrets Panel) मा GEMINI_API_KEY सेट गरिएको छ भनेर निश्चित गर्नुहोस्। त्रुटि विवरण: ${e.localizedMessage}"
            }

            val aiMsg = ChatMessage(sender = MessageSender.AI, text = responseText)
            _chatMessages.value = _chatMessages.value + aiMsg
            _isAiLoading.value = false
        }
    }

    private suspend fun callGeminiApi(userPrompt: String): String = withContext(Dispatchers.IO) {
        // Read configuration key from secrets BuildConfig
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateOfflineResponse(userPrompt)
        }

        val systemInstructionText = """
            You are Sabal AI (सबल एआई), an exceptionally helpful, polite, and authoritative AI assistant for Nepalese citizens. 
            You are built into the 'Sabal Nepal' app to empower locals with critical local knowledge, government details, and day-to-day assistance.
            You speak fluent Nepali, Roman Nepali, and English. Keep your answers clear, very structured, practical, and highly accurate.
            If the user asks about official Nepalese regulations or civil requirements (Citizenship/Nagarikta, Passport/Rahadani, Driving License registration, PAN/VAT registration, birth/death/marriage certificate registering), explain the official step-by-step procedure in Nepal, listing the necessary documents, estimated fees, and correct government offices (like Wada Karyalaya/Ward Office, CDO Office/District Administration, Yatayat Karyalaya, etc.).
            If they ask about agriculture, vegetables, local markets, climate, or general emergency, share solid practical tips matching Nepal's context.
            Be extremely professional but deeply warm and friendly, greeting them with 'Namaste' (नमस्ते) or 'Namaskar' (नमस्कार) and using polite address tokens like 'Hajur', 'Tapai', 'Sathi', etc. Maintain clean line breaks in your responses so they render elegantly on Android.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = userPrompt)))
            ),
            generationConfig = GenerationConfig(temperature = 0.5f),
            systemInstruction = Content(parts = listOf(Part(text = systemInstructionText)))
        )

        try {
            // Attempt with the default gemini-3.5-flash model
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "माफ गर्नुहोस्, एआईले उत्तर सिर्जना गर्न असमर्थ भयो। कृपया पुन: प्रयास गर्नुहोस्।"
        } catch (e1: Exception) {
            try {
                // Failover attempt with the fallback gemini-2.5-flash model
                val fallbackResponse = RetrofitClient.service.generateContentWithPath("gemini-2.5-flash", apiKey, request)
                fallbackResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                    ?: "माफ गर्नुहोस्, एआईले उत्तर सिर्जना गर्न असमर्थ भयो। कृपया पुन: प्रयास गर्नुहोस्।"
            } catch (e2: Exception) {
                // Elegant local offline knowledge-base fallback when both fail
                "💡 [अफलाइन मोड • जडान हुन सकेन] एआई सर्भर व्यस्त छ वा कुनै इन्टरनेट छैन। स्थानीय सबल डाटाबेसबाट तत्काल प्राप्त जानकारी:\n\n" + generateOfflineResponse(userPrompt)
            }
        }
    }

    private fun generateOfflineResponse(userPrompt: String): String {
        val promptLower = userPrompt.lowercase()
        val vegetableSummary = NepaleseData.vegetables.take(5).joinToString("\n") { 
            "  • ${it.nameNp} (${it.nameEn}): रु. ${it.minPrice} - ${it.maxPrice} प्रति ${it.unit} (औसत रु. ${it.averagePrice})" 
        }
        val currencySummary = NepaleseData.currencies.take(5).joinToString("\n") { 
            "  • ${it.flag} ${it.nameNp} (${it.code}): १ = नेरु. ${it.rateToNpr}" 
        }

        return when {
            promptLower.contains("लाइसेन्स") || promptLower.contains("license") || promptLower.contains("सवारी") || promptLower.contains("ट्रायल") -> {
                """
                🚗 **नेपालमा सवारी चालक अनुमति पत्र (Driving License) लिने प्रक्रिया:**

                1. **अनलाइन आवेदन:** यातायात व्यवस्था विभागको आधिकारिक अनलाइन पोर्टल (dotm.gov.np) मा गइ 'Online Application' फारम भर्नुहोस्।
                2. **बायोमेट्रिक्स र राजस्व:** तोकिएको मितिमा यातायात व्यवस्था कार्यालय (Yatayat Office) मा गइ फोटो खिचाउने, बायोमेट्रिक्स दिने र राजस्व दस्तुर (रु. ५००) बुझाउनुहोस्।
                3. **लिखित परीक्षा (Written Test):** ट्राफिक संकेत, नियमहरू र प्रविधिक ज्ञान सम्बन्धी २० वटा बहुवैकल्पिक प्रश्नावली सोधिन्छ। उत्तीर्ण हुन कम्तीमा १० वटा अनिवार्य मिलाउनुपर्छ।
                4. **प्रयोगात्मक परीक्षा (Trial):** लिखित पास भएपछि 'ट्रायल' परीक्षा दिनुपर्नेछ। यसमा ८ आकार, उकालो, साँघुरो फलेक, ट्राफिक लाइट र यू-टर्न पार गर्नुपर्छ।
                5. **राजस्व र कार्ड वितरण:** ट्रायल पास भएपछि सवारी कार्यालयमा राजश्व बुझाएपछि १ वर्षको परीक्षणकालीन लाइसेन्स पाइन्छ र क्युआर कोडयुक्त स्मार्ट लाइसेन्स कार्ड उपलब्ध हुन्छ।
                """.trimIndent()
            }
            promptLower.contains("राहदानी") || promptLower.contains("passport") || promptLower.contains("पासपोर्ट") -> {
                """
                ✈️ **नेपाली विद्युतीय राहदानी (e-Passport) बनाउने प्रक्रिया र शुल्क:**

                1. **अनिवार्य आवश्यकता:** राष्ट्रिय परिचयपत्र (National ID Card) नम्बर हुनु अनिवार्य छ।
                2. **अनलाइन फारम:** राहदानी विभागको वेबसाइट (nepalpassport.gov.np) मा गइ मिति र कार्यालय रोजेर डिजिटल आवेदन फारम दर्ता गर्नुहोस्।
                3. **आवश्यक कागजातहरू:**
                   - सक्कल नेपाली नागरिकता प्रमाणपत्र (Nagarikta) र फोटोकपी
                   - राष्ट्रिय परिचयपत्र (NICS) को प्रतिलिपि
                   - नयाँ बनाउनको हकमा विवाह दर्ता वा सम्बन्धित वडाको सिफारिस (भौगोलिक ठेगाना फरक भएमा)
                   - म्याद सकिन लागेको वा पुरानो पासपोर्ट (नवीकरणको लागि)
                4. **लाग्ने शुल्क (Fees):**
                   - जिल्ला प्रशासन कार्यालय (CDO) बाट नियमित बनाउँदा (करिब १५-३० दिन): **रु. ५,०००**
                   - राहदानी विभाग, त्रिपुरेश्वरबाट द्रुत सेवा (२/३ कार्यदिन): **रु. १२,०००**
                """.trimIndent()
            }
            promptLower.contains("नागरिकता") || promptLower.contains("citizenship") -> {
                """
                🇳🇵 **नेपाली नागरिकता (Nagarikta Certificate) प्राप्त गर्ने आधिकारिक प्रक्रिया:**

                1. **योग्यता:** १६ वर्ष पुगेका नेपाली नागरिकले आफ्ना बुबा वा आमाको नेपाली नागरिकता प्रमाणपत्रको आधारमा नागरिकता पाउन सक्छन्।
                2. **वडाको सिफारिस:** सर्वप्रथम स्थायी ठेगाना भएको वडा कार्यालय (Wada Karyalaya) मा आवश्यक सिफारिस फारम भरी सर्जमिन (Local witness consensus) र 'नागरिकता प्रमाणपत्र सिफारिस' गराउनुहोस्।
                3. **आवश्यक कागजातहरू:**
                   - बुबा र आमाको सक्कल नेपाली नागरिकता र प्रतिलिपि
                   - जन्म दर्ता प्रमाणपत्र (Birth Certificate) को सक्कल कपी
                   - शैक्षिक योग्यताको प्रमाणपत्र (कक्षा ८ वा १० को मार्कसिट) वा उमेर खुल्ने कागजात
                   - विवाहित महिलाको हकमा विवाह दर्ता प्रमाणपत्र र माइतीको नागरिकता कपी
                4. **जिल्ला प्रशासन:** सिफारिस र सबै कागजात लिई जिल्ला प्रशासन कार्यालय वा ईलाका प्रशासन कार्यालयमा गइ सनाखत (Identity verification) गरेपछि सोही दिन नागरिकता प्राप्त हुन्छ।
                """.trimIndent()
            }
            promptLower.contains("प्यान") || promptLower.contains("pan") || promptLower.contains("कर") || promptLower.contains("vat") -> {
                """
                💼 **नेपालमा व्यक्तिगत प्यान कार्ड (Personal PAN Card) बनाउने पूर्ण तरिका:**

                1. **लाग्ने दस्तुर:** प्यान कार्ड पूर्ण रूपमा **निःशुल्क (Free of cost)** बन्दछ।
                2. **अनलाइन फारम भर्ने विधि:** आन्तरिक राजस्व विभाग (IRD) को करदाता पोर्टल (ird.gov.np) मा प्रवेश गरी 'Taxpayer Portal' मा जानुहोस्।
                3. **रजिष्ट्रेशन:** 'Registration (PAN/VAT)' मेनु अन्तर्गत 'Application for Personal PAN' रोजेर विवरण तथा आफ्नो पायक पर्ने आन्तरिक राजस्व कार्यालय (IRO/IRTSC) भर्नुहोस्।
                4. **संकलन:** फारम बुझाएर प्राप्त भएको सबमिसन नम्बर र फारम प्रिन्ट गरी नागरिकताको सक्कल कपी र एक प्रति फोटो सहित सम्बन्धित करदाता सेवा कार्यालयमा जानुहोस्। त्यहाँ तुरुन्तै प्यान कार्ड प्रिन्ट गरेर दिइन्छ।
                """.trimIndent()
            }
            promptLower.contains("कृषि") || promptLower.contains("बाली") || promptLower.contains("मल") || promptLower.contains("रोग") || promptLower.contains("कीरा") || promptLower.contains("compost") -> {
                """
                🌱 **कृषि सल्लाह, घरेलु प्राङ्गारिक मल बनाउने विधि र बाली संरक्षण:**

                1. **घरेलु कम्पोस्ट मल (Organic Fertilizer):** सुकेका पात, घाँस, भान्साको तरकारी तथा फलफूलको बाँकी अंश कम्पोस्ट बिनमा राख्नुहोस्। यसलाई हल्का ओसिलो बनाई जैविक हावा प्रवाह हुन दिनुहोस्। ३ देखि ४ महिनामा पोषकयुक्त प्राङ्गारिक कम्पोस्ट मल तयार हुन्छ।
                2. **निमको झोल (Bio-pesticide):** निमको पातलाई पानीमा राम्ररी उमालेर तयार पारेको झोलले बालीमा लाग्ने हानिकारक कीरा र ढुसी (fungus) नियन्त्रण गर्छ।
                3. **तीतेपाती र बकाइनोको जैविक विषादी:** गाउँघरमै पाइने तीतेपाती र बकाइनोलाई पिसेर जैविक विषादी बनाउन सकिन्छ, जसले रासायनिक विषादीको खर्च पूर्ण रूपमा बचाउँछ र स्वास्थ्यलाई फाइदा पुऱ्याउँछ।
                4. **बाली चक्र (Crop Rotation):** माटोको उर्वराशक्ति बचाउन एउटै बाली निरन्तर नलगाई गेडागुडी वा दलहन जातका बोटबिरुवा फेरेर लगाउनुहोस्।
                """.trimIndent()
            }
            promptLower.contains("तरकारी") || promptLower.contains("मूल्य") || promptLower.contains("रेट") || promptLower.contains("rate") || promptLower.contains("veg") -> {
                """
                🥦 **ताजा तरकारी बजार मूल्य विवरण (चितवन/काठमाडौं चल्तीको नेपाली थोक थोक दर):**

                $vegetableSummary

                *हाम्रो 'तरकारी बजार' ट्याबमा गएर सम्पूर्ण तरकारीको खोज तथा वर्गीकरण सजिलै हेर्न सक्नुहुन्छ।*
                """.trimIndent()
            }
            promptLower.contains("दाम") || promptLower.contains("डलर") || promptLower.contains("रेमिट्यान्स") || promptLower.contains("exchange") || promptLower.contains("पैसा") -> {
                """
                💵 **प्रमुख प्रतिनिधि अन्तर्राष्ट्रिय विनिमय दर (नेपाली रुपैयाँ NPR मा):**

                $currencySummary

                *हाम्रो 'रेमिट्यान्स' ट्याबमा गइ नेपाली रुपैयाँमा रूपान्तरण गर्ने क्याल्कुलेटर चलाउन सक्नुहुन्छ।*
                """.trimIndent()
            }
            else -> {
                """
                💡 **नमस्ते! म सबल नेपालको 'सबल एआई एन्ड्रोइड गाइड' हुँ।**

                मैले तपाईंको प्रश्न पाएँ। अहिले हाम्रो सुरक्षित स्थानीय अफलाइन डेटाबेस (Offline Database) प्रयोग गरि सेवा दिदै छु। 

                तपाईं मलाई निम्न मुख्य विषयहरू मध्ये कुनै पनि प्रश्न सोध्न सक्नुहुन्छ:
                ■ **नागरिकता** (प्रक्रिया र चाहिने कागजात)
                ■ **लाइसेन्स** (लाइसेन्स लिखित र ट्रायल प्रक्रिया)
                ■ **राहदानी** (e-Passport फारम र शुल्क)
                ■ **प्यान कार्ड** (Personal PAN Card तरिका)
                ■ **कृषि र मल** (जैविक मल तथा बाली जोगाउने उपाय)
                ■ **तरकारी दर** (बजार मूल्यहरू)
                ■ **विनिमय दर** (डलर र रियाल दरहरू)

                *यदि तपाईंको मोबाइलमा वाइफाइ/डाटा सक्रिय छ र एआई स्टुडिओमा 'GEMINI_API_KEY' सुरक्षित प्यानल थपिएको छ भने म प्रत्यक्ष गुगल जेमिनी अनलाइन क्षमता मार्फत अन्य लाखौँ प्रश्नको उत्तर तत्काल नेपाली वा अंग्रेजीमा दिन सक्छु!*
                """.trimIndent()
            }
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                sender = MessageSender.AI,
                text = "नमस्ते हजुर! कुराकानी पुन: सुरु भयो। तपाईंलाई के मद्दत चाहिन्छ?"
            )
        )
    }
}
