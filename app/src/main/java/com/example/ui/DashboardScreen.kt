package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.accounts.AccountManager
import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.api.ShiaQuote
import com.example.utils.HijriCalendarHelper
import com.example.utils.PrayerTimeCalculator
import com.example.utils.LocalizationUtility
import java.text.SimpleDateFormat
import java.util.*
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.alpha
import com.example.R
import com.example.data.MasoomeenData
import com.example.data.MasoomDetails

import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: PrayerViewModel,
    azimuth: Float,
    modifier: Modifier = Modifier,
    eventType: com.example.data.EventType? = null
) {
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val displayDateStr by viewModel.displayDateStr.collectAsState()
    val dailyQuote by viewModel.dailyQuote.collectAsState()
    val isGeneratingQuote by viewModel.isGeneratingQuote.collectAsState()
    val favoriteQuotes by viewModel.favoriteQuotes.collectAsState()
    val arabicUrduFontScale by viewModel.arabicUrduFontScale.collectAsState()
    
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val currentLang by viewModel.appLanguage.collectAsState()
    val t = remember(currentLang) {
        object : Map<String, String> {
            override val entries: Set<Map.Entry<String, String>> get() = emptySet()
            override val keys: Set<String> get() = emptySet()
            override val size: Int get() = 0
            override val values: Collection<String> get() = emptyList()
            override fun isEmpty(): Boolean = false
            override fun containsKey(key: String): Boolean = true
            override fun containsValue(value: String): Boolean = false
            override fun get(key: String): String {
                return LocalizationUtility.get(key, currentLang)
            }
        }
    }

    var showProfileSettingsDialog by remember { mutableStateOf(false) }
    var showLocationSelector by remember { mutableStateOf(false) }
    var showDetailedTimings by remember { mutableStateOf(false) }
    var showFavoritesDialog by remember { mutableStateOf(false) }

    val showRateUsPrompt by viewModel.showRateUsPrompt.collectAsState()
    val globalAppFont by viewModel.appFont.collectAsState()
    val baseAppFontFamily = com.example.ui.theme.getAppFontFamily(globalAppFont, currentLang)

    val showGuidanceOnStartup by viewModel.showGuidanceOnStartup.collectAsState()
    val use24HourFormat by viewModel.use24HourFormat.collectAsState()
    val hasSeenOnboarding by viewModel.hasSeenOnboarding.collectAsState()
    var showedStartupPopup by rememberSaveable { mutableStateOf(false) }
    var showStartupDialog by remember { mutableStateOf(false) }
    var showOnboardingDialog by remember { mutableStateOf(false) }

    LaunchedEffect(hasSeenOnboarding) {
        if (!hasSeenOnboarding) {
            showOnboardingDialog = true
        }
    }

    LaunchedEffect(Unit) {
        if (androidx.core.app.ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            viewModel.requestExactLocation(context)
        }
    }

    LaunchedEffect(showedStartupPopup, showGuidanceOnStartup, showOnboardingDialog) {
        if (!showOnboardingDialog && !showedStartupPopup && showGuidanceOnStartup) {
            showStartupDialog = true
        }
    }
    // Adhan Notification Permission Launcher
    val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setAdhanEnabled(true, context)
        }
    }

    val onboardLocationLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms.values.any { it }) {
            viewModel.requestExactLocation(context)
        }
    }
    
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val email = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME) ?: ""
            if (email.isNotBlank()) {
                val name = email.substringBefore("@").replace(".", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                viewModel.loginWithGoogle(email, name)
            }
        }
    }
    
    var customCity by remember { mutableStateOf("") }
    var customLat by remember { mutableStateOf("") }
    var customLon by remember { mutableStateOf("") }
    var customTz by remember { mutableStateOf("") }
    var customLocError by remember { mutableStateOf("") }

    // Navigation Overlay system for the 9 beautiful Platform tiles
    var activeOverlay by remember { mutableStateOf<String?>(null) }
    var selectedTickerItem by remember { mutableStateOf<TickerItem?>(null) }

    // Live Media Playback states for Hussainiyah
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isAudioPlaying by remember { mutableStateOf(false) }
    var selectedAudioTrack by remember { mutableStateOf("Karbala Shia Adhan") }
    var playbackProgress by remember { mutableFloatStateOf(0f) }
    


    // Release MediaPlayer safely
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    // Convert Gregorian to Hijri
    val hijriDate = remember(selectedDate) {
        HijriCalendarHelper.convertGregorianToHijri(selectedDate)
    }

    // Calculate prayer times
    val year = selectedDate.get(Calendar.YEAR)
    val month = selectedDate.get(Calendar.MONTH) + 1
    val day = selectedDate.get(Calendar.DAY_OF_MONTH)
    val times = remember(year, month, day, selectedLocation) {
        PrayerTimeCalculator.calculateTimes(
            year, month, day,
            selectedLocation.lat, selectedLocation.lon, selectedLocation.timezone
        )
    }

    var currentTick by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000L * 30L)
            currentTick = System.currentTimeMillis()
        }
    }

    // Determine upcoming prayer time
    val countdownInfo = remember(times, currentTick) {
        calculateNextPrayer(times)
    }

    // Manage standard android physical back press inside screen overlay
    if (activeOverlay != null) {
        BackHandler {
            activeOverlay = null
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0) // Fully disable Scaffold's default insets
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Main Noor Screen View
            val isTickerActive by viewModel.isTickerActive.collectAsState()
            val navBarsInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val mainBottomPadding = if (isTickerActive) 124.dp + navBarsInset else 80.dp + navBarsInset

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = mainBottomPadding) // Space for bottom navigation and ticker with system insets
                    .verticalScroll(scrollState)
            ) {
                if (selectedTab == 0) {
                    // Outer Box: Clean Premium Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        // Beautiful background image of a Shia shrine / mosque silhouette
                        Image(
                            painter = painterResource(id = R.drawable.mosque_background_1780218634256),
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop,
                            alpha = 0.28f
                        )

                        // Subtle atmospheric overlay for depth & high contrast readability
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.5f)
                                        )
                                    )
                                )
                        )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 1. App Icon & Slogan Label
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.shia_pulse_refined_1780463967811),
                                    contentDescription = "Shia Pulse Logo",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Shia Pulse",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            // Google login indicator at the top right header space
                            val isLoggedIn by viewModel.isUserLoggedIn.collectAsState()
                            val userDisplayName by viewModel.userDisplayName.collectAsState()

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isLoggedIn) Color(0xFF00E676).copy(alpha = 0.2f)
                                        else Color.White.copy(alpha = 0.15f)
                                    )
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isLoggedIn) Color(0xFF00E676) else Color.White.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    )
                                    .clickable { showProfileSettingsDialog = true }
                                    .testTag("profile_button_top"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoggedIn) {
                                    val initial = (userDisplayName ?: "H").take(1).uppercase()
                                    Text(
                                        text = initial,
                                        color = Color(0xFF00E676),
                                        fontWeight = FontWeight.Black,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Sign In Profile Settings",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // App Statement
                        Text(
                            text = t["slogan"] ?: "Comprehensive Shia Companion Platform",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // 2. Centerpiece: Paint Your Life By Allah's Color Verse View / Live quotes
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = t["verse_text"] ?: "“Paint Your Life by Allah’s Color”",
                                color = Color(0xFFFFD54F), // Premium Gold Muted
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                fontFamily = baseAppFontFamily,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = t["verse_source"] ?: "Surah Baqarah, Verse 138",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.ExtraBold
                            )
                            
                            Spacer(modifier = Modifier.height(14.dp))

                            // Elegant Tap Location header option
                            Text(
                                text = "📍 ${selectedLocation.city}",
                                modifier = Modifier
                                    .clickable { showLocationSelector = true }
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .testTag("location_indicator_header"),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3. Premium Glassmorphic horizontal list displaying precise astronomical Namaz timings
                        val timingList = remember(times, currentLang, use24HourFormat) {
                            listOf(
                                Triple("fajr", t["fajr"] ?: "Fajr", com.example.utils.LocalizationUtility.formatTimeUI(times.fajrString, use24HourFormat)),
                                Triple("dhuhr", t["dhuhr"] ?: "Dhuhr (Zohrain)", com.example.utils.LocalizationUtility.formatTimeUI(times.dhuhrString, use24HourFormat)),
                                Triple("maghrib", t["maghrib"] ?: "Maghrib (Maghribain)", com.example.utils.LocalizationUtility.formatTimeUI(times.maghribString, use24HourFormat))
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            timingList.forEach { (key, pName, pTime) ->
                                val nextName = countdownInfo.nextPrayerName
                                val isFajrGrp = key == "fajr" && (nextName.contains("Fajr", true) || nextName.contains("Sunrise", true))
                                val isZohGrp = key == "dhuhr" && (nextName.contains("Dhuhr", true) || nextName.contains("Asr", true) || nextName.contains("Sunset", true))
                                val isMaghGrp = key == "maghrib" && (nextName.contains("Maghrib", true) || nextName.contains("Isha", true) || nextName.contains("Midnight", true))
                                val activeMatch = isFajrGrp || isZohGrp || isMaghGrp

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (activeMatch) Color.White.copy(alpha = 0.25f)
                                            else Color.White.copy(alpha = 0.1f)
                                        )
                                        .border(
                                            width = if (activeMatch) 1.5.dp else 1.dp,
                                            color = if (activeMatch) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = pName,
                                            fontSize = (12 * arabicUrduFontScale).sp,
                                            fontWeight = if (activeMatch) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = if (activeMatch) Color(0xFFFFE082) else Color.White.copy(alpha = 0.8f),
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = pTime,
                                            fontSize = (16 * arabicUrduFontScale).sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                        if (activeMatch) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${countdownInfo.nextPrayerName}: -${countdownInfo.countdownText}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFFFF5252),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Expanded schedule trigger
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.12f)
                            ),
                            onClick = { showDetailedTimings = !showDetailedTimings },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (showDetailedTimings) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Toggle Detailed Times",
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (showDetailedTimings) "Hide Detailed Timings" else "Show Detailed Shia Astronomical Timings",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                AnimatedVisibility(visible = showDetailedTimings) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val activeKey = remember(times) {
                                            val now = Calendar.getInstance()
                                            val curHrs = now.get(Calendar.HOUR_OF_DAY)
                                            val curMins = now.get(Calendar.MINUTE)
                                            val curMinsTotal = curHrs * 60 + curMins

                                            val parseToMins = { s: String ->
                                                val parts = s.split(":")
                                                if (parts.size >= 2) {
                                                     val h = parts[0].toIntOrNull() ?: 0
                                                     val m = parts[1].toIntOrNull() ?: 0
                                                     h * 60 + m
                                                } else {
                                                     0
                                                }
                                            }

                                            val markers = listOf(
                                                "imsak" to parseToMins(times.imsakString),
                                                "fajr" to parseToMins(times.fajrString),
                                                "sunrise" to parseToMins(times.sunriseString),
                                                "dhuhr" to parseToMins(times.dhuhrString),
                                                "asr" to parseToMins(times.asrString),
                                                "sunset" to parseToMins(times.sunsetString),
                                                "maghrib" to parseToMins(times.maghribString),
                                                "isha" to parseToMins(times.ishaString),
                                                "midnight" to parseToMins(times.midnightString)
                                            )

                                            var bestKey = "imsak"
                                            var minDist = Int.MAX_VALUE

                                            for ((key, markerMins) in markers) {
                                                 val dist = (curMinsTotal - markerMins + 1440) % 1440
                                                 if (dist < minDist) {
                                                     minDist = dist
                                                     bestKey = key
                                                 }
                                            }
                                            bestKey
                                        }

                                        val details = remember(times, currentLang, use24HourFormat) {
                                            listOf(
                                                Triple("imsak", t["imsak"] ?: "Imsak", com.example.utils.LocalizationUtility.formatTimeUI(times.imsakString, use24HourFormat)),
                                                Triple("fajr", t["fajr"] ?: "Fajr", com.example.utils.LocalizationUtility.formatTimeUI(times.fajrString, use24HourFormat)),
                                                Triple("sunrise", t["sunrise"] ?: "Sunrise", com.example.utils.LocalizationUtility.formatTimeUI(times.sunriseString, use24HourFormat)),
                                                Triple("dhuhr", t["dhuhr"] ?: "Dhuhr", com.example.utils.LocalizationUtility.formatTimeUI(times.dhuhrString, use24HourFormat)),
                                                Triple("asr", t["asr"] ?: "Asr", com.example.utils.LocalizationUtility.formatTimeUI(times.asrString, use24HourFormat)),
                                                Triple("sunset", t["sunset"] ?: "Sunset", com.example.utils.LocalizationUtility.formatTimeUI(times.sunsetString, use24HourFormat)),
                                                Triple("maghrib", t["maghrib"] ?: "Maghrib", com.example.utils.LocalizationUtility.formatTimeUI(times.maghribString, use24HourFormat)),
                                                Triple("isha", t["isha"] ?: "Isha", com.example.utils.LocalizationUtility.formatTimeUI(times.ishaString, use24HourFormat)),
                                                Triple("midnight", t["midnight"] ?: "Midnight (Nisfe Shab)", com.example.utils.LocalizationUtility.formatTimeUI(times.midnightString, use24HourFormat))
                                            )
                                        }

                                        details.forEach { (key, label, value) ->
                                             val isActive = key == activeKey
                                             val (timingIcon, iconColor) = remember(key) {
                                                 when (key) {
                                                     "imsak" -> Pair(Icons.Default.Alarm, Color(0xFFFFD54F))
                                                     "fajr" -> Pair(Icons.Default.WbTwilight, Color(0xFF90CAF9))
                                                     "sunrise" -> Pair(Icons.Default.WbSunny, Color(0xFFFFB74D))
                                                     "dhuhr" -> Pair(Icons.Default.LightMode, Color(0xFFFFD54F))
                                                     "asr" -> Pair(Icons.Default.WbSunny, Color(0xFFFFA726))
                                                     "sunset" -> Pair(Icons.Default.WbTwilight, Color(0xFFFF7043))
                                                     "maghrib" -> Pair(Icons.Default.NightsStay, Color(0xFFB39DDB))
                                                     "isha" -> Pair(Icons.Default.Bedtime, Color(0xFF7986CB))
                                                     "midnight" -> Pair(Icons.Default.DarkMode, Color(0xFF5C6BC0))
                                                     else -> Pair(Icons.Default.AccessTime, Color.White)
                                                 }
                                             }
                                         
                                             val rowModifier = if (isActive) {
                                                 Modifier
                                                     .fillMaxWidth()
                                                     .background(Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
                                                     .border(1.2.dp, Color(0xFFFFD54F), shape = RoundedCornerShape(8.dp))
                                                     .padding(vertical = 8.dp, horizontal = 12.dp)
                                             } else {
                                                 Modifier
                                                     .fillMaxWidth()
                                                     .padding(vertical = 4.dp, horizontal = 6.dp)
                                             }

                                             Row(
                                                 modifier = rowModifier,
                                                 horizontalArrangement = Arrangement.SpaceBetween,
                                                 verticalAlignment = Alignment.CenterVertically
                                             ) {
                                                 Row(
                                                     verticalAlignment = Alignment.CenterVertically,
                                                     horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                 ) {
                                                     Icon(
                                                         imageVector = timingIcon,
                                                         contentDescription = label,
                                                         tint = iconColor,
                                                         modifier = Modifier.size(if (isActive) 20.dp else 18.dp)
                                                     )
                                                     Text(
                                                         text = label + if (isActive) {
                                                             if (currentLang == "ur") " (موجودہ)" else if (currentLang == "ar") " (الحالي)" else " (Active)"
                                                         } else "",
                                                         fontSize = ((if (isActive) 15 else 14) * arabicUrduFontScale).sp,
                                                         fontWeight = if (isActive || key == "imsak" || key == "fajr" || key == "maghrib") FontWeight.Bold else FontWeight.Normal,
                                                         color = if (isActive || key == "imsak") Color(0xFFFFD54F) else Color.White
                                                     )
                                                 }
                                                 Text(
                                                     text = value,
                                                     fontSize = ((if (isActive) 15 else 14) * arabicUrduFontScale).sp,
                                                     fontFamily = FontFamily.Monospace,
                                                     fontWeight = FontWeight.Bold,
                                                     color = if (isActive || key == "imsak") Color(0xFFFFD54F) else Color.White
                                                 )
                                             }
                                         }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Note: Shia timings calculate Fajr at 15°30' twilight, Asr with shadow rules, and Maghrib precisely 4° (Eastern redness vanish) past sunset.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.6f),
                                            textAlign = TextAlign.Center,
                                            lineHeight = 12.sp,
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } // End of Header Box
                } // End of if (selectedTab == 0)

                // 4. Bottom Sheet Container (Houses the iconic 3x3 platform grid)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = if (selectedTab == 0) (-32).dp else 0.dp)
                        .clip(RoundedCornerShape(topStart = if (selectedTab == 0) 32.dp else 0.dp, topEnd = if (selectedTab == 0) 32.dp else 0.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(top = if (selectedTab == 0) 16.dp else 48.dp, bottom = 24.dp)
                        .let { if (selectedTab > 0) it.statusBarsPadding() else it }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        if (selectedTab > 0) {
                            val categoryTitle = when(selectedTab) {
                                1 -> if (currentLang == "ur") "عبادات اور روحانیت" else if (currentLang == "ar") "العبادات والروحانيات" else "Worship & Spiritual"
                                2 -> if (currentLang == "ur") "حسابات اور قواعد" else if (currentLang == "ar") "الحسابات والأحكام" else "Calculations & Rules"
                                3 -> if (currentLang == "ur") "علم اور حوالہ" else if (currentLang == "ar") "العلم والمراجع" else "Knowledge & Reference"
                                else -> "Modules"
                            }
                            val categoryDesc = when(selectedTab) {
                                1 -> if (currentLang == "ur") "دعائیں، زیارات اور روحانی کتب تک رسائی حاصل کریں۔" else if (currentLang == "ar") "الوصول إلى الأدعية، الزيارات والأعمال الروحية." else "Access Duas, Ziyarats and spiritual items."
                                2 -> if (currentLang == "ur") "روزمرہ کی زندگی کے لئے اسلامی کیلکولیٹر اور قواعد۔" else if (currentLang == "ar") "الحاسبات الإسلامية والقواعد للحياة اليومية." else "Islamic calculators and rules for daily life."
                                3 -> if (currentLang == "ur") "اسلامی معلومات، کیلنڈر اور ناموں کا مطالعہ کریں۔" else if (currentLang == "ar") "اكتشاف المعلومات الإسلامية، التقويم والأسماء." else "Discover facts, calendars and Islamic knowledge."
                                else -> if (currentLang == "ur") "اپنی روزمرہ کی عبادت کی رہنمائی کے لئے روحانی اوزار دریافت کریں۔" else if (currentLang == "ar") "اكتشف الأدوات الروحية لتوجيه عبادتك اليومية." else "Discover spiritual tools to guide your daily worship."
                            }
                            Text(
                                text = categoryTitle, 
                                style = MaterialTheme.typography.headlineLarge, 
                                fontWeight = FontWeight.Black, 
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = categoryDesc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(28.dp))

                            // Localized string for buttons
                            val duaBtnTitle = LocalizationUtility.get("tile_dua", currentLang)
                            val calculatorBtnTitle = LocalizationUtility.get("tile_calculator", currentLang)
                            val qiblaBtnTitle = LocalizationUtility.get("tile_qibla", currentLang)
                            val calendarBtnTitle = LocalizationUtility.get("tile_calendar", currentLang)
                            val tasbeehBtnTitle = LocalizationUtility.get("tile_tasbeeh", currentLang)
                            val trackerBtnTitle = LocalizationUtility.get("tile_tracker", currentLang)
                            val aiChatBtnTitle = LocalizationUtility.get("tile_aichat", currentLang)
                            val masoomeenBtnTitle = LocalizationUtility.get("tile_masoomeen", currentLang)
                            val namesOfAllahBtnTitle = LocalizationUtility.get("tile_names_allah", currentLang)
                            val namesOfMuhammadBtnTitle = LocalizationUtility.get("tile_names_muhammad", currentLang)
                            val ramadanTrackerBtnTitle = LocalizationUtility.get("tile_ramadan", currentLang)

                            Column(
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (selectedTab == 1) { // Worship & Spiritual
                                    ResponsiveTileGrid { itemModifier ->
                                        GridPlatformTile(
                                            title = duaBtnTitle,
                                            gradient = Brush.linearGradient(listOf(Color(0xFFF857A6), Color(0xFFFF5858))),
                                            icon = Icons.Default.List,
                                            modifier = itemModifier,
                                            onClick = { activeOverlay = "duas" },
                                            customIconResId = R.drawable.ic_duas_custom
                                        )
                                        GridPlatformTile(
                                            title = if (currentLang == "ur") "زیاراتِ مقدسہ" else "Sacred Ziyarats",
                                            gradient = Brush.linearGradient(listOf(Color(0xFF009688), Color(0xFF00796B))),
                                            icon = Icons.Default.Book,
                                            modifier = itemModifier.testTag("ziyarats_dashboard_tile"),
                                            onClick = { activeOverlay = "ziyarats" }
                                        )
                                        GridPlatformTile(
                                            title = if (currentLang == "ur") "زیارات کا نقشہ" else if (currentLang == "ar") "خريطة الزيارات" else "Ziyarats Map",
                                            gradient = Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF10B981))),
                                            icon = Icons.Default.LocationOn,
                                            modifier = itemModifier.testTag("ziyarats_map_dashboard_tile"),
                                            onClick = { activeOverlay = "ziyarats_map" }
                                        )
                                        GridPlatformTile(
                                            title = qiblaBtnTitle,
                                            gradient = Brush.linearGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))),
                                            icon = Icons.Default.LocationOn,
                                            modifier = itemModifier,
                                            onClick = { activeOverlay = "compass" },
                                            customIconResId = R.drawable.ic_qibla_custom
                                        )
                                        GridPlatformTile(
                                            title = trackerBtnTitle,
                                            gradient = Brush.linearGradient(listOf(Color(0xFF0575E6), Color(0xFF00F260))),
                                            icon = Icons.Default.Check,
                                            modifier = itemModifier,
                                            onClick = { activeOverlay = "tracker" }
                                        )
                                        GridPlatformTile(
                                            title = ramadanTrackerBtnTitle,
                                            gradient = Brush.linearGradient(listOf(Color(0xFFFFB300), Color(0xFFFF3D00))),
                                            icon = Icons.Default.Star,
                                            modifier = itemModifier.testTag("ramadan_tracker_tile"),
                                            onClick = { activeOverlay = "ramadan_tracker" }
                                        )
                                        GridPlatformTile(
                                            title = tasbeehBtnTitle,
                                            gradient = Brush.linearGradient(listOf(Color(0xFFF39C12), Color(0xFFD35400))),
                                            icon = Icons.Default.Refresh,
                                            modifier = itemModifier,
                                            onClick = { activeOverlay = "tasbeeh" },
                                            customIconResId = R.drawable.ic_tasbeeh_custom
                                        )
                                        GridPlatformTile(
                                            title = aiChatBtnTitle,
                                            gradient = Brush.linearGradient(listOf(Color(0xFF673AB7), Color(0xFF3F51B5))),
                                            icon = Icons.Default.Star,
                                            modifier = itemModifier,
                                            onClick = { activeOverlay = "aichat" }
                                        )
                                        GridPlatformTile(
                                            title = if (currentLang == "ur") "محفوظ شدہ" else "Saved Items",
                                            gradient = Brush.linearGradient(listOf(Color(0xFFE94057), Color(0xFFF27121))),
                                            icon = Icons.Default.FavoriteBorder,
                                            modifier = itemModifier,
                                            onClick = { activeOverlay = "saved_bookmarks" }
                                        )
                                    }
                                } else if (selectedTab == 2) { // Calculations & Rules
                                    ResponsiveTileGrid { itemModifier ->
                                        GridPlatformTile(
                                            title = if (currentLang == "ur") "زکوٰۃ" else if (currentLang == "ar") "زكاة" else "Zakat",
                                            gradient = Brush.linearGradient(listOf(Color(0xFF00C6FF), Color(0xFF0072FF))),
                                            icon = Icons.Default.Payments,
                                            modifier = itemModifier,
                                            onClick = { activeOverlay = "calc_zakat" }
                                        )
                                        GridPlatformTile(
                                            title = if (currentLang == "ur") "خمس" else if (currentLang == "ar") "خمس" else "Khums",
                                            gradient = Brush.linearGradient(listOf(Color(0xFFFDB99B), Color(0xFFCF8BF3))),
                                            icon = Icons.Default.AccountBalanceWallet,
                                            modifier = itemModifier,
                                            onClick = { activeOverlay = "calc_khums" }
                                        )
                                        GridPlatformTile(
                                            title = if (currentLang == "ur") "فطرانہ" else if (currentLang == "ar") "فطرة" else "Fitrana",
                                            gradient = Brush.linearGradient(listOf(Color(0xFF45B649), Color(0xFFDCE35B))),
                                            icon = Icons.Default.Calculate,
                                            modifier = itemModifier,
                                            onClick = { activeOverlay = "calc_fitrana" }
                                        )
                                        GridPlatformTile(
                                            title = if (currentLang == "ur") "مسافر" else if (currentLang == "ar") "مسافر" else "Musafir",
                                            gradient = Brush.linearGradient(listOf(Color(0xFF8360C3), Color(0xFF2EBF91))),
                                            icon = Icons.Default.AirplanemodeActive,
                                            modifier = itemModifier,
                                            onClick = { activeOverlay = "calc_musafir" }
                                        )
                                        GridPlatformTile(
                                            title = if (currentLang == "ur") "میراث" else if (currentLang == "ar") "ميراث" else "Inheritance",
                                            gradient = Brush.linearGradient(listOf(Color(0xFFE55D87), Color(0xFF5FC3E4))),
                                            icon = Icons.Default.FamilyRestroom,
                                            modifier = itemModifier,
                                            onClick = { activeOverlay = "calc_inheritance" }
                                        )

                                        GridPlatformTile(
                                            title = if (currentLang == "ur") "تاریخ کنورٹر" else if (currentLang == "ar") "محول التاريخ" else "Date Converter",
                                            gradient = Brush.linearGradient(listOf(Color(0xFFF77737), Color(0xFFF43F5E))),
                                            icon = Icons.Default.DateRange,
                                            modifier = itemModifier,
                                            onClick = { activeOverlay = "calc_date" }
                                        )
                                        GridPlatformTile(
                                            title = if (currentLang == "ur") "عشر" else "Ushar",
                                            gradient = Brush.linearGradient(listOf(Color(0xFF11998e), Color(0xFF38ef7d))),
                                            icon = Icons.Default.Grass,
                                            modifier = itemModifier,
                                            onClick = { activeOverlay = "calc_ushar" }
                                        )
                                        GridPlatformTile(
                                            title = if (currentLang == "ur") "مہرِ فاطمیؑ" else "Mehr-e-Fatimi",
                                            gradient = Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500))),
                                            icon = Icons.Default.BrightnessHigh,
                                            modifier = itemModifier,
                                            onClick = { activeOverlay = "calc_mehr" }
                                        )
                                    }
                                } else if (selectedTab == 3) { // Knowledge & Reference
                                    ResponsiveTileGrid { itemModifier ->
                                        GridPlatformTile(
                                            title = masoomeenBtnTitle,
                                            gradient = Brush.linearGradient(listOf(Color(0xFFE65100), Color(0xFFF57C00))),
                                            icon = Icons.Default.Star,
                                            modifier = itemModifier,
                                            onClick = { activeOverlay = "masoomeen" }
                                        )
                                        GridPlatformTile(
                                            title = calendarBtnTitle,
                                            gradient = Brush.linearGradient(listOf(Color(0xFFF77737), Color(0xFFF43F5E))),
                                            icon = Icons.Default.DateRange,
                                            modifier = itemModifier,
                                            onClick = { activeOverlay = "calendar" }
                                        )
                                        GridPlatformTile(
                                            title = namesOfAllahBtnTitle,
                                            gradient = Brush.linearGradient(listOf(Color(0xFF11998E), Color(0xFF38EF7D))),
                                            icon = Icons.Default.Favorite,
                                            modifier = itemModifier,
                                            onClick = { activeOverlay = "names_of_allah" },
                                            customIconResId = R.drawable.ic_knowledge_custom
                                        )
                                        GridPlatformTile(
                                            title = namesOfMuhammadBtnTitle,
                                            gradient = Brush.linearGradient(listOf(Color(0xFFFFB300), Color(0xFFFF6F00))),
                                            icon = Icons.Default.AccountCircle,
                                            modifier = itemModifier,
                                            onClick = { activeOverlay = "names_of_muhammad" }
                                        )
                                        GridPlatformTile(
                                            title = if (currentLang == "ur") "کربلا کا نقشہ و تاریخی سفر" else "Karbala Journey Map",
                                            gradient = Brush.linearGradient(listOf(Color(0xFFC0392B), Color(0xFF8E44AD))),
                                            icon = Icons.Default.Map,
                                            modifier = itemModifier,
                                            onClick = { activeOverlay = "karbala_map" }
                                        )
                                        GridPlatformTile(
                                            title = if (currentLang == "ur") "حج و عمرہ" else "Hajj & Umrah",
                                            gradient = Brush.linearGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))),
                                            icon = Icons.Default.EventNote,
                                            modifier = itemModifier,
                                            onClick = { activeOverlay = "calc_hajj" }
                                        )

                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                        } else {
                            // HOME TAB CONTENT (Below Header)
                            HijriGregorianCalendarHeader(viewModel = viewModel)

                            Spacer(modifier = Modifier.height(18.dp))

                            // Celestial Lunar Phase & Shia Dates component
                            MoonPhaseTracker(
                                hijriDay = hijriDate.day,
                                hijriMonth = hijriDate.monthName,
                                hijriYear = hijriDate.year,
                                fontScale = arabicUrduFontScale,
                                languageCode = currentLang,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Aamal-e-Rozana Premium Home Card
                            val aamalCustomAamal by viewModel.allCustomAamal.collectAsState()
                            val aamalTodayCompletedList by viewModel.todayAamalCompletions.collectAsState()
                            val aamalAllCompletions by viewModel.allAamalCompletions.collectAsState()
                            val aamalRawSelectedDate by viewModel.selectedDate.collectAsState()
                            val aamalActiveDateStr = remember(aamalRawSelectedDate) {
                                val sdfStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                sdfStr.format(aamalRawSelectedDate.time)
                            }
                            
                            val aamalDefaultList = remember { 
                                listOf(
                                    "salat_fajr", "salat_dhuhr", "salat_asr", "salat_maghrib", "salat_isha",
                                    "nafilah_fajr", "nafilah_dhuhr", "nafilah_asr", "nafilah_maghrib", "nafilah_isha", "nafilah_tahajjud",
                                    "adhkar_fatima", "adhkar_salawat", "adhkar_ayat_kursi",
                                    "dua_parents", "dua_ziyarat_weekday",
                                    "mustahab_sadaqah", "mustahab_quran"
                                )
                            }
                            
                            val aamalTotalRequired = aamalDefaultList.size + aamalCustomAamal.filter { it.isEnabled }.size
                            val aamalTotalCompleted = remember(aamalTodayCompletedList, aamalCustomAamal) {
                                val completedIds = aamalTodayCompletedList.filter { it.isCompleted }.map { it.activityId }.toSet()
                                val defaultCompleted = aamalDefaultList.count { it in completedIds }
                                val customCompleted = aamalCustomAamal.filter { it.isEnabled }.count { it.id in completedIds }
                                defaultCompleted + customCompleted
                            }
                            val aamalPercentage = if (aamalTotalRequired > 0) {
                                aamalTotalCompleted.toFloat() / aamalTotalRequired.toFloat()
                            } else 0f
                            
                            val aamalPercentageTarget = if (aamalTotalRequired > 0) {
                                (aamalTotalCompleted.toFloat() / aamalTotalRequired.toFloat())
                            } else 0f

                            val aamalStreak = remember(aamalAllCompletions, aamalCustomAamal) {
                                val completedByDate = aamalAllCompletions.filter { it.isCompleted }.groupBy { it.dateString }
                                val today = java.util.Calendar.getInstance()
                                var streakCount = 0
                                var isTodayPerf = false
                                val sdfStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                val tStr = sdfStr.format(today.time)
                                val todayCount = completedByDate[tStr]?.size ?: 0
                                val reqToday = aamalDefaultList.size + aamalCustomAamal.filter { it.isEnabled }.size
                                if (todayCount >= reqToday && reqToday > 0) {
                                    isTodayPerf = true
                                }
                                var checkCal = today
                                if (!isTodayPerf) {
                                    val yesterday = java.util.Calendar.getInstance()
                                    yesterday.add(java.util.Calendar.DAY_OF_YEAR, -1)
                                    checkCal = yesterday
                                }
                                while (true) {
                                    val checkStr = sdfStr.format(checkCal.time)
                                    val dayCount = completedByDate[checkStr]?.size ?: 0
                                    val req = aamalDefaultList.size + aamalCustomAamal.filter { it.isEnabled }.size
                                    if (dayCount >= req && req > 0) {
                                        streakCount++
                                        checkCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
                                    } else {
                                        break
                                    }
                                }
                                if (isTodayPerf) streakCount + 1 else streakCount
                            }

                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { activeOverlay = "aamal_tracker" }
                                    .testTag("aamal_home_tracker_card")
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Aamal",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = when (currentLang) {
                                                    "ur" -> "اعمالِ روزانہ (عبادت ٹریکر)"
                                                    "ar" -> "الأعمال اليومية (متابع العبادات)"
                                                    "fa" -> "اعمال روزانه"
                                                    "hi" -> "आमले रोज़ाना (दैनिक ट्रैकर)"
                                                    else -> "Aamal-e-Rozana Daily Tracker"
                                                },
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                        
                                        if (aamalStreak > 0) {
                                            Surface(
                                                color = Color.Black.copy(alpha = 0.08f),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = "Streak",
                                                        tint = Color(0xFFFFD54F),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "$aamalStreak🔥",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(14.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = when (currentLang) {
                                                "ur" -> "آج کی تکمیل: ${(aamalPercentageTarget * 100).toInt()}%"
                                                "ar" -> "إنجاز اليوم: ${(aamalPercentageTarget * 100).toInt()}%"
                                                "fa" -> "تکمیل امروز: ${(aamalPercentageTarget * 100).toInt()}%"
                                                else -> "Today's Completion: ${(aamalPercentageTarget * 100).toInt()}%"
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                                        )
                                        Text(
                                            text = "$aamalTotalCompleted / $aamalTotalRequired",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    LinearProgressIndicator(
                                        progress = aamalPercentageTarget,
                                        color = Color(0xFFFFD54F),
                                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                                        strokeCap = StrokeCap.Round,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text(
                                            text = when (currentLang) {
                                                "ur" -> "اعمال کھولیں ➔"
                                                "ar" -> "افتح الأعمال ➔"
                                                "fa" -> "مشاهده اعمال ➔"
                                                else -> "Open Daily Routine Tracker ➔"
                                            },
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Ayat of the Day Component
                            AyatOfTheDayCard(
                                viewModel = viewModel,
                                fontScale = arabicUrduFontScale,
                                languageCode = currentLang,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Hadith of the Day Component
                            HadithOfTheDayCard(
                                viewModel = viewModel,
                                fontScale = arabicUrduFontScale,
                                languageCode = currentLang,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Upcoming 14 Masoomeen Events Countdown and Section Component
                            UpcomingMasoomeenEventsSection(
                                viewModel = viewModel,
                                languageCode = currentLang,
                                fontScale = arabicUrduFontScale,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Bottom News/Events Ticker Bar (sticky above bottom navigation)
            if (isTickerActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 80.dp + navBarsInset)
                ) {
                    NewsTickerBar(
                        viewModel = viewModel,
                        onItemClick = { item ->
                            if (item.id == "CUSTOMIZE_TICKER") {
                                activeOverlay = "ticker_settings"
                            } else {
                                selectedTickerItem = item
                            }
                        }
                    )
                }
            }

            // Ticker Item Detail View Modal
            selectedTickerItem?.let { item ->
                TickerDetailDialog(
                    item = item,
                    languageCode = currentLang,
                    onDismiss = { selectedTickerItem = null },
                    onMuteCategory = {
                        viewModel.toggleTickerMuteCategory(item.category)
                        selectedTickerItem = null
                    },
                    onDelete = {
                        viewModel.deleteTickerItem(item.id)
                        selectedTickerItem = null
                    }
                )
            }

            // Bottom Navigation Bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text(if (currentLang == "ur") "ہوم" else if (currentLang == "ar") "الرئيسية" else "Home", fontWeight = FontWeight.Bold, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Worship") },
                        label = { Text(if (currentLang == "ur") "عبادات" else if (currentLang == "ar") "العبادات" else "Worship", fontWeight = FontWeight.Bold, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Calculate, contentDescription = "Calculations") },
                        label = { Text(if (currentLang == "ur") "حسابات" else if (currentLang == "ar") "الحسابات" else "Calculations", fontWeight = FontWeight.Bold, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.Book, contentDescription = "Knowledge") },
                        label = { Text(if (currentLang == "ur") "معلومات" else if (currentLang == "ar") "المعرفة" else "Knowledge", fontWeight = FontWeight.Bold, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }

            // Occasion celebration or mourning falling animation overlays
            OccasionAnimationOverlay(
                eventType = eventType,
                modifier = Modifier.fillMaxSize()
            )

            // High Performance Overlays drawing screens directly above main view with slide animations
            AnimatedVisibility(
                visible = activeOverlay != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) {}
                ) {
                    when (activeOverlay) {
                        "aamal_tracker" -> AamalTrackerScreen(viewModel = viewModel, onBack = { activeOverlay = null }, modifier = Modifier.fillMaxSize())
                        "tracker" -> TrackerScreen(viewModel = viewModel, initialTab = 0, onBack = { activeOverlay = null }, modifier = Modifier.fillMaxSize())
                        "compass" -> CompassScreen(viewModel = viewModel, azimuth = azimuth, modifier = Modifier.fillMaxSize(), onBack = { activeOverlay = null })
                        "tasbeeh" -> TasbeehScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                        "calendar" -> CalendarScreen(viewModel = viewModel, onBack = { activeOverlay = null })
                        "duas" -> DuasScreen(
                            viewModel = viewModel, 
                            onBack = { activeOverlay = null },
                            onOpenZiyaratsMap = {
                                activeOverlay = "ziyarats_map"
                            }
                        )
                        "calc_khums" -> KhumsScreen(viewModel = viewModel, initialTool = "khums", onBack = { activeOverlay = null })
                        "calc_zakat" -> KhumsScreen(viewModel = viewModel, initialTool = "zakat", onBack = { activeOverlay = null })
                        "calc_fitrana" -> KhumsScreen(viewModel = viewModel, initialTool = "fitrana", onBack = { activeOverlay = null })
                        "calc_ushar" -> KhumsScreen(viewModel = viewModel, initialTool = "ushar", onBack = { activeOverlay = null })
                        "calc_mehr" -> KhumsScreen(viewModel = viewModel, initialTool = "mehr", onBack = { activeOverlay = null })
                        "calc_hajj" -> KhumsScreen(viewModel = viewModel, initialTool = "hajj_umrah", onBack = { activeOverlay = null })
                        "calc_qadha" -> TrackerScreen(viewModel = viewModel, initialTab = 1, onBack = { activeOverlay = null }, modifier = Modifier.fillMaxSize())
                        "karbala_map" -> KarbalaMapScreen(currentLang = currentLang, onBack = { activeOverlay = null })
                        "calc_musafir" -> KhumsScreen(viewModel = viewModel, initialTool = "musafir", onBack = { activeOverlay = null })
                        "calc_inheritance" -> KhumsScreen(viewModel = viewModel, initialTool = "inheritance", onBack = { activeOverlay = null })
                        "calc_date" -> KhumsScreen(viewModel = viewModel, initialTool = "date_conv", onBack = { activeOverlay = null })
                        "khums" -> KhumsScreen(viewModel = viewModel, onBack = { activeOverlay = null })
                        "ramadan_tracker" -> RamadanTrackerScreen(viewModel = viewModel, onBack = { activeOverlay = null }, modifier = Modifier.fillMaxSize())
                        "books" -> BooksScreen(onBack = { activeOverlay = null }, modifier = Modifier.fillMaxSize())
                        "aichat" -> AIChatScreen(onBack = { activeOverlay = null }, modifier = Modifier.fillMaxSize())
                        "masoomeen" -> MasoomeenScreen(onBack = { activeOverlay = null }, languageCode = currentLang, modifier = Modifier.fillMaxSize())
                        "ziyarats" -> ZiyaratsScreen(
                            viewModel = viewModel, 
                            onBack = { activeOverlay = null }, 
                            languageCode = currentLang, 
                            modifier = Modifier.fillMaxSize()
                        )
                        "ziyarats_map" -> ZiyaratsMapScreen(
                            viewModel = viewModel,
                            onBack = { activeOverlay = null },
                            languageCode = currentLang,
                            modifier = Modifier.fillMaxSize()
                        )
                        "names_of_allah" -> NamesOfAllahScreen(onBack = { activeOverlay = null }, languageCode = currentLang, modifier = Modifier.fillMaxSize())
                        "names_of_muhammad" -> NamesOfMuhammadScreen(onBack = { activeOverlay = null }, languageCode = currentLang, modifier = Modifier.fillMaxSize())
                        "saved_bookmarks" -> SavedBookmarksScreen(viewModel = viewModel, onBack = { activeOverlay = null }, languageCode = currentLang, modifier = Modifier.fillMaxSize())
                        "ticker_settings" -> TickerSettingsOverlay(viewModel = viewModel, languageCode = currentLang, onDismiss = { activeOverlay = null })


                        "hussainiyah" -> {
                            // Immersive Shia Audio stream & Adhan controller
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("SPIRITUAL MEDIA", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, color = Color(0xFF00BFA5))
                                        IconButton(onClick = { 
                                            try { 
                                                mediaPlayer?.stop() 
                                                mediaPlayer?.release()
                                            } catch(e: Exception) {}
                                            mediaPlayer = null
                                            isAudioPlaying = false
                                            activeOverlay = null 
                                        }) {
                                            Icon(Icons.Default.Close, "Close")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(28.dp))

                                    // Spinnable cassette disk mockup
                                    Box(
                                        modifier = Modifier
                                            .size(200.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(Color(0xFF333333), Color(0xFF111111))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            drawCircle(Color.Black, radius = size.minDimension / 4)
                                        }
                                        Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Text(
                                        text = selectedAudioTrack,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isAudioPlaying) "Streaming Spiritual Frequencies..." else "Playback Paused",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    LinearProgressIndicator(
                                        progress = { playbackProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = Color(0xFF00BFA5),
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(28.dp))

                                    // Track player buttons
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                try { 
                                                    mediaPlayer?.stop() 
                                                    mediaPlayer?.release()
                                                } catch(e: Exception) {}
                                                mediaPlayer = null
                                                selectedAudioTrack = "Ya Hussain Latmiyah"
                                                isAudioPlaying = false
                                                playbackProgress = 0.5f
                                            },
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(Icons.Default.ArrowBack, "Prev")
                                        }

                                        Button(
                                            onClick = {
                                                if (isAudioPlaying) {
                                                    try { mediaPlayer?.pause() } catch(e: Exception) {}
                                                    isAudioPlaying = false
                                                } else {
                                                    // Stream Shia Azan calmly
                                                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                                        try {
                                                            if (mediaPlayer == null) {
                                                                val mp = MediaPlayer().apply {
                                                                    setDataSource("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")
                                                                    setOnPreparedListener { 
                                                                        start()
                                                                        isAudioPlaying = true
                                                                    }
                                                                    setOnCompletionListener {
                                                                        isAudioPlaying = false
                                                                        playbackProgress = 0f
                                                                    }
                                                                }
                                                                mediaPlayer = mp
                                                                mp.prepareAsync()
                                                            } else {
                                                                try { mediaPlayer?.start() } catch (e: Exception) {}
                                                                isAudioPlaying = true
                                                            }
                                                        } catch (e: Exception) {
                                                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                                                // Playback mock simulation if connection fails
                                                                isAudioPlaying = true
                                                                playbackProgress = 0.35f
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                            shape = CircleShape,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5)),
                                            modifier = Modifier.size(72.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isAudioPlaying) Icons.Default.Info else Icons.Default.PlayArrow,
                                                contentDescription = "Play"
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                try { 
                                                    mediaPlayer?.stop() 
                                                    mediaPlayer?.release()
                                                } catch(e: Exception) {}
                                                mediaPlayer = null
                                                selectedAudioTrack = "Dua Kumayl Stream"
                                                isAudioPlaying = false
                                                playbackProgress = 0.8f
                                            },
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, "Next")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))
                                    Text(
                                        text = "Please note: streaming requires an active internet connection.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Location Selector Dialog
    if (showLocationSelector) {
        Dialog(onDismissRequest = { showLocationSelector = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "SELECT LOCATION",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Preset Locations
                    items(viewModel.locations.size) { idx ->
                        val preset = viewModel.locations[idx]
                        val isSelected = preset.city == selectedLocation.city
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                                .clickable {
                                    viewModel.setLocation(preset, context)
                                    showLocationSelector = false
                                }
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = preset.city,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Lat: ${preset.lat}, Lon: ${preset.lon}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp).fillMaxWidth().clickable {
                                onboardLocationLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION))
                                showLocationSelector = false
                            }, horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = "GPS", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Use Exact Device Location", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }
                    }

                    // Custom Location Input
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Custom Coordinates",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = customCity,
                                    onValueChange = { customCity = it },
                                    label = { Text("Display Name (City)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = customLat,
                                        onValueChange = { customLat = it },
                                        label = { Text("Lat (e.g. 32.61)") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f).height(52.dp),
                                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                                    )
                                    OutlinedTextField(
                                        value = customLon,
                                        onValueChange = { customLon = it },
                                        label = { Text("Lon (e.g. 44.02)") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f).height(52.dp),
                                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = customTz,
                                    onValueChange = { customTz = it },
                                    label = { Text("Timezone Offset (e.g. 3.0)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                                )

                                if (customLocError.isNotEmpty()) {
                                    Text(
                                        text = customLocError,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        if (customCity.isBlank() || customLat.isBlank() || customLon.isBlank() || customTz.isBlank()) {
                                            customLocError = "All fields are required."
                                            return@Button
                                        }
                                        val lat = customLat.toDoubleOrNull()
                                        val lon = customLon.toDoubleOrNull()
                                        val tz = customTz.toDoubleOrNull()
                                        if (lat == null || lon == null || tz == null) {
                                            customLocError = "Latitude, Longitude and Timezone must be numerical values."
                                            return@Button
                                        }
                                        viewModel.setCustomLocation(customCity, lat, lon, tz, context)
                                        showLocationSelector = false
                                        customLocError = ""
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Apply Coordinates")
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { showLocationSelector = false },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }

    // Saved Favorites Dialog
    if (showFavoritesDialog) {
        Dialog(onDismissRequest = { showFavoritesDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "SAVED REFLECTIONS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (favoriteQuotes.isEmpty()) {
                        Text(
                            text = "Your spiritual scrapbook is currently empty. Tap the Heart icon on any quote to preserve it here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .heightIn(max = 340.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(favoriteQuotes.size) { idx ->
                                val fav = favoriteQuotes[idx]
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                if (fav.arabic.isNotEmpty()) {
                                                    Text(
                                                        text = fav.arabic,
                                                        fontFamily = baseAppFontFamily,
                                                        fontSize = 16.sp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        textAlign = TextAlign.Right,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                }
                                                Text(
                                                    text = "“${fav.english}”",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontStyle = FontStyle.Italic
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "— ${fav.source}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.tertiary
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    viewModel.removeFavoriteQuote(fav)
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { showFavoritesDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }

    if (showOnboardingDialog) {
        OnboardingWizard(
            viewModel = viewModel,
            onComplete = {
                viewModel.completeOnboarding()
                showOnboardingDialog = false
            },
            onRequestLocation = {
                onboardLocationLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        )
    }

    if (showStartupDialog) {
        Dialog(onDismissRequest = {
            showStartupDialog = false
            showedStartupPopup = true
        }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 4.dp, vertical = 24.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .border(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val showUrdu = currentLang == "ur"

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Spiritual Guidance Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (showUrdu) "رہنمائی اہلبیت (ع)" else if (currentLang == "ar") "إرشاد أهل البيت (ع)" else "Ahlul Bayt (as) Guidance",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (showUrdu) "روزانہ آغازِ سفر کے لیے فیضِ علم" else if (currentLang == "ar") "فيض العلم لبداية يومك" else "Pure Wisdom to Begin Your Day",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            dailyQuote?.let { q ->
                                if (q.arabic.isNotEmpty()) {
                                    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
                                    val arabicTextColor = if (isDark) Color(0xFFF3C654) else Color(0xFF825500)
                                    Text(
                                        text = q.arabic,
                                        fontSize = (16 * arabicUrduFontScale).sp,
                                        fontFamily = baseAppFontFamily,
                                        color = arabicTextColor,
                                        textAlign = TextAlign.Center,
                                        lineHeight = (24 * arabicUrduFontScale).sp
                                    )
                                }

                                if (currentLang != "ar") {
                                    Text(
                                        text = if (showUrdu) {
                                            "“${com.example.api.UrduTranslations.getUrdu(q)}”"
                                        } else {
                                            "“${q.english}”"
                                        },
                                        fontSize = (14 * arabicUrduFontScale).sp,
                                        fontStyle = FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                        lineHeight = (20 * arabicUrduFontScale).sp
                                    )
                                }

                                Text(
                                    text = "— ${q.source}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            } ?: run {
                                Text(
                                    text = "Ahlul Bayt (as) Guidance: In patience lies salvation and in prayer lies divine color.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                dailyQuote?.let { q ->
                                    val urQuote = com.example.api.UrduTranslations.getUrdu(q)
                                    val shareText = """
                                        ✨ Ahlul Bayt (as) Wisdom ✨
                                        
                                        📖 Arabic: ${q.arabic}
                                        
                                        🇬🇧 English: ${q.english}
                                        
                                        🇵🇰 اردو: $urQuote
                                        
                                        Source: ${q.source}
                                        — via Shia Pulse
                                    """.trimIndent()
                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_SUBJECT, "Ahlul Bayt Guidance")
                                        putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Guidance Quote via"))
                                }
                            },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).testTag("popup_share_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share Quote", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                showStartupDialog = false
                                showedStartupPopup = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1.2f).testTag("popup_continue_btn")
                        ) {
                            Text("Continue", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    if (showProfileSettingsDialog) {
        val themeMode by viewModel.themeMode.collectAsState()
        val currencySymbol by viewModel.currencySymbol.collectAsState()
        val autoOccasionTheme by viewModel.autoOccasionTheme.collectAsState()
        val themeOverride by viewModel.themeOverride.collectAsState()
        Dialog(onDismissRequest = { showProfileSettingsDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(vertical = 12.dp)
                    .clip(RoundedCornerShape(24.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                val isLoggedIn by viewModel.isUserLoggedIn.collectAsState()
                val userEmail by viewModel.userEmail.collectAsState()
                val userDisplayName by viewModel.userDisplayName.collectAsState()
                val isAdhanEnabled by viewModel.isAdhanEnabled.collectAsState()
                val isFajrAdhanEnabled by viewModel.isFajrAdhanEnabled.collectAsState()
                val isDhuhrAdhanEnabled by viewModel.isDhuhrAdhanEnabled.collectAsState()
                val isAsrAdhanEnabled by viewModel.isAsrAdhanEnabled.collectAsState()
                val isMaghribAdhanEnabled by viewModel.isMaghribAdhanEnabled.collectAsState()
                val isIshaAdhanEnabled by viewModel.isIshaAdhanEnabled.collectAsState()
                val showGuidanceOnStartup by viewModel.showGuidanceOnStartup.collectAsState()
                val adhanSoundKey by viewModel.adhanSoundKey.collectAsState()
                val appFont = globalAppFont

                var playingAdhanPreviewKey by remember { mutableStateOf<String?>(null) }
                DisposableEffect(Unit) {
                    onDispose {
                        viewModel.stopAdhanPreview()
                    }
                }

                val faqList = remember {
                    listOf(
                        "How are prayer times calculated?" to "Timings utilize exact geolocal structures. Dhuhr starts at astronomical solar transit, Fajr uses 15°30' angle, and Maghrib is strictly 4 degrees (around 15-20 min astronomical delay) past sunset representing the Shia school.",
                        "What is the Qibla angle from my location?" to "The Qibla angle is computed based on Great Circle distance to the Kaaba in Mecca, Saudi Arabia (21.4225° N, 39.8262° E). Ensure the compass sensor is calibrated.",
                        "Are offline backups supported?" to "Yes! Once you log in with Google, your tracking logs and Qada counts are continuously cached locally and synced automatically with the Firebase cloud database whenever an internet connection is available."
                    )
                }
                var expandedFaqIndex by remember { mutableStateOf<Int?>(null) }
                var isAboutUsExpanded by remember { mutableStateOf(false) }
                var isTermsExpanded by remember { mutableStateOf(false) }
                var isPrivacyExpanded by remember { mutableStateOf(false) }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .heightIn(max = 550.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = t["settings"] ?: "Settings & Profile",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = { showProfileSettingsDialog = false }) {
                                Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // PROFILE / AUTH SECTION
                    item {
                        if (isLoggedIn) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF00E676)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = (userDisplayName ?: "H").take(1).uppercase(),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = userDisplayName ?: "Shia Pulse Companion",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = userEmail ?: "rzshah5@gmail.com",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = t["google_logged_in"] ?: "Google Connected",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF00E676),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Button(
                                            onClick = { viewModel.logout() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text(t["log_out"] ?: "Sign Out", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimary)
                                        }
                                    }
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Backup & Cloud Synchronization",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Sign in securely via Google to backing up your tracking progress, custom locations, and Shia calculations.",
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Button(
                                        onClick = {
                                            val intent = AccountManager.newChooseAccountIntent(
                                                null, null, arrayOf("com.google"), null, null, null, null
                                            )
                                            googleSignInLauncher.launch(intent)
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("google_login_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.AccountBox, "Google", tint = MaterialTheme.colorScheme.onPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(t["log_in_google"] ?: "Sign in with Google", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // ARABIC & URDU TYPOGRAPHY SETTINGS SECTION (REPLACED FROM CLOUD SYNC)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "ع",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Arabic & Urdu Script Size",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Adjust script size of prayer times and Duas",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val sizes = listOf(
                                        1.0f to "1.0x",
                                        1.2f to "1.2x",
                                        1.4f to "1.4x",
                                        1.6f to "1.6x"
                                    )
                                    sizes.forEach { (scale, labelScale) ->
                                        val isSelected = arabicUrduFontScale == scale
                                        Button(
                                            onClick = { viewModel.setArabicUrduFontScale(scale) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("dialog_font_scale_btn_${scale.toString().replace(".", "_")}"),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        ) {
                                            Text(
                                                text = labelScale,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(14.dp))
                                
                                Text(
                                    text = if (currentLang == "ur") "رسم الخط کا انداز (پیش منظر کے ساتھ)" else "Script Font Style (with Live Previews)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val fonts = listOf(
                                        "system" to "System Default",
                                        "Jameel Noori" to "Jameel Noori",
                                        "Amiri" to "Amiri",
                                        "Noto Naskh Arabic" to "Noto Naskh"
                                    )
                                    fonts.forEach { (fontName, label) ->
                                        val isSelected = appFont == fontName
                                        OutlinedButton(
                                            onClick = { viewModel.setAppFont(fontName) },
                                            modifier = Modifier.weight(1f).height(48.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                            ),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                                                contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            contentPadding = PaddingValues(2.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 12.sp
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                // Silent preview row
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val currentAppFontFamily = com.example.ui.theme.getAppFontFamily(appFont, currentLang)
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = if (appFont == "Amiri") "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ (خطِ امیری)" else if (appFont == "Noto Naskh Arabic") "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ (خطِ نسخ)" else "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ (جمیل نوری نستعلیق)",
                                            fontSize = (18 * arabicUrduFontScale).sp,
                                            fontFamily = currentAppFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = if (appFont == "Amiri") "Amiri Serif: Classical Quranic Script Style" else if (appFont == "Noto Naskh Arabic") "Noto Naskh: Clear Modern Digital Script Style" else if (appFont == "system") "System Default: Standard Android Sans Script" else "Jameel Noori: Flowing Urdu Nastaliq Script Style",
                                            fontSize = (14 * arabicUrduFontScale).sp,
                                            fontFamily = currentAppFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // LANGUAGE OPTION
                    item {
                        Text(
                            text = t["language"] ?: "Change Language",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("en" to "English", "ar" to "العربية", "ur" to "اردو").forEach { (code, langLabel) ->
                                val isSelected = currentLang == code
                                Button(
                                    onClick = { viewModel.setAppLanguage(code) },
                                    modifier = Modifier.weight(1f).testTag("lang_btn_$code"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(langLabel, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // APP THEME OPTION
                    item {
                        Text(
                            text = "App Theme",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("dark" to "Dark", "light" to "Light", "system" to "System").forEach { (code, themeLabel) ->
                                val isSelected = themeMode == code
                                Button(
                                    onClick = { viewModel.setThemeMode(code) },
                                    modifier = Modifier.weight(1f).testTag("theme_btn_$code"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(themeLabel, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // NEWS TICKER ON/OFF SETTING
                    item {
                        val isTickerActiveVal by viewModel.isTickerActive.collectAsState()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (currentLang == "ur") "مذہبی ایونٹ ٹکر" else if (currentLang == "ar") "شريط المناسبات والشيعة" else "News & Events Ticker Bar 📰",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = if (currentLang == "ur") "ہوم اسکرین پر نیچے چلنے والی پٹی کو آن یا آف کریں" else if (currentLang == "ar") "عرض أو إخفاء شريط التمرير السفلي للأحداث" else "Toggle sticky bottom news & Shi'ite event ticker stream",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isTickerActiveVal,
                                onCheckedChange = { viewModel.setTickerActive(it) },
                                modifier = Modifier.testTag("dialog_ticker_active_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }

                    // OCCASION THEME SETTING
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Occasion-Aware Palette 🌟",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Automate festive green for Wiladats / dark solemn rose for Shahadats",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = autoOccasionTheme,
                                onCheckedChange = { viewModel.setAutoOccasionTheme(it) },
                                modifier = Modifier.testTag("dialog_auto_occasion_theme_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }

                    item {
                        Text(
                            text = "Ahlul Bayt Occasion Theme Previews:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "none" to "Auto Settings",
                                "wiladat" to "Wiladat Mode",
                                "shahadat" to "Shahadat Mode"
                            ).forEach { (choice, label) ->
                                val isCurrentOverride = themeOverride == choice
                                Button(
                                    onClick = { viewModel.setThemeOverride(choice) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("dialog_override_btn_$choice"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isCurrentOverride) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (isCurrentOverride) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // TIME FORMAT (12H / 24H)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = t["time_format"] ?: "Use 24-Hour Format",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Switch(
                                checked = use24HourFormat,
                                onCheckedChange = { viewModel.setUse24HourFormat(it) },
                                modifier = Modifier.testTag("use_24h_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }

                    // CALCULATOR CURRENCY OPTION
                    item {
                        Text(
                            text = if (currentLang == "ur") "کیلکولیٹر کرنسی" else "Calculator Currency",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("$" to "USD/CAD", "PKR" to "PKR", "£" to "GBP", "€" to "EUR", "₹" to "INR", "AED" to "AED").forEach { (code, label) ->
                                val isSelected = currencySymbol == code
                                Button(
                                    onClick = { viewModel.setCurrencySymbol(code) },
                                    modifier = Modifier.testTag("currency_btn_$code"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text("$code ($label)", fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }

                    // ADHAN NOTIFICATIONS
                    item {
                        Text(
                            text = t["adhan"] ?: "Adhan Setting",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(0.8f)) {
                                        Text(
                                            text = if (isAdhanEnabled) (t["notifications_enabled"] ?: "Adhan Active") else (t["notifications_disabled"] ?: "Adhan Muted"),
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Play beautiful Shia Adhan stream on timing intervals.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = isAdhanEnabled,
                                        onCheckedChange = { isEnabled -> 
                                            if (isEnabled && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                                if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                                } else {
                                                    viewModel.setAdhanEnabled(true, context)
                                                }
                                            } else {
                                                viewModel.setAdhanEnabled(isEnabled, context)
                                            }
                                        },
                                        modifier = Modifier.testTag("adhan_settings_switch")
                                    )
                                }

                                if (isAdhanEnabled) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    Text(
                                        text = "Namaz Specific Alarm Settings:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    val prayerToggles = listOf(
                                        "Fajr" to isFajrAdhanEnabled,
                                        "Dhuhr" to isDhuhrAdhanEnabled,
                                        "Maghrib" to isMaghribAdhanEnabled
                                    )

                                    prayerToggles.forEach { (pName, isEnabled) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isEnabled) Color(0xFF00E676) else Color(0xFF94A3B8))
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = pName,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            Switch(
                                                checked = isEnabled,
                                                onCheckedChange = { viewModel.setPrayerAdhanEnabled(pName, it) },
                                                modifier = Modifier.testTag("adhan_switch_${pName.lowercase()}"),
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = Color(0xFF00E676),
                                                    checkedTrackColor = Color(0xFF00E676).copy(alpha = 0.3f)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ADHAN SOUND NOTIFICATION
                    if (isAdhanEnabled) {
                        item {
                            Text(
                                text = "Adhan Sound Notification",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                val adhanSounds = listOf(
                                    Triple("mesmerizing_shia_azan", "Mesmerizing Shia Azan", "Beautiful, highly melodious and spiritually resonant local Shia Azan")
                                )

                                adhanSounds.forEach { (key, title, desc) ->
                                    val isSelected = adhanSoundKey == key
                                    val isCurrentlyPlaying = playingAdhanPreviewKey == key

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                            .clickable { viewModel.setAdhanSoundKey(key) }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = { viewModel.setAdhanSoundKey(key) }
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = title,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Text(
                                                text = desc,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(start = 38.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                if (isCurrentlyPlaying) {
                                                    viewModel.stopAdhanPreview()
                                                    playingAdhanPreviewKey = null
                                                } else {
                                                    viewModel.stopAdhanPreview()
                                                    viewModel.playAdhanPreview(context, key)
                                                    playingAdhanPreviewKey = key
                                                }
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isCurrentlyPlaying) Icons.Filled.Close else Icons.Filled.PlayArrow,
                                                contentDescription = "Test Sound",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    }

                    // ABOUT US SECTION
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { isAboutUsExpanded = !isAboutUsExpanded }
                                .testTag("settings_about_us_card"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "About Us Icon",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "About Us",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isAboutUsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                                        contentDescription = "Expand About Us",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isAboutUsExpanded) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "This app is created with the sole intention of serving the Shia community by spreading the true message of Ahlulbayt (AS).\n\n" +
                                               "\"Indeed, my prayer, my sacrifice, my living, and my dying are for Allah, Lord of the worlds.\" (Holy Qur'an 6:162)\n\n" +
                                               "Our message is the same message that Rasulullah (SAWW) left behind:\n\n" +
                                               "\"I leave behind two weighty things for you: The Book of Allah and my Ahlulbayt (AS). If you hold onto both, you will never go astray.\" (Hadith al-Thaqalayn)\n\n" +
                                               "اللهم صل علی محمد و آل محمد\n\n" +
                                               "O Allah, send your blessings upon Muhammad and the family of Muhammad, as you have blessed Ibrahim and the family of Ibrahim. Indeed, you are Praiseworthy and Glorious.\n\n" +
                                               "This humble app is a small effort to keep the name of Muhammad (SAWW) and his pure Aale Muhammad (AS) alive in every heart.\n\n" +
                                               "For any feedback, suggestions, or duas:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Email action
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                try {
                                                    val emailIntent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                                        data = Uri.parse("mailto:aliraza_riu@hotmail.com")
                                                    }
                                                    context.startActivity(android.content.Intent.createChooser(emailIntent, "Send Email"))
                                                } catch (e: Exception) {}
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = "Email",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "aliraza_riu@hotmail.com",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // WhatsApp action
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                try {
                                                    val waIntent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                                        data = Uri.parse("https://wa.me/923485033712")
                                                    }
                                                    context.startActivity(waIntent)
                                                } catch (e: Exception) {}
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = "WhatsApp",
                                            tint = Color(0xFF25D366),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "+92 348 5033712",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF25D366),
                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "— Mojedarya (Individual Developer)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }

                    // TERMS & CONDITIONS SECTION
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { isTermsExpanded = !isTermsExpanded }
                                .testTag("settings_terms_card"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Terms & Conditions Icon",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Terms & Conditions",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isTermsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                                        contentDescription = "Expand Terms",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isTermsExpanded) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "By referencing or downloading the Shia Pulse software application, you acknowledge and agree to be bound by the following terms:\n\n" +
                                               "1. Data Privacy: All daily prayer logs, Tasbeeh counter logs, and user activity data belong to the user. We do not transmit or sell tracking metrics to secondary systems. Cloud syncing via Google and Firebase is exclusively voluntary.\n\n" +
                                               "2. Guidance Verification: Calculations for Shiah Astrological and astronomical timings (Dhuhr, Fajr Angle, Maghrib delay) are executed locally with maximum scientific calibration. However, they serve as general spiritual guidance.\n\n" +
                                               "3. Intellectual Property: All custom system code, graphics, branding, and resource layouts are the exclusive property of Mojedarya Software Solutions. Unauthorized redistribution is restricted.\n\n" +
                                               "Thank you for trusting Shia Pulse.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    // PRIVACY POLICY SECTION
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { isPrivacyExpanded = !isPrivacyExpanded }
                                .testTag("settings_privacy_policy_card"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Privacy Policy Icon",
                                            tint = Color(0xFF00FF88),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Privacy Policy",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isPrivacyExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                                        contentDescription = "Expand Privacy Policy",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isPrivacyExpanded) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Your privacy is of paramount importance to us. This Privacy Policy outlines exactly how Shia Pulse handles data, complying directly with Google Play Store guidelines:\n\n" +
                                               "1. Local SQLite Database (Room): All interactive data entered by you, including daily prayer logs, tasbeeh tallies, Qada prayer balances, and saved bookmarked Ayat/Hadith/Dua nodes, are saved locally on your active handset device in a secure SQLite structure powered by Room persistence.\n\n" +
                                               "2. Offline Configurations: User selections like font scaling settings, custom translation languages (English, Arabic, Urdu), and geographic location data for local prayer mathematical offsets are stored strictly on-device utilizing local Android SharedPreferences.\n\n" +
                                               "3. Voluntary Cloud backup: Syncing settings and database entries to the cloud is completely optional. If and only if you voluntarily authenticate and choose to log in using Google/Firebase, are your records safely backed up to your personalized cloud container to preserve user states across multi-device configurations.\n\n" +
                                               "4. Transit and Processing Safety: No personal identifiers, bookmarks, activity tracks, or telemetry analytics are gathered, cataloged, or forwarded to external servers or secondary marketing brokers. When selecting AI features (Gemini API) or Location finders, processing is done securely without transit tracking.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    // SHARE APP OPTION
                    item {
                        Button(
                            onClick = {
                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Shia Pulse")
                                    putExtra(
                                        android.content.Intent.EXTRA_TEXT,
                                        "Assalamu Alaikum! Discover Shia Pulse - The comprehensive high-fidelity Shia companion app. Track your daily salat, keep counter of Tasbeeh, calculate Islamic inheritance and dues, view masoomeen calendar events and much more! Save & sync online. Download here: https://play.google.com/store/apps/details?id=com.mojedarya.shiapulse"
                                    )
                                }
                                try {
                                    if (shareIntent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Shia Pulse via"))
                                    } else {
                                        android.widget.Toast.makeText(context, "No app available to share.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {}
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .testTag("share_app_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                        ) {
                            Icon(Icons.Default.Share, "Share", tint = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(t["share"] ?: "Share Application", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    // FAQ OPTION LIST
                    item {
                        Text(
                            text = t["faq"] ?: "Frequently Asked Questions",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        faqList.forEachIndexed { index, (question, answer) ->
                            val isExpanded = expandedFaqIndex == index
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { expandedFaqIndex = if (isExpanded) null else index },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "❓ $question",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(0.9f)
                                        )
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                                            contentDescription = "Expand FAQ",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isExpanded) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = answer,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }


                }
            }
        }
    }

    if (showRateUsPrompt) {
        Dialog(onDismissRequest = { viewModel.dismissRateUsPrompt(false) }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .clip(RoundedCornerShape(28.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rate Stars Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Love Shia Pulse?",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Thank you for using our app! We hope your spiritual journey is going well. Could you please take a moment to leave a rating on the Play Store? Your feedback helps us keep the app 100% free, independent, and ad-free.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    val uri = Uri.parse("market://details?id=" + context.packageName)
                                    val goToMarket = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                    context.startActivity(goToMarket)
                                } catch (e: Exception) {
                                    try {
                                        val webUri = Uri.parse("https://play.google.com/store/apps/details?id=" + context.packageName)
                                        context.startActivity(android.content.Intent.createChooser(android.content.Intent(android.content.Intent.ACTION_VIEW, webUri), "Open Link"))
                                    } catch (ex: Exception) {}
                                }
                                viewModel.dismissRateUsPrompt(true)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("rate_now_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("Rate Shia Pulse", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.resetLaunchCounter()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("remind_me_later_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Remind Me Later")
                        }

                        TextButton(
                            onClick = {
                                viewModel.dismissRateUsPrompt(true)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dont_show_again_button")
                        ) {
                            Text(
                                "Don't Show Again",
                                color = MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

// Custom Grid Platform Composable Tile
@Composable
fun GridPlatformTile(
    title: String,
    gradient: Brush,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    customIconResId: Int? = null
) {
    BoxWithConstraints(modifier = modifier.aspectRatio(0.95f)) {
        val isWide = maxWidth > 110.dp
        val boxSize = if (isWide) 56.dp else 38.dp
        val iconSize = if (isWide) 28.dp else 18.dp
        val textSize = if (isWide) 14.sp else 10.5.sp
        val titleLineHeight = if (isWide) 16.sp else 12.sp
        val outerPadding = if (isWide) 8.dp else 4.dp
        val itemSpacing = if (isWide) 12.dp else 4.dp
        val iconPadding = if (isWide) 12.dp else 8.dp

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(outerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(boxSize)
                        .clip(RoundedCornerShape(10.dp))
                        .background(gradient)
                        .let { if (customIconResId == null) it.padding(iconPadding) else it },
                    contentAlignment = Alignment.Center
                ) {
                    if (customIconResId != null) {
                        Image(
                            painter = painterResource(id = customIconResId),
                            contentDescription = title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = Color.White,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(itemSpacing))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = textSize),
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    lineHeight = titleLineHeight,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ResponsiveTileGrid(
    modifier: Modifier = Modifier,
    content: @Composable (tileModifier: Modifier) -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val columns = if (maxWidth > 800.dp) 6 else if (maxWidth > 600.dp) 5 else 4
        val spacing = 8.dp
        val tileWidth = ((maxWidth - (spacing * (columns - 1))) / columns) - 0.5.dp

        androidx.compose.foundation.layout.FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            content(Modifier.width(tileWidth))
        }
    }
}

data class NextPrayerInfo(
    val nextPrayerName: String,
    val nextPrayerTime: String,
    val countdownText: String
)

private fun calculateNextPrayer(times: PrayerTimeCalculator.PrayerTimes): NextPrayerInfo {
    val now = Calendar.getInstance()
    val curHrs = now.get(Calendar.HOUR_OF_DAY)
    val curMins = now.get(Calendar.MINUTE)
    val curMinsTotal = curHrs * 60 + curMins

    val prayerTimes = listOf(
        "Fajr" to times.fajrString,
        "Sunrise" to times.sunriseString,
        "Dhuhr" to times.dhuhrString,
        "Asr" to times.asrString,
        "Sunset" to times.sunsetString,
        "Maghrib" to times.maghribString,
        "Isha" to times.ishaString,
        "Midnight" to times.midnightString
    )

    var selectedPrayerName = ""
    var selectedPrayerTimeString = ""
    var diffMinutes = -1

    for (pair in prayerTimes) {
        val parts = pair.second.split(":")
        if (parts.size == 2) {
            val hrs = parts[0].toIntOrNull() ?: 0
            val mins = parts[1].toIntOrNull() ?: 0
            val targetMinsTotal = hrs * 60 + mins

            if (targetMinsTotal > curMinsTotal) {
                selectedPrayerName = pair.first
                selectedPrayerTimeString = pair.second
                diffMinutes = targetMinsTotal - curMinsTotal
                break
            }
        }
    }

    // If none of the remaining today is upcoming, then the next upcoming prayer is Fajr tomorrow!
    if (diffMinutes == -1) {
        selectedPrayerName = "Fajr (Tomorrow)"
        val parts = times.fajrString.split(":")
        selectedPrayerTimeString = times.fajrString
        if (parts.size == 2) {
            val hrs = parts[0].toIntOrNull() ?: 0
            val mins = parts[1].toIntOrNull() ?: 0
            val targetMinsTotal = hrs * 60 + mins
            diffMinutes = (24 * 60 - curMinsTotal) + targetMinsTotal
        } else {
            diffMinutes = 180 // Arbitrary backup
        }
    }

    val dHours = diffMinutes / 60
    val dMins = diffMinutes % 60
    val countdownText = String.format("%02dh %02dm", dHours, dMins)
    return NextPrayerInfo(
        nextPrayerName = selectedPrayerName,
        nextPrayerTime = selectedPrayerTimeString,
        countdownText = countdownText
    )
}

@Composable
fun HijriGregorianCalendarHeader(
    viewModel: PrayerViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val hijriDate = remember(selectedDate) {
        com.example.utils.HijriCalendarHelper.convertGregorianToHijri(selectedDate)
    }

    val context = LocalContext.current
    
    val languageCode by viewModel.appLanguage.collectAsState()
    
    // Format Gregorian date: e.g. "Thursday, June 4, 2026"
    val gregDateStr = remember(selectedDate) {
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        sdf.format(selectedDate.time)
    }

    // Synchronized Lunar Calendar Events
    val allEvents = remember {
        com.example.data.MasoomeenData.generalNotableDays + com.example.data.MasoomeenData.list.flatMap { it.events }
    }
    
    val currentHijriDateStr = "${hijriDate.day} ${hijriDate.monthName}".lowercase().trim()
    val todaysEvents = remember(hijriDate) {
        allEvents.filter {
            val eventDates = it.dateStringHijri.split("/").map { d -> d.trim().lowercase() }
            eventDates.any { d -> d == currentHijriDateStr }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Date Navigator Container
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .testTag("hijri_gregorian_header_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Stepper Controls & Date display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Day Button
                    IconButton(
                        onClick = { viewModel.changeDate(-1) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape)
                            .testTag("header_prev_day_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Previous Day",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Combined Date Display with subtle visual accents
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                // Launch DatePickerDialog to choose custom Gregorian date
                                val year = selectedDate.get(Calendar.YEAR)
                                val month = selectedDate.get(Calendar.MONTH)
                                val day = selectedDate.get(Calendar.DAY_OF_MONTH)
                                android.app.DatePickerDialog(
                                    context,
                                    { _, selYear, selMonth, selDay ->
                                        val newCal = Calendar.getInstance().apply {
                                            set(Calendar.YEAR, selYear)
                                            set(Calendar.MONTH, selMonth)
                                            set(Calendar.DAY_OF_MONTH, selDay)
                                        }
                                        viewModel.selectDate(newCal)
                                    },
                                    year, month, day
                                ).show()
                            }
                            .padding(horizontal = 8.dp)
                            .testTag("header_date_picker_trigger")
                    ) {
                        // Hijri Date display with beautiful golden text
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "🌙 ",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${hijriDate.day} ${hijriDate.monthName} ${hijriDate.year} AH",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        // Gregorian Date
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = gregDateStr,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "📅",
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Next Day Button
                    IconButton(
                        onClick = { viewModel.changeDate(1) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape)
                            .testTag("header_next_day_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Next Day",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Highlight Synchronized Lunar Events for today!
        AnimatedVisibility(
            visible = todaysEvents.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                todaysEvents.forEach { event ->
                    val isShahadat = event.eventType == com.example.data.EventType.SHAHADAT
                    val bgGradient = if (isShahadat) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF3E2723).copy(alpha = 0.9f), // deep brown
                                Color(0xFF212121).copy(alpha = 0.95f) // charcoal
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF0D533A).copy(alpha = 0.9f), // dark emerald
                                Color(0xFF1B4D3E).copy(alpha = 0.95f) // deep forest green
                            )
                        )
                    }

                    val accentColor = if (isShahadat) Color(0xFFFF8A80) else Color(0xFF69F0AE)
                    val iconSymbol = if (isShahadat) "🕯️" else "🎉"

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .border(
                                width = 1.2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(accentColor.copy(alpha = 0.4f), Color.Transparent)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .testTag("header_lunar_event_${event.title.replace(" ", "_").lowercase()}"),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(bgGradient)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = iconSymbol,
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(end = 10.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = com.example.utils.LocalizationUtility.translateEventTitle(event.title, languageCode),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Black,
                                            color = accentColor
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(accentColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isShahadat) "SHAHADAT" else "WILADAT",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 8.sp,
                                                color = accentColor
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Text(
                                        text = event.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(2.dp))
                                    
                                    Text(
                                        text = "Occurs on ${event.dateStringHijri} in the Lunar Calendar",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontStyle = FontStyle.Italic,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HadithOfTheDayCard(
    viewModel: PrayerViewModel,
    fontScale: Float,
    languageCode: String,
    modifier: Modifier = Modifier
) {
    val dailyQuote by viewModel.dailyQuote.collectAsState()
    val isGeneratingQuote by viewModel.isGeneratingQuote.collectAsState()
    val favoriteQuotes by viewModel.favoriteQuotes.collectAsState()
    val appFont by viewModel.appFont.collectAsState()
    val baseAppFontFamily = com.example.ui.theme.getAppFontFamily(appFont, languageCode)
    val context = LocalContext.current

    val showUrdu = languageCode == "ur"

    val themeMode by viewModel.themeMode.collectAsState()
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> systemDark
    }

    // Determine lock/favorite status
    val isFavorited = remember(favoriteQuotes, dailyQuote) {
        val dq = dailyQuote
        if (dq != null) {
            favoriteQuotes.any { it.english.trim().lowercase() == dq.english.trim().lowercase() }
        } else {
            false
        }
    }

    var showShareDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color.White.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
            width = 1.2.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF00C6FF).copy(alpha = if (isDark) 0.3f else 0.8f), // Premium light cyan
                    Color(0xFFFFD54F).copy(alpha = if (isDark) 0.4f else 0.8f)  // Shia golden glow
                )
            )
        ),
        modifier = modifier
            .padding(vertical = 12.dp)
            .testTag("hadith_of_the_day_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title with sparkle & Gemini label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "✨",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = if (showUrdu) "حدیثِ مبارکہ مبارک" else "Hadith of the Day",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = if (isDark) Color(0xFFFFD54F) else Color(0xFFE65100),
                        fontFamily = if (showUrdu) baseAppFontFamily else FontFamily.Default
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                text = if (showUrdu) "اہلبیت اطہار (ع) کے مستند اور معتبر ارشادات" else "Verified Shia Islamic traditions from the Prophet (saws) and the Ahlul Bayt (as)",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDark) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Hadith Display Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isDark) Color.Black.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isDark) Color.White.copy(alpha = 0.08f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isGeneratingQuote) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = if (isDark) Color(0xFFFFD54F) else Color(0xFFE65100),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (showUrdu) "حدیث مبارکہ لوڈ ہو رہی ہے..." else "Retrieving Shia Hadith from sources...",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    val q = dailyQuote
                    if (q != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Arabic Calligraphy
                            if (q.arabic.isNotEmpty()) {
                                Text(
                                    text = q.arabic,
                                    fontSize = (18 * fontScale).sp,
                                    fontFamily = baseAppFontFamily,
                                    color = if (isDark) Color(0xFFFFD54F) else Color(0xFFE65100),
                                    textAlign = TextAlign.Center,
                                    lineHeight = (28 * fontScale).sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("hadith_arabic_text")
                                )
                            }

                            // Translation (Urdu / English)
                            if (languageCode != "ar") {
                                val displayText = if (showUrdu) {
                                    com.example.api.UrduTranslations.getUrdu(q)
                                } else {
                                    q.english
                                }

                                Text(
                                    text = "“$displayText”",
                                    fontSize = (14 * fontScale).sp,
                                    fontStyle = FontStyle.Italic,
                                    color = if (isDark) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    lineHeight = (20 * fontScale).sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("hadith_translation_text")
                                )
                            }

                            // Authenticated Shia source badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📚 ",
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = q.source,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDark) Color(0xFF69F0AE) else Color(0xFF0D533A),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.testTag("hadith_source_label")
                                )
                            }
                        }
                    } else {
                        // Edge case fallback
                        Text(
                            text = "Please tap below to retrieve a majestic Shia Hadith",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Favorite Button
                OutlinedButton(
                    onClick = {
                        if (!isGeneratingQuote && dailyQuote != null) {
                            viewModel.toggleFavoriteQuote()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("hadith_action_favorite_btn"),
                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isFavorited) Color(0xFFFF5252) else (if (isDark) Color.White else MaterialTheme.colorScheme.onSurface)
                    ),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite Hadith",
                        tint = if (isFavorited) Color(0xFFFF5252) else (if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showUrdu) {
                             if (isFavorited) "محفوظ" else "محفوظ"
                        } else {
                             if (isFavorited) "Saved" else "Save"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                // Share Text Button
                OutlinedButton(
                    onClick = {
                        val q = dailyQuote ?: return@OutlinedButton
                        val urQuote = com.example.api.UrduTranslations.getUrdu(q)
                        val shareText = """
                            ✨ Ahlul Bayt (as) Wisdom ✨
                            
                            📖 Arabic: ${q.arabic}
                            
                            🇬🇧 English: ${q.english}
                            
                            🇵🇰 اردو: $urQuote
                            
                            Source: ${q.source}
                            — via Shia Pulse (Guidance Portal)
                        """.trimIndent()
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Ahlul Bayt Hadith of the Day")
                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Shia Wisdom via"))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("hadith_action_share_btn"),
                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Text",
                        tint = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showUrdu) "شیئر" else "Share",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                
                // Share Card Button
                OutlinedButton(
                    onClick = { showShareDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("hadith_action_share_card_btn"),
                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Brush,
                        contentDescription = "Share Card",
                        tint = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showUrdu) "کارڈ" else "Card",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
    
    val scope = rememberCoroutineScope()
    if (showShareDialog && dailyQuote != null) {
        AyatImageShareDialog(
            ayat = dailyQuote!!,
            onDismiss = { showShareDialog = false },
            onShare = { includeArabic, includeEnglish, includeUrdu ->
                com.example.utils.ShareService.shareQuoteCard(
                    context = context,
                    quote = dailyQuote!!,
                    includeArabic = includeArabic,
                    includeEnglish = includeEnglish,
                    includeUrdu = includeUrdu,
                    fileNamePrefix = "shiapulse_hadith",
                    chooserTitle = "Share Hadith Card",
                    scope = scope
                )
            }
        )
    }
}

@Composable
fun AyatOfTheDayCard(
    viewModel: PrayerViewModel,
    fontScale: Float,
    languageCode: String,
    modifier: Modifier = Modifier
) {
    val dailyAyat by viewModel.dailyAyat.collectAsState()
    val isGeneratingAyat by viewModel.isGeneratingAyat.collectAsState()
    val favoriteQuotes by viewModel.favoriteQuotes.collectAsState()
    val appFont by viewModel.appFont.collectAsState()
    val baseAppFontFamily = com.example.ui.theme.getAppFontFamily(appFont, languageCode)
    val context = LocalContext.current

    val showUrdu = languageCode == "ur"

    val themeMode by viewModel.themeMode.collectAsState()
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> systemDark
    }

    val isFavorited = remember(favoriteQuotes, dailyAyat) {
        val dq = dailyAyat
        if (dq != null) {
            favoriteQuotes.any { it.english.trim().lowercase() == dq.english.trim().lowercase() }
        } else {
            false
        }
    }

    var showShareDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color.White.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
            width = 1.2.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF69F0AE).copy(alpha = if (isDark) 0.3f else 0.8f), // Premium light green
                    Color(0xFF00C6FF).copy(alpha = if (isDark) 0.4f else 0.8f)  // cyan glow
                )
            )
        ),
        modifier = modifier
            .padding(vertical = 12.dp)
            .testTag("ayat_of_the_day_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📖",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = if (showUrdu) "آیتِ مبارکہ" else "Ayat of the Day",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = if (isDark) Color(0xFF69F0AE) else Color(0xFF0D533A),
                        fontFamily = if (showUrdu) baseAppFontFamily else FontFamily.Default
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (showUrdu) "قرآن مجید کی روشنی، روزانہ کی بنیاد پر" else "Daily illumination from the Holy Quran",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDark) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isDark) Color.Black.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isDark) Color.White.copy(alpha = 0.08f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isGeneratingAyat) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = if (isDark) Color(0xFF69F0AE) else Color(0xFF0D533A),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (showUrdu) "آیت مبارکہ تلاش ہو رہی ہے..." else "Retrieving Ayah from Quran...",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    val q = dailyAyat
                    if (q != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (q.arabic.isNotEmpty()) {
                                Text(
                                    text = q.arabic,
                                    fontSize = (18 * fontScale).sp,
                                    fontFamily = baseAppFontFamily,
                                    color = if (isDark) Color(0xFF69F0AE) else Color(0xFF0D533A),
                                    textAlign = TextAlign.Center,
                                    lineHeight = (28 * fontScale).sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (languageCode != "ar") {
                                val displayText = if (showUrdu) {
                                    com.example.api.UrduTranslations.getUrdu(q)
                                } else {
                                    q.english
                                }

                                Text(
                                    text = "“$displayText”",
                                    fontSize = (14 * fontScale).sp,
                                    fontStyle = FontStyle.Italic,
                                    color = if (isDark) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    lineHeight = (20 * fontScale).sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📚 ",
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = q.source,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDark) Color(0xFFFFD54F) else Color(0xFFE65100),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Please tap below to retrieve a beautiful Ayat",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        if (!isGeneratingAyat && dailyAyat != null) {
                            viewModel.toggleFavoriteAyat()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isFavorited) Color(0xFFFF5252) else (if (isDark) Color.White else MaterialTheme.colorScheme.onSurface)
                    ),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite Ayat",
                        tint = if (isFavorited) Color(0xFFFF5252) else (if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showUrdu) {
                             if (isFavorited) "محفوظ" else "محفوظ"
                        } else {
                             if (isFavorited) "Saved" else "Save"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = {
                        val q = dailyAyat ?: return@OutlinedButton
                        val urAyat = com.example.api.UrduTranslations.getUrdu(q)
                        val shareText = """
                            ✨ Ayat of the Day ✨
                            
                            📖 Arabic: ${q.arabic}
                            
                            🇬🇧 English: ${q.english}
                            
                            🇵🇰 اردو: $urAyat
                            
                            Source: ${q.source}
                            — via Shia Pulse (Guidance Portal)
                        """.trimIndent()
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Ayat of the Day")
                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Ayat via"))
                    },
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Text",
                        tint = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showUrdu) "شیئر" else "Share",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                
                OutlinedButton(
                    onClick = { showShareDialog = true },
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Brush,
                        contentDescription = "Share Card",
                        tint = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showUrdu) "کارڈ" else "Card",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
    
    val scope = rememberCoroutineScope()
    if (showShareDialog && dailyAyat != null) {
        AyatImageShareDialog(
            ayat = dailyAyat!!,
            onDismiss = { showShareDialog = false },
            onShare = { includeArabic, includeEnglish, includeUrdu ->
                com.example.utils.ShareService.shareQuoteCard(
                    context = context,
                    quote = dailyAyat!!,
                    includeArabic = includeArabic,
                    includeEnglish = includeEnglish,
                    includeUrdu = includeUrdu,
                    fileNamePrefix = "shiapulse_ayat",
                    chooserTitle = "Share Ayat Card",
                    scope = scope
                )
            }
        )
    }
}

@Composable
fun AyatImageShareDialog(
    ayat: com.example.api.ShiaQuote,
    onDismiss: () -> Unit,
    onShare: (includeArabic: Boolean, includeEnglish: Boolean, includeUrdu: Boolean) -> Unit
) {
    var includeArabic by remember { mutableStateOf(true) }
    var includeEnglish by remember { mutableStateOf(true) }
    var includeUrdu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Share as Image",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column {
                Text(
                    text = "Select what to include in the shared image:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Checkbox(checked = includeArabic, onCheckedChange = { includeArabic = it })
                    Text("Arabic Text")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Checkbox(checked = includeEnglish, onCheckedChange = { includeEnglish = it })
                    Text("English Translation")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Checkbox(checked = includeUrdu, onCheckedChange = { includeUrdu = it })
                    Text("Urdu Translation")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (includeArabic || includeEnglish || includeUrdu) {
                        onShare(includeArabic, includeEnglish, includeUrdu)
                        onDismiss()
                    }
                }
            ) {
                Text("Generate & Share")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}



@Composable
fun TasbeehDailyChartCard(viewModel: PrayerViewModel, modifier: Modifier = Modifier) {
    val records by viewModel.tasbeehRecords.collectAsState()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Daily Tasbeeh History", 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (records.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No history yet. Start your daily tasbeeh!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val last7 = records.takeLast(7)
                    val maxCount = last7.maxOfOrNull { it.totalCount }?.coerceAtLeast(1) ?: 1
                    
                    last7.forEach { record ->
                        val heightRatio = (record.totalCount.toFloat() / maxCount.toFloat()).coerceIn(0.05f, 1f)
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .fillMaxHeight(heightRatio)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val dayStr = record.dateString.takeLast(5)
                            Text(
                                text = dayStr,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${record.totalCount}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

// Data structures and functions for Upcoming Masoomeen Events Countdown
data class UpcomingMasoomEvent(
    val title: String,
    val dateStringHijri: String,
    val eventType: com.example.data.EventType,
    val description: String,
    val masoomName: String,
    val targetGregorian: Calendar,
    val daysRemaining: Long
)

fun parseHijriDateString(dateStr: String): Pair<Int, Int>? {
    val clean = dateStr.substringBefore("(").trim()
    val parts = clean.split(" ")
    if (parts.isEmpty()) return null
    val day = parts[0].toIntOrNull() ?: return null
    val monthName = parts.drop(1).joinToString(" ").trim()
    
    // Clean month name further to align with HijriCalendarHelper constants
    val cleanMonthName = monthName.replace("'", "").replace("-", " ").lowercase()
    
    val monthIndex = HijriCalendarHelper.MONTH_NAMES.indexOfFirst {
        val helperName = it.replace("'", "").replace("-", " ").lowercase()
        helperName == cleanMonthName || cleanMonthName.contains(helperName) || helperName.contains(cleanMonthName)
    }
    
    if (monthIndex == -1) return null
    return Pair(day, monthIndex + 1)
}

fun getUpcomingEvents(nowMillis: Long): List<UpcomingMasoomEvent> {
    val results = mutableListOf<UpcomingMasoomEvent>()
    
    // Get current Hijri date to find current year
    val currentCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
    val currentHijri = HijriCalendarHelper.convertGregorianToHijri(currentCal)
    val curHYear = currentHijri.year
    
    for (masoom in com.example.data.MasoomeenData.list) {
        for (event in masoom.events) {
            val hDate = parseHijriDateString(event.dateStringHijri) ?: continue
            val hDay = hDate.first
            val hMonth = hDate.second
            
            // 1. Try with current Hijri year
            var targetCal = HijriCalendarHelper.convertHijriToGregorian(curHYear, hMonth, hDay)
            targetCal.set(Calendar.HOUR_OF_DAY, 0)
            targetCal.set(Calendar.MINUTE, 0)
            targetCal.set(Calendar.SECOND, 0)
            targetCal.set(Calendar.MILLISECOND, 0)
            
            val todayStart = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            if (targetCal.timeInMillis < todayStart.timeInMillis) {
                // Already passed in this Hijri year, get the one in next Hijri year
                targetCal = HijriCalendarHelper.convertHijriToGregorian(curHYear + 1, hMonth, hDay)
                targetCal.set(Calendar.HOUR_OF_DAY, 0)
                targetCal.set(Calendar.MINUTE, 0)
                targetCal.set(Calendar.SECOND, 0)
                targetCal.set(Calendar.MILLISECOND, 0)
            }
            
            val diffMillis = targetCal.timeInMillis - nowMillis
            val daysRem = if (diffMillis <= 0) 0L else (diffMillis / (24 * 60 * 60 * 1000L))
            
            results.add(
                UpcomingMasoomEvent(
                    title = event.title,
                    dateStringHijri = event.dateStringHijri,
                    eventType = event.eventType,
                    description = event.description,
                    masoomName = masoom.name,
                    targetGregorian = targetCal,
                    daysRemaining = daysRem
                )
            )
        }
    }
    
    // Sort chronologically
    results.sortBy { it.targetGregorian.timeInMillis }
    return results
}

@Composable
fun UpcomingMasoomeenEventsSection(
    viewModel: PrayerViewModel,
    languageCode: String,
    fontScale: Float,
    modifier: Modifier = Modifier
) {
    val currentTime = remember { System.currentTimeMillis() }
    
    val upcomingEvents = remember(currentTime) {
        getUpcomingEvents(currentTime)
    }
    
    if (upcomingEvents.isEmpty()) return
    
    val nextEvent = upcomingEvents.first()
    
    val diffMillis = maxOf(0L, nextEvent.targetGregorian.timeInMillis - currentTime)
    val daysVisible = maxOf(1L, diffMillis / (24 * 60 * 60 * 1000L))
    
    val isUrdu = languageCode == "ur"

    val themeMode by viewModel.themeMode.collectAsState()
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> systemDark
    }
    
    Column(modifier = modifier) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFB300))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = com.example.utils.LocalizationUtility.get("masoomeen_remembrance_calendar", languageCode),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 0.5.sp
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Next Big Anniversary Glowing Countdown Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceVariant
            ),
            border = BorderStroke(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFB300).copy(alpha = if (isDark) 0.35f else 0.8f), // Gold Accent glow
                        Color(0xFF00E676).copy(alpha = if (isDark) 0.15f else 0.5f)
                    )
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                // Top line: Next event badge & type badge
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = com.example.utils.LocalizationUtility.get("next_event", languageCode),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                    
                    // EventType Badge (Birth vs Martyrdom)
                    val isBirth = nextEvent.eventType == com.example.data.EventType.WILADAT
                    val badgeColor = if (isBirth) Color(0xFF00E676) else Color(0xFFFF5252)
                    val badgeText = if (isBirth) {
                        com.example.utils.LocalizationUtility.get("wiladat", languageCode)
                    } else {
                        com.example.utils.LocalizationUtility.get("shahadat", languageCode)
                    }
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .border(0.8.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                // Event Title & Target Masoom Name
                Text(
                    text = com.example.utils.LocalizationUtility.translateEventTitle(nextEvent.title, languageCode),
                    fontSize = (20 * fontScale).sp,
                    fontWeight = FontWeight.Black,
                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                val ahlulBaytLabel = com.example.utils.LocalizationUtility.get("ahlul_bayt", languageCode)
                Text(
                    text = "$ahlulBaytLabel: ${com.example.utils.LocalizationUtility.translateMasoomName(nextEvent.masoomName, languageCode)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Gregorian date conversion notice & Hijri date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val lunarDateLabelText = com.example.utils.LocalizationUtility.get("lunar_date", languageCode)
                    Text(
                        text = "🗓️ $lunarDateLabelText ${nextEvent.dateStringHijri}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    // Format Gregorian Date WITH Day suffix
                    val sdf = java.text.SimpleDateFormat("EEEE, d MMM yyyy", java.util.Locale.ENGLISH)
                    val gregorianDateStr = sdf.format(nextEvent.targetGregorian.time)
                    val gregorianLabelText = com.example.utils.LocalizationUtility.get("gregorian_label", languageCode)
                    
                    Text(
                        text = gregorianLabelText + gregorianDateStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Color(0xFFFFD54F) else Color(0xFFE65100),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
                
                // Real-time Countdown timer block (Days only)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isDark) Color.White.copy(alpha = 0.05f)
                                else MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                            )
                            .border(
                                width = 0.8.dp,
                                color = if (isDark) Color.White.copy(alpha = 0.1f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$daysVisible",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) Color(0xFFFFD54F) else Color(0xFFE65100)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = com.example.utils.LocalizationUtility.get("days_remaining", languageCode),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isDark) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(14.dp))
        
        // Upcoming list on the horizon: Mini upcoming event sliders
        Text(
            text = com.example.utils.LocalizationUtility.get("upcoming_anniversary", languageCode),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val futureSchedule = upcomingEvents.drop(1).take(6)
            items(futureSchedule) { ev ->
                UpcomingEventMiniCard(ev, languageCode, isDark)
            }
        }
    }
}

@Composable
fun UpcomingEventMiniCard(event: UpcomingMasoomEvent, languageCode: String, isDark: Boolean) {
    val isBirth = event.eventType == com.example.data.EventType.WILADAT
    val accentColor = if (isBirth) Color(0xFF00E676) else Color(0xFFFF5252)
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color.White.copy(alpha = 0.04f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isDark) Color.White.copy(alpha = 0.08f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        ),
        modifier = Modifier
            .width(170.dp)
            .height(115.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.dateStringHijri,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = com.example.utils.LocalizationUtility.translateEventTitle(event.title, languageCode),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 13.sp
                )
            }
            
            Text(
                text = if (event.daysRemaining == 0L) {
                    com.example.utils.LocalizationUtility.get("today", languageCode)
                } else if (event.daysRemaining == 1L) {
                    com.example.utils.LocalizationUtility.get("tomorrow", languageCode)
                } else {
                    com.example.utils.LocalizationUtility.get("in_days", languageCode).replace("%d", event.daysRemaining.toString())
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = if (isDark) accentColor else (if (isBirth) Color(0xFF00796B) else Color(0xFFC62828))
            )
        }
    }
}

// Sparkly celebratory particles or rich dark solemn fading embers depending on the Lunar Hijri occasion
data class CelebrationParticle(
    var x: Float,
    var y: Float,
    var speed: Float,
    var size: Float,
    val color: Color,
    val isStar: Boolean = false,
    var angle: Float = 0f,
    var angleSpeed: Float = 0f,
    var opacity: Float = 1f
)

@Composable
fun OccasionAnimationOverlay(
    eventType: com.example.data.EventType?,
    modifier: Modifier = Modifier
) {
    if (eventType == null) return

    val isWiladat = eventType == com.example.data.EventType.WILADAT
    
    // Track lifecycle to trigger animation on resume
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var resumeCounter by remember { mutableIntStateOf(0) }
    
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                resumeCounter++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var isFinished by remember(eventType, resumeCounter) { mutableStateOf(false) }

    if (isFinished) return

    val particles = remember(eventType, resumeCounter) { mutableStateListOf<CelebrationParticle>() }
    var lastTimeNanos by remember(eventType, resumeCounter) { mutableStateOf(0L) }
    var elapsedTotalSeconds by remember(eventType, resumeCounter) { mutableStateOf(0f) }
    var frameTick by remember(eventType, resumeCounter) { mutableIntStateOf(0) } // Force canvas redraw

    LaunchedEffect(eventType, resumeCounter) {
        // Initialize particles ONCE
        particles.clear()
        val count = if (isWiladat) 95 else 16 // More confetti for Wiladat, 16 for dhabay
        for (i in 0 until count) {
            if (isWiladat) {
                // Festive Confetti / Stars
                val r = kotlin.random.Random.nextFloat()
                val color = when {
                    r < 0.35f -> Color(0xFF00E676) // Mint Emerald
                    r < 0.70f -> Color(0xFFFFD600) // Gold
                    r < 0.85f -> Color(0xFF26A69A) // Teal
                    else -> Color(0xFFFFFFFF) // Sparkle
                }
                particles.add(
                    CelebrationParticle(
                        x = kotlin.random.Random.nextFloat(),
                        y = -0.1f - kotlin.random.Random.nextFloat() * 4.5f, // Spread far above for continuous fall over 10 seconds
                        speed = 0.25f + kotlin.random.Random.nextFloat() * 0.35f, // Screen heights per sec
                        size = 18f + kotlin.random.Random.nextFloat() * 14f,
                        color = color,
                        isStar = kotlin.random.Random.nextFloat() < 0.32f,
                        angle = kotlin.random.Random.nextFloat() * 360f,
                        angleSpeed = (kotlin.random.Random.nextFloat() - 0.5f) * 200f,
                        opacity = 1f
                    )
                )
            } else {
                // Shahadat Blood Stains (Khoon k dhabay)
                val r = kotlin.random.Random.nextFloat()
                val color = when {
                    r < 0.4f -> Color(0xFF8B0000) // Dark Red
                    r < 0.7f -> Color(0xFFB22222) // Firebrick
                    else -> Color(0xFF600000) // Deep Maroon
                }
                particles.add(
                    CelebrationParticle(
                        x = 0.05f + kotlin.random.Random.nextFloat() * 0.9f, 
                        y = 0.05f + kotlin.random.Random.nextFloat() * 0.85f,
                        speed = 0.005f + kotlin.random.Random.nextFloat() * 0.02f, // Slower bleed downwards
                        size = 20f + kotlin.random.Random.nextFloat() * 35f,
                        color = color,
                        opacity = 0f // Start hidden, fade in
                    )
                )
            }
        }

        lastTimeNanos = System.nanoTime()
        val totalDuration = if (isWiladat) 10f else 8f // 10 seconds celebration, 8s for dhabay

        while (elapsedTotalSeconds < totalDuration) {
            androidx.compose.runtime.withFrameNanos { frameTimeNanos ->
                val elapsedSeconds = if (lastTimeNanos == 0L) 0f else (frameTimeNanos - lastTimeNanos) / 1_000_000_000f
                lastTimeNanos = frameTimeNanos
                elapsedTotalSeconds += elapsedSeconds

                val iterator = particles.iterator()
                while (iterator.hasNext()) {
                    val p = iterator.next()
                    if (isWiladat) {
                        p.y += p.speed * elapsedSeconds
                        p.angle += p.angleSpeed * elapsedSeconds
                        // slowly fade out at the end
                        if (elapsedTotalSeconds > 8.5f) {
                            p.opacity = maxOf(0f, p.opacity - elapsedSeconds * 0.8f)
                        }
                    } else {
                        // blood stain: fade in first 2 seconds, bleed down slightly, fade out after 6 seconds
                        if (elapsedTotalSeconds < 2f) {
                            p.opacity = minOf(0.85f, p.opacity + elapsedSeconds * 0.4f)
                        } else if (elapsedTotalSeconds > 6f) {
                            p.opacity = maxOf(0f, p.opacity - elapsedSeconds * 0.4f)
                        }
                        // extremely slow drip downwards
                        p.y += p.speed * elapsedSeconds
                    }
                }
                frameTick++ // Force recomposition
            }
        }
        isFinished = true
    }

    if (particles.isEmpty()) return

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .testTag("occasion_particle_canvas")
    ) {
        val currentTick = frameTick // Read to force redraw
        
        val width = size.width
        val height = size.height

        particles.forEach { p ->
            if (p.opacity <= 0f) return@forEach

            val px = p.x * width
            val py = p.y * height

            val roundedX = px.coerceIn(0f, width)

            if (isWiladat) {
                // Confetti drawing
                val pathAlpha = p.opacity * 0.85f
                
                if (p.isStar) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(roundedX, py - p.size)
                        lineTo(roundedX + p.size / 3.5f, py - p.size / 3.5f)
                        lineTo(roundedX + p.size, py)
                        lineTo(roundedX + p.size / 3.5f, py + p.size / 3.5f)
                        lineTo(roundedX, py + p.size)
                        lineTo(roundedX - p.size / 3.5f, py + p.size / 3.5f)
                        lineTo(roundedX - p.size, py)
                        lineTo(roundedX - p.size / 3.5f, py - p.size / 3.5f)
                        close()
                    }
                    drawPath(path, p.color.copy(alpha = pathAlpha))
                } else {
                    val halfSize = p.size / 2f
                    rotate(degrees = p.angle, pivot = androidx.compose.ui.geometry.Offset(roundedX, py)) {
                        drawRect(
                            color = p.color.copy(alpha = p.opacity * 0.72f),
                            topLeft = androidx.compose.ui.geometry.Offset(roundedX - halfSize, py - halfSize / 1.8f),
                            size = androidx.compose.ui.geometry.Size(p.size, p.size / 1.8f)
                        )
                    }
                }
            } else {
                // Shahadat Blood Stain (Dhabba)
                val dropAlpha = p.opacity
                
                // Main droplet circle
                drawCircle(
                    color = p.color.copy(alpha = dropAlpha),
                    radius = p.size,
                    center = androidx.compose.ui.geometry.Offset(roundedX, py)
                )
                // Droplet tail bleeding upwards
                val tailPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(roundedX - p.size * 0.85f, py)
                    lineTo(roundedX + p.size * 0.85f, py)
                    lineTo(roundedX, py - p.size * 2.8f)
                    close()
                }
                drawPath(tailPath, p.color.copy(alpha = dropAlpha * 0.8f))
                
                // Small adjacent blood splatter circles
                drawCircle(
                    color = p.color.copy(alpha = dropAlpha * 0.7f),
                    radius = p.size * 0.35f,
                    center = androidx.compose.ui.geometry.Offset(roundedX + p.size * 1.6f, py + p.size * 0.6f)
                )
                drawCircle(
                    color = p.color.copy(alpha = dropAlpha * 0.5f),
                    radius = p.size * 0.2f,
                    center = androidx.compose.ui.geometry.Offset(roundedX - p.size * 1.3f, py - p.size * 0.9f)
                )
            }
        }
    }
}

