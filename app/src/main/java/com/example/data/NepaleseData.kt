package com.example.data

// --- Vegetable Market Model ---

data class VegetableItem(
    val id: Int,
    val nameEn: String,
    val nameNp: String,
    val category: String, // "Root", "Greens", "Fruits&Pods", "Spices", "Mushroom"
    val unit: String,
    val minPrice: Int,
    val maxPrice: Int,
    val averagePrice: Int = (minPrice + maxPrice) / 2
)

// --- Currency Model ---

data class CurrencyRate(
    val code: String,       // USD, INR, MYR, QAR, SAR, AED, EUR, GBP
    val nameNp: String,     // अमेरिकी डलर, भारतीय रुपैयाँ, मलेसियन रिंगिट, etc.
    val nameEn: String,
    val flag: String,       // Emoji flag
    val rateToNpr: Double   // 1 unit of this currency in NPR (for INR, it is 1.6)
)

// --- Gold & Silver Rates ---

data class CommodityItem(
    val nameNp: String,
    val nameEn: String,
    val pricePerTola: Int,   // Nepalese traditional weight (11.66g)
    val pricePer10g: Int,    // 10g standardized rate
    val changeAmount: Int,   // Difference from yesterday (+ or -)
    val unit: String = "Tola"
)

// --- Emergency Service ---

data class EmergencyContact(
    val nameNp: String,
    val nameEn: String,
    val number: String,
    val category: String, // "Security", "Medical", "Fire", "Disaster", "Social"
    val descriptionNp: String
)

// --- Static Predefined Datasets ---

object NepaleseData {
    val vegetables = listOf(
        VegetableItem(1, "Potato (Red)", "आलु (रातो)", "Root", "Kg", 48, 56),
        VegetableItem(2, "Potato (White)", "आलु (सेतो)", "Root", "Kg", 42, 48),
        VegetableItem(3, "Onion (Dry)", "प्याज (सुकेको)", "Root", "Kg", 75, 85),
        VegetableItem(4, "Tomato (Local)", "गोलभेडा (स्थानीय)", "Fruits&Pods", "Kg", 58, 68),
        VegetableItem(5, "Tomato (Big/Tunnel)", "गोलभेडा (ठूलो)", "Fruits&Pods", "Kg", 80, 95),
        VegetableItem(6, "Cabbage", "बन्दा गोभी", "Greens", "Kg", 30, 40),
        VegetableItem(7, "Cauliflower (Local)", "काउली (स्थानीय)", "Greens", "Kg", 65, 80),
        VegetableItem(8, "Spinach", "पालुङ्गो साग", "Greens", "Mutha", 45, 55),
        VegetableItem(9, "Mustard Greens", "रायोको साग", "Greens", "Mutha", 35, 45),
        VegetableItem(10, "Radish (White)", "मूला (सेतो)", "Root", "Kg", 25, 32),
        VegetableItem(11, "Carrot (Local)", "गाजर (स्थानीय)", "Root", "Kg", 55, 70),
        VegetableItem(12, "Okra (Bhindi)", "भिन्डी", "Fruits&Pods", "Kg", 60, 75),
        VegetableItem(13, "Eggplant (Long)", "भन्टा (लामो)", "Fruits&Pods", "Kg", 45, 55),
        VegetableItem(14, "Ginger", "अदुवा", "Spices", "Kg", 140, 160),
        VegetableItem(15, "Garlic (Dry)", "लसुन (सुकेको)", "Spices", "Kg", 220, 260),
        VegetableItem(16, "Sichuan Pepper", "टिमुर", "Spices", "Kg", 850, 1100),
        VegetableItem(17, "Green Chili", "अमिलो/हरियो खुर्सानी", "Spices", "Kg", 90, 110),
        VegetableItem(18, "Coriander Leaf", "धनियाँ (हरियो)", "Greens", "Kg", 120, 150),
        VegetableItem(19, "Button Mushroom", "गोब्रे च्याउ", "Mushroom", "Kg", 280, 340),
        VegetableItem(20, "Oyster Mushroom", "कन्या च्याउ", "Mushroom", "Kg", 180, 220),
        VegetableItem(21, "Lemon", "कागती", "Fruits&Pods", "Kg", 150, 180),
        VegetableItem(22, "Pumpkin", "फर्सी", "Fruits&Pods", "Kg", 35, 45)
    )

    val currencies = listOf(
        CurrencyRate("INR", "भारतीय रुपैयाँ", "Indian Rupee", "🇮🇳", 1.60),
        CurrencyRate("USD", "अमेरिकी डलर", "US Dollar", "🇺🇸", 133.45),
        CurrencyRate("QAR", "कतार रियाल", "Qatari Riyal", "🇶🇦", 36.62),
        CurrencyRate("AED", "संयुक्त अरब इमिरेट्स दिराम", "UAE Dirham", "🇦🇪", 36.33),
        CurrencyRate("SAR", "साउदी रियाल", "Saudi Riyal", "🇸🇦", 35.58),
        CurrencyRate("MYR", "मलेसियन रिंगिट", "Malaysian Ringgit", "🇲🇾", 28.25),
        CurrencyRate("EUR", "युरो", "Euro", "🇪🇺", 144.15),
        CurrencyRate("GBP", "ब्रिटिश पाउण्ड", "British Pound", "🇬🇧", 168.20),
        CurrencyRate("AUD", "अस्ट्रेलियन डलर", "Australian Dollar", "🇦🇺", 88.40)
    )

    val commodities = listOf(
        CommodityItem("सुन (छापावाल)", "Gold (Fine - 24K)", 136500, 117030, 800),
        CommodityItem("सुन (तेजावी)", "Gold (Tejabi - 22K)", 135850, 116470, 750),
        CommodityItem("चाँदी", "Silver", 1650, 1415, -15)
    )

    val emergencyContacts = listOf(
        EmergencyContact("प्रहरी नियन्त्रण कक्ष", "Police Control Room", "100", "Security", "कुनै पनि अपराध, झडप वा आपतकालिन सुरक्षा सहायताको लागि।"),
        EmergencyContact("एम्बुलेन्स सेवा", "Ambulance Routing System", "102", "Medical", "देशव्यापी स्वास्थ्य आपतकाल वा बिरामी ओसारपसार गर्न एम्बुलेन्सको लागि।"),
        EmergencyContact("दमकल (अग्निनियन्त्रक)", "Fire Brigade Hotline", "101", "Fire", "आगोलागी वा डढेलो नियन्त्रण र उद्दार कार्यको लागि।"),
        EmergencyContact("ट्राफिक प्रहरी", "Traffic Police Control", "103", "Security", "सडक दुर्घटना, जाम, वा ट्राफिक जरिवाना/नियम बुझ्न।"),
        EmergencyContact("राष्ट्रिय विपद् जोखिम प्रतिकार्य", "National Disaster Emergency", "1149", "Disaster", "बाढी, पहिरो, भूकम्प वा प्राकृतिक प्रकोप उद्दारको लागि।"),
        EmergencyContact("पर्यटक प्रहरी", "Tourist Police HQ", "1144", "Security", "नेपाल भ्रमणमा रहेका विदेशी वा स्थानीय पर्यटकको सहायता र सुरक्षा।"),
        EmergencyContact("बाल हेल्पलाइन", "Child Helpline Nepal", "1098", "Social", "हराएका, उत्पीडन वा श्रम शोषणमा परेका बालबालिकाको संरक्षणका लागि।"),
        EmergencyContact("महिला हिंसा विरुद्धको हेल्पलाइन", "Women Violence Helpline", "1145", "Social", "घरेलु वा सामाजिक हिंसामा परेका महिलाहरूको कानुनी र मानसिक सहायता।")
    )
}
