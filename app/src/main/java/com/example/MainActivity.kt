package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.utils.*
import com.example.viewmodel.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// --- Premium Palette ---
private val CrimsonRed = Color(0xFFD81F3E) // Nepalese Flag Crimson
private val NavyBlue = Color(0xFF1E365D)   // Nepalese Flag Border Blue
private val GoldAccent = Color(0xFFFFB300) // Beautiful Mustard Gold
private val SoftWhite = Color(0xFFFAFAFA)
private val LightSlate = Color(0xFFF1F5F9)
private val DarkSlate = Color(0xFF0F172A)
private val MutedText = Color(0xFF64748B)

class MainActivity : ComponentActivity() {
    private val viewModel: SabalNepalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SabalNepalTheme {
                MainAppScreen(viewModel)
            }
        }
    }
}

@Composable
fun SabalNepalTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary = CrimsonRed,
        onPrimary = Color.White,
        secondary = NavyBlue,
        onSecondary = Color.White,
        tertiary = GoldAccent,
        background = LightSlate,
        surface = Color.White,
        onBackground = DarkSlate,
        onSurface = DarkSlate
    )
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: SabalNepalViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("main_scaffold"),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tiny Nepal Flag Decorative Drawing
                        Canvas(modifier = Modifier.size(24.dp, 28.dp)) {
                            val w = size.width
                            val h = size.height

                            val blueStroke = 2.dp.toPx()

                            // Draw Double Pennon
                            val flagPath = Path().apply {
                                moveTo(0f, 0f)
                                lineTo(w, h * 0.45f)
                                lineTo(w * 0.3f, h * 0.55f)
                                lineTo(w, h * 0.95f)
                                lineTo(0f, h)
                                close()
                            }
                            // Base Flag Crimson Fill
                            drawPath(flagPath, CrimsonRed)
                            
                            // Blue border outline
                            drawPath(
                                path = flagPath,
                                color = NavyBlue,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = blueStroke,
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                            )

                            // Sun and Moon symbolic white dots
                            drawCircle(Color.White, radius = 3f, center = Offset(w * 0.3f, h * 0.28f))
                            drawCircle(Color.White, radius = 3f, center = Offset(w * 0.3f, h * 0.72f))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "सबल नेपाल",
                                style = LocalTextStyle.current.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Sabal Nepal Companion",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Light,
                                color = SoftWhite.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyBlue,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "विकासकर्ता: सबल नेपाल टिम २०८३", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "हाम्रो बारेमा",
                            tint = GoldAccent
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = NavyBlue,
                contentColor = SoftWhite,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars).testTag("bottom_nav_bar")
            ) {
                listOf(
                    Triple(AppTab.Dashboard, "गृह", Icons.Default.Home),
                    Triple(AppTab.Vegetables, "बजार", Icons.Default.ShoppingCart),
                    Triple(AppTab.Calendar, "पात्रो", Icons.Default.DateRange),
                    Triple(AppTab.Remittance, "रेमिट", Icons.Default.Refresh),
                    Triple(AppTab.Emergency, "आपतकालीन", Icons.Default.Phone),
                    Triple(AppTab.SabalAI, "AI साथी", Icons.Default.Star)
                ).forEach { (tab, label, icon) ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(tab) },
                        icon = {
                            Icon(
                                icon,
                                contentDescription = label,
                                tint = if (isSelected) NavyBlue else SoftWhite.copy(alpha = 0.7f)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) GoldAccent else SoftWhite.copy(alpha = 0.7f)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = GoldAccent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentTab,
            transitionSpec = {
                (slideInVertically { height -> height/10 } + fadeIn()).togetherWith(
                    slideOutVertically { height -> -height/10 } + fadeOut()
                )
            },
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            label = "tab_switch"
        ) { tab ->
            when (tab) {
                AppTab.Dashboard -> DashboardScreen(viewModel)
                AppTab.Vegetables -> VegetablesScreen(viewModel)
                AppTab.Calendar -> CalendarScreen(viewModel)
                AppTab.Remittance -> RemittanceScreen(viewModel)
                AppTab.Emergency -> EmergencyScreen(viewModel)
                AppTab.SabalAI -> SabalAIScreen(viewModel)
            }
        }
    }
}

// ==========================================
// 1. DASHBOARD SCREEN
// ==========================================
@Composable
fun DashboardScreen(viewModel: SabalNepalViewModel) {
    val scrollState = rememberScrollState()
    
    // Get real-time ticking clock
    var timeString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            val now = java.time.LocalTime.now()
            timeString = now.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm:ss a"))
            kotlinx.coroutines.delay(1000)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- Mount Everest Canvas with Date Banner ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            NavyBlue,
                            Color(0xFF101D38)
                        )
                    )
                )
                .border(2.dp, GoldAccent.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
        ) {
            // Distant starry sky & sunrise glowing radial brush
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw glowing Sun behind Mount Everest peaks
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(GoldAccent, Color.Transparent),
                        center = Offset(w * 0.65f, h * 0.45f),
                        radius = 160f
                    ),
                    radius = 160f,
                    center = Offset(w * 0.65f, h * 0.45f)
                )

                // Background stars/sun highlights
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = 3f,
                    center = Offset(w * 0.15f, h * 0.2f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.5f),
                    radius = 2f,
                    center = Offset(w * 0.45f, h * 0.15f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.6f),
                    radius = 3.5f,
                    center = Offset(w * 0.82f, h * 0.22f)
                )

                // Draw Snowy mountain range 1 (Distant Range with low opacity)
                val farPath = Path().apply {
                    moveTo(0f, h)
                    lineTo(w * 0.15f, h * 0.65f)
                    lineTo(w * 0.35f, h * 0.52f)
                    lineTo(w * 0.55f, h * 0.70f)
                    lineTo(w * 0.75f, h * 0.48f)
                    lineTo(w, h * 0.75f)
                    lineTo(w, h)
                    close()
                }
                drawPath(farPath, Color(0x3394A3B8))

                // Draw Snowy mountain range 2 (Middle Range - High Contrast Peaks)
                val midPath = Path().apply {
                    moveTo(0f, h)
                    lineTo(w * 0.25f, h * 0.50f)
                    lineTo(w * 0.4f, h * 0.68f)
                    lineTo(w * 0.62f, h * 0.38f) // Everest Peak
                    lineTo(w * 0.82f, h * 0.62f)
                    lineTo(w, h * 0.35f) // Machhapuchhre Peak
                    lineTo(w, h)
                    close()
                }
                drawPath(midPath, Color(0x44F1F5F9))

                // Snowy caps for Middle Range
                val cap1 = Path().apply {
                    moveTo(w * 0.25f, h * 0.50f)
                    lineTo(w * 0.20f, h * 0.58f)
                    lineTo(w * 0.30f, h * 0.58f)
                    close()
                }
                drawPath(cap1, Color.White)

                val cap2 = Path().apply {
                    moveTo(w * 0.62f, h * 0.38f) // Everest summit snow
                    lineTo(w * 0.55f, h * 0.48f)
                    lineTo(w * 0.69f, h * 0.48f)
                    close()
                }
                drawPath(cap2, Color.White)

                val cap3 = Path().apply {
                    moveTo(w, h * 0.35f)
                    lineTo(w * 0.92f, h * 0.45f)
                    lineTo(w, h * 0.52f)
                    close()
                }
                drawPath(cap3, Color.White)
            }

            // High-fidelity absolute overlay panels
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CrimsonRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "नेपाली डिजिटल पात्रो",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Live Ticking Clock (Nepal Time Zone Style)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.border(1.dp, Color.White.copy(0.2f), RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(GoldAccent)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = timeString.ifEmpty { "नेपाल समय" },
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Bottom Date display (beautiful typography hierarchy)
                Column {
                    Text(
                        text = viewModel.todayBsDate.formatNp(),
                        color = GoldAccent,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = viewModel.todayBsDate.dayOfWeekNp,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(modifier = Modifier.size(4.dp, 12.dp).background(CrimsonRed.copy(0.7f)))
                        Text(
                            text = "${viewModel.todayAdDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))} AD",
                            color = SoftWhite.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // --- Premium Quick AI Assist Card (Glassmorphic vibe) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("quick_ai_assist_card")
                .border(1.dp, CrimsonRed.copy(0.12f), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CrimsonRed.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = CrimsonRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "सबल एआई स्मार्ट साथी",
                                fontWeight = FontWeight.Bold,
                                color = DarkSlate,
                                fontSize = 15.sp,
                                letterSpacing = 0.3.sp
                            )
                            Text(
                                text = "Sabal AI Conversational Guide",
                                color = MutedText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Light
                            )
                        }
                    }
                    
                    // "Active" badge
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE2F0D9).copy(0.6f)),
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF388E3C)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("क्रियशील", color = Color(0xFF2E7D32), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                Text(
                    text = "हाम्रो सेक्रेट्स प्यानल (Secrets Panel) मा गइ 'GEMINI_API_KEY' सुरक्षित रुपमा थप्नुहोस् वा अफलाइन मोड प्रयोग गरि लाइसेन्स, राहदानी, प्यान, नागरिकता र कृषिका अनगिन्ती गाइडहरू तुरुन्तै नेपालीमा प्राप्त गर्नुहोस्।",
                    fontSize = 13.sp,
                    color = DarkSlate.copy(alpha = 0.85f),
                    lineHeight = 19.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { viewModel.selectTab(AppTab.SabalAI) },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = GoldAccent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "एआई च्याट कोठामा जानुहोस् (Chat with AI)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // --- Live Gold & Silver Commodities (सुन चाँदीको दर) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "सुन तथा चाँदी बजारको नवीनतम दर",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NavyBlue
                )
                Text(
                    text = "Gold & Silver Market Rates in Nepal",
                    fontSize = 11.sp,
                    color = MutedText
                )
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyBlue.copy(0.06f)),
                shape = RoundedCornerShape(100.dp)
            ) {
                Text(
                    text = "तोला दर",
                    color = NavyBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NepaleseData.commodities.take(2).forEach { item ->
                val isSilver = item.nameEn.contains("Silver", ignoreCase = true)
                val cardAccent = if (isSilver) Color(0xFF64748B) else GoldAccent
                val bgGradient = if (isSilver) {
                    listOf(Color.White, Color(0xFFF8FAFC))
                } else {
                    listOf(Color.White, Color(0xFFFFFDF5))
                }
                
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = 1.dp,
                            color = cardAccent.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.verticalGradient(bgGradient))
                            .padding(14.dp)
                    ) {
                        Column {
                            // Header segment
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.nameNp,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = DarkSlate
                                )
                                // Financial trend indicator
                                val isPositive = item.changeAmount >= 0
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = if (isPositive) "▲" else "▼",
                                        color = if (isPositive) Color(0xFF2E7D32) else CrimsonRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${if (isPositive) "+" else ""}${NepaliNumberConverter.toNepali(item.changeAmount)}",
                                        color = if (isPositive) Color(0xFF2E7D32) else CrimsonRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Text(
                                text = "रु ${NepaliNumberConverter.toNepali(item.pricePerTola)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSilver) NavyBlue else CrimsonRed,
                                letterSpacing = 0.2.sp
                            )
                            
                            Text(
                                text = "प्रति तोला (11.66 Grams)",
                                fontSize = 10.sp,
                                color = MutedText,
                                fontWeight = FontWeight.Medium
                            )
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = 1.dp,
                                color = LightSlate
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "१० ग्राम दर:",
                                    fontSize = 10.sp,
                                    color = MutedText
                                )
                                Text(
                                    text = "रु ${NepaliNumberConverter.toNepali(item.pricePer10g)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkSlate
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Quick Links Bento Row / Custom Access Board ---
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "द्रुत सुविधाहरू (Quick Access Board)",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NavyBlue
            )
            Text(
                text = "Fast and comfortable utility shortcuts",
                fontSize = 11.sp,
                color = MutedText
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf(
                Triple("तरकारी बजार", "कालीमाटी थोक बजार दर", AppTab.Vegetables),
                Triple("विनिमय दर", "प्रमुख मुद्रा र विनिमय", AppTab.Remittance),
                Triple("विपद् हटलाइन", "२४ घण्टा आपतकालीन", AppTab.Emergency)
            ).zip(
                listOf(
                    Pair(Icons.Default.ShoppingCart, Color(0xFF2E7D32)),
                    Pair(Icons.Default.Refresh, Color(0xFF1565C0)),
                    Pair(Icons.Default.Phone, CrimsonRed)
                )
            ).forEach { (shortcut, design) ->
                val (labelNp, labelEn, tab) = shortcut
                val (icon, color) = design
                
                Card(
                    onClick = { viewModel.selectTab(tab) },
                    modifier = Modifier
                        .weight(1f)
                        .height(104.dp)
                        .border(
                            width = 1.dp,
                            color = color.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        Column {
                            Text(
                                text = labelNp,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = NavyBlue,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = labelEn,
                                fontSize = 9.sp,
                                color = MutedText,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. VEGETABLE PRICES SCREEN (कालीमाटी बजार)
// ==========================================
@Composable
fun VegetablesScreen(viewModel: SabalNepalViewModel) {
    val vegetables by viewModel.filteredVegetables.collectAsState()
    val searchQuery by viewModel.vegSearchQuery.collectAsState()
    val selectedCategory by viewModel.selectedVegCategory.collectAsState()

    val categories = listOf("All" to "सबै", "Root" to "जरावाली", "Greens" to "सागपात", "Fruits&Pods" to "फलफूल", "Spices" to "मसला", "Mushroom" to "च्याउ")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search & Explanation Text
        Column {
            Text("कालीमाटी बजार दैनिक दररेट", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
            Text("Kalimati Vegetable Market Daily Rates (Kathmandu)", fontSize = 12.sp, color = MutedText)
        }

        // Search text field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onVegSearchChange(it) },
            placeholder = { Text("तरकारी खोज्नुहोस्... (Search vegetable...)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CrimsonRed) },
            modifier = Modifier.fillMaxWidth().testTag("veg_search_field"),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CrimsonRed,
                unfocusedBorderColor = MutedText.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Category filter chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { (code, name) ->
                val isSelected = selectedCategory == code
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectVegCategory(code) },
                    label = { Text("$name ($code)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CrimsonRed,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Vegetables grid list
        if (vegetables.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(48.dp), tint = MutedText)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("कुनै तरकारी फेला परेन।", fontWeight = FontWeight.Bold, color = MutedText)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(vegetables) { veggie ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(veggie.nameNp, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkSlate)
                                Text(veggie.nameEn, fontSize = 12.sp, color = MutedText)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MutedText.copy(alpha = 0.15f)),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = veggie.category,
                                            fontSize = 9.sp,
                                            color = NavyBlue,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("इकाई: प्रति ${veggie.unit}", fontSize = 10.sp, color = MutedText, modifier = Modifier.align(Alignment.CenterVertically))
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("औसत: रु ${NepaliNumberConverter.toNepali(veggie.averagePrice)}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CrimsonRed)
                                Text("रु ${NepaliNumberConverter.toNepali(veggie.minPrice)} - रु ${NepaliNumberConverter.toNepali(veggie.maxPrice)}", fontSize = 11.sp, color = MutedText)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. CALENDAR & DATE CONVERTER SCREEN
// ==========================================
@Composable
fun CalendarScreen(viewModel: SabalNepalViewModel) {
    val calcBsDate by viewModel.calculatorBsDate.collectAsState()
    val calendarAdDate by viewModel.calculatorAdDate.collectAsState()
    
    // Reverse BS to AD states
    val bsYearInput by viewModel.bsYearInput.collectAsState()
    val bsMonthInputIndex by viewModel.bsMonthInputIndex.collectAsState()
    val bsDayInput by viewModel.bsDayInput.collectAsState()
    val convertedAdResult by viewModel.convertedAdResult.collectAsState()

    val context = LocalContext.current
    var isReverseCalcOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("मिति रूपान्तरण र पर्व (Date Converter & Festivals)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
            Text("Convert Gregorian (AD) to Bikram Sambat (BS) or view major festivals.", fontSize = 12.sp, color = MutedText)
        }

        // --- Date Converter Card ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isReverseCalcOpen) "विक्रम संवत् (BS) ➔ इस्वी संवत् (AD)" else "इस्वी संवत् (AD) ➔ विक्रम संवत् (BS)",
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue
                    )
                    TextButton(onClick = { isReverseCalcOpen = !isReverseCalcOpen }) {
                        Text(if (isReverseCalcOpen) "AD - BS" else "BS - AD", color = CrimsonRed, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (!isReverseCalcOpen) {
                    // AD to BS Converter
                    Text("Gregorian AD मिति छान्नुहोस् (Select AD Date)", fontSize = 12.sp, color = MutedText)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val dp = android.app.DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        viewModel.onAdDateSelected(LocalDate.of(year, month + 1, day))
                                    },
                                    calendarAdDate.year,
                                    calendarAdDate.monthValue - 1,
                                    calendarAdDate.dayOfMonth
                                )
                                dp.show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("मिति परिवर्तन गर्नुहोस्", fontSize = 12.sp)
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = LightSlate),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Text(
                                text = calendarAdDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                                fontWeight = FontWeight.Bold,
                                color = DarkSlate,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NavyBlue.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                            Text("रूपान्तरित विक्रम संवत् (BS Date):", fontSize = 12.sp, color = NavyBlue, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(calcBsDate.formatNp(), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = CrimsonRed)
                            Text("साल ${calcBsDate.year}, ${NepaliCalendar.MONTH_NAMES_EN[calcBsDate.month - 1]} ${calcBsDate.day} (${calcBsDate.dayOfWeekEn})", fontSize = 13.sp, color = DarkSlate)
                        }
                    }
                } else {
                    // BS to AD Converter
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Year text
                            OutlinedTextField(
                                value = bsYearInput,
                                onValueChange = { viewModel.onBsYearChange(it) },
                                label = { Text("वर्ष (Year)", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )

                            // Day text
                            OutlinedTextField(
                                value = bsDayInput,
                                onValueChange = { viewModel.onBsDayChange(it) },
                                label = { Text("गते (Day)", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }

                        // Month chooser dropdown styled in row
                        Text("महिना चयन गर्नुहोस् (Select Month)", fontSize = 11.sp, color = MutedText)
                        var isDropOpen by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { isDropOpen = true },
                                colors = ButtonDefaults.buttonColors(containerColor = LightSlate),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(NepaliCalendar.MONTH_NAMES_NP[bsMonthInputIndex] + " (${NepaliCalendar.MONTH_NAMES_EN[bsMonthInputIndex]})", color = DarkSlate, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = DarkSlate)
                            }
                            DropdownMenu(expanded = isDropOpen, onDismissRequest = { isDropOpen = false }) {
                                NepaliCalendar.MONTH_NAMES_NP.forEachIndexed { idx, name ->
                                    DropdownMenuItem(
                                        text = { Text("$name (${NepaliCalendar.MONTH_NAMES_EN[idx]})") },
                                        onClick = {
                                            viewModel.onBsMonthSelect(idx)
                                            isDropOpen = false
                                        }
                                    )
                                }
                            }
                        }

                        // Result
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NavyBlue.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                                Text("रूपान्तरित इस्वी संवत् (AD Result):", fontSize = 12.sp, color = NavyBlue, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = convertedAdResult.ifBlank { "अमान्य मिति विवरण" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = CrimsonRed
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Major Festivals List ---
        Text("आसन्न पर्व तथा उत्सवहरू (Upcoming Nepal Festivals)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
        NepaliCalendar.festivals2083.forEach { festival ->
            val daysDiff = ChronoUnit.DAYS.between(viewModel.todayAdDate, festival.dateAd)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(festival.nameNp, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkSlate)
                        
                        // Countdown banner
                        val countdownColor = if (daysDiff < 0) MutedText else if (daysDiff < 30) CrimsonRed else NavyBlue
                        Card(
                            colors = CardDefaults.cardColors(containerColor = countdownColor.copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (daysDiff < 0) "सकियो" else if (daysDiff == 0L) "आज हो!" else "${daysDiff} दिन बाँकी",
                                fontSize = 10.sp,
                                color = countdownColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(festival.nameEn, fontSize = 11.sp, color = MutedText)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(festival.descriptionNp, fontSize = 12.sp, color = DarkSlate)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "मिति: ${festival.dateAd.format(DateTimeFormatter.ofPattern("yyyy MMMM dd"))} (BS ${NepaliNumberConverter.toNepali(festival.monthBs)}/${NepaliNumberConverter.toNepali(festival.dayBs)})",
                        fontSize = 10.sp,
                        color = MutedText
                    )
                }
            }
        }
    }
}

// ==========================================
// 4. REMITTANCE & CURRENCY CONVERTER SCREEN
// ==========================================
@Composable
fun RemittanceScreen(viewModel: SabalNepalViewModel) {
    val currencyAmountText by viewModel.currencyAmount.collectAsState()
    val selectedCode by viewModel.selectedCurrencyCode.collectAsState()
    val convertState by viewModel.currencyConversionRatesState.collectAsState()

    val (srcAmount, finalNpr, otherConversions) = convertState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("रेमिट्यान्स र विदेशी विनिमय दर (Forex & Remit)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
            Text("Check forex currency rate in Nepal or calculate the remittance value.", fontSize = 12.sp, color = MutedText)
        }

        // --- Peg reminder banner (INR is fixed pegged at 1.6!) ---
        Card(
            colors = CardDefaults.cardColors(containerColor = CrimsonRed.copy(0.1f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = CrimsonRed)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "भारतीय रुपैयाँ (INR) सँग नेपाली रुपैयाँ (NPR) को विनिमय दर 'स्थिर' (Fixed Pegged - 1.60) राखी गणना गरिएको छ।",
                    fontSize = 11.sp,
                    color = CrimsonRed,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // --- Calculator panel ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("रेमिट्यान्स गणना (Remittance Calculator)", fontWeight = FontWeight.Bold, color = NavyBlue)

                // Input currency value
                OutlinedTextField(
                    value = currencyAmountText,
                    onValueChange = { viewModel.onCurrencyAmountChange(it) },
                    label = { Text("रकम प्रविष्ट गर्नुहोस् (Amount)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("money_input_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrimsonRed,
                        unfocusedBorderColor = MutedText.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Select sender local currency using visual cards of flags
                Text("विदेशी मुद्रा चयन गर्नुहोस् (Select Foreign Currency)", fontSize = 11.sp, color = MutedText)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(NepaleseData.currencies) { cur ->
                        val isSelected = selectedCode == cur.code
                        val bg = if (isSelected) NavyBlue else LightSlate
                        val tc = if (isSelected) Color.White else DarkSlate
                        
                        Card(
                            onClick = { viewModel.selectCurrency(cur.code) },
                            colors = CardDefaults.cardColors(containerColor = bg),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isSelected) CrimsonRed else Color.Transparent)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(cur.flag, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(cur.code, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tc)
                            }
                        }
                    }
                }

                // Calculation Result
                Card(
                    colors = CardDefaults.cardColors(containerColor = CrimsonRed.copy(0.05f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                        val activeCurrency = NepaleseData.currencies.find { it.code == selectedCode }
                        Text(
                            text = "${activeCurrency?.flag} ${activeCurrency?.code} ($srcAmount) = ",
                            fontSize = 12.sp,
                            color = MutedText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "नेपाली रु ${String.format("%.2f", finalNpr)} (NPR)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = CrimsonRed
                        )
                        Text(
                            text = "@ प्रति इकाई दर: रु ${activeCurrency?.rateToNpr} NPR",
                            fontSize = 11.sp,
                            color = MutedText
                        )
                    }
                }
            }
        }

        // --- Standard reference table ---
        Text("विदेशी विनिमय बजार दर (Reference Exchange Rates)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                NepaleseData.currencies.forEach { cur ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(cur.flag, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(cur.code, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkSlate)
                                Text(cur.nameNp, fontSize = 10.sp, color = MutedText)
                            }
                        }
                        Text("रु ${cur.rateToNpr}", fontWeight = FontWeight.Bold, color = CrimsonRed, fontSize = 14.sp)
                    }
                    Divider(color = LightSlate)
                }
            }
        }
    }
}

// ==========================================
// 5. EMERGENCY CONTACTS SCREEN
// ==========================================
@Composable
fun EmergencyScreen(viewModel: SabalNepalViewModel) {
    val contacts by viewModel.activeEmergencyContactsState.collectAsState()
    val activeCategory by viewModel.emergencyCategory.collectAsState()
    val context = LocalContext.current

    val categories = listOf("All" to "सबै", "Security" to "सुरक्षा", "Medical" to "स्वास्थ्य", "Fire" to "दमकल", "Disaster" to "विपद्", "Social" to "सामाजिक")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text("आपतकालीन सेवा हटलाइन (Emergency Hotlines)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
            Text("One-tap direct dialing support for public emergency bodies.", fontSize = 12.sp, color = MutedText)
        }

        // M3 category chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { (code, name) ->
                val isSelected = activeCategory == code
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectEmergencyCategory(code) },
                    label = { Text(name, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CrimsonRed,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(contacts) { contact ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (contact.number == "100" || contact.number == "102") CrimsonRed.copy(0.3f) else Color.Transparent)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val icon = when (contact.category) {
                                        "Security" -> Icons.Default.Lock
                                        "Medical" -> Icons.Default.Home
                                        "Fire" -> Icons.Default.Warning
                                        "Disaster" -> Icons.Default.Warning
                                        else -> Icons.Default.Notifications
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = CrimsonRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(contact.nameNp, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkSlate)
                                }
                                Text(contact.nameEn, fontSize = 11.sp, color = MutedText)
                            }

                            // Dynamic Call Action Button
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.number}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "डायलर खोल्न सकिएन।", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                modifier = Modifier.testTag("call_button_${contact.number}"),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(contact.number, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(contact.descriptionNp, fontSize = 12.sp, color = DarkSlate)
                    }
                }
            }
        }
    }
}

// --- Markdown Text Renderer for AI Responses ---
@Composable
fun ParsedMarkdownText(text: String, color: Color, fontSize: androidx.compose.ui.unit.TextUnit = 13.sp) {
    val annotatedString = remember(text) {
        buildAnnotatedString {
            var currentIndex = 0
            val pattern = Regex("\\*\\*(.*?)\\*\\*")
            val matches = pattern.findAll(text)
            
            for (match in matches) {
                if (match.range.first > currentIndex) {
                    append(text.substring(currentIndex, match.range.first))
                }
                val boldText = match.groupValues[1]
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(boldText)
                }
                currentIndex = match.range.last + 1
            }
            
            if (currentIndex < text.length) {
                append(text.substring(currentIndex))
            }
        }
    }
    Text(
        text = annotatedString,
        color = color,
        fontSize = fontSize,
        lineHeight = 18.sp
    )
}

// ==========================================
// 6. GEMINI POWERED SABAL AI CHAT SCREEN
// ==========================================
@Composable
fun SabalAIScreen(viewModel: SabalNepalViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val inputText by viewModel.aiInputText.collectAsState()
    val isLoading by viewModel.isAiLoading.collectAsState()
    val kb = LocalSoftwareKeyboardController.current

    val lazyListState = rememberLazyListState()

    // Auto scroll bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // AI Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("सबल एआई सरकारी र बजार सहायक", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                Text("Smart citizen guide for license, passport, tax & rules.", fontSize = 11.sp, color = MutedText)
            }
            IconButton(
                onClick = { viewModel.clearChat() },
                colors = IconButtonDefaults.iconButtonColors(containerColor = CrimsonRed.copy(0.1f))
            ) {
                Icon(Icons.Default.Delete, contentDescription = "च्याट मेट्नुहोस्", tint = CrimsonRed)
            }
        }

        // Messages list box with scrolling
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(LightSlate, RoundedCornerShape(12.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                val isAi = message.sender == MessageSender.AI
                val bubbleColor = if (isAi) Color.White else CrimsonRed
                val textStyleColor = if (isAi) DarkSlate else Color.White
                val alignment = if (isAi) Alignment.Start else Alignment.End

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = alignment
                ) {
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isAi) 4.dp else 16.dp,
                                    bottomEnd = if (isAi) 16.dp else 4.dp
                                )
                            )
                            .background(bubbleColor)
                            .padding(12.dp)
                            .widthIn(max = 280.dp)
                    ) {
                        ParsedMarkdownText(
                            text = message.text,
                            color = textStyleColor,
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        text = if (isAi) "सबल एआई" else "तपाईं",
                        fontSize = 9.sp,
                        color = MutedText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = CrimsonRed,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("सबल एआई टाइप गर्दैछ... (Thinking...)", fontSize = 11.sp, color = MutedText)
                    }
                }
            }
        }

        // Suggestions chips row
        Text("सुझाव प्रश्नहरू (Suggestions):", fontSize = 10.sp, color = MutedText, fontWeight = FontWeight.Bold)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
             listOf(
                 "नेपालमा चालक अनुमति पत्र (License) लिने प्रक्रिया के हो?",
                 "नेपाली राहदानी (Passport) बनाउन के के कागजात चाहिन्छ?",
                 "व्यक्तिगत प्यान (PAN Card) कसरी बनाउने?",
                 "बाली संरक्षण र प्राङ्गारिक मल बनाउने ५ वटा उपाय भन्नुस्।"
             ).forEach { tip ->
                 item {
                     Card(
                         onClick = {
                             if (!isLoading) {
                                 viewModel.sendChatMessage(tip)
                             }
                         },
                         colors = CardDefaults.cardColors(containerColor = NavyBlue.copy(0.08f)),
                         shape = RoundedCornerShape(16.dp)
                     ) {
                         Text(
                             text = tip,
                             fontSize = 11.sp,
                             fontWeight = FontWeight.SemiBold,
                             color = NavyBlue,
                             modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                             maxLines = 1,
                             overflow = TextOverflow.Ellipsis
                         )
                     }
                 }
             }
        }

        // Text input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { viewModel.onAiInputChange(it) },
                placeholder = { Text("नागरिकता, लाइसेन्स वा बाली समस्या सोध्नुहोस्...", fontSize = 12.sp) },
                modifier = Modifier.weight(1f).testTag("chat_input_field"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputText.isNotBlank() && !isLoading) {
                        viewModel.sendChatMessage()
                        kb?.hide()
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CrimsonRed,
                    unfocusedBorderColor = MutedText.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            FloatingActionButton(
                onClick = {
                    if (inputText.isNotBlank() && !isLoading) {
                        viewModel.sendChatMessage()
                        kb?.hide()
                    }
                },
                containerColor = CrimsonRed,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(48.dp).testTag("chat_send_btn")
            ) {
                Icon(Icons.Default.Send, contentDescription = "पठाउनुहोस्")
            }
        }
    }
}
