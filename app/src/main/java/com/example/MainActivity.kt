package com.example

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.ui.*
import com.example.ui.theme.ShiaPulseTheme
import com.example.receiver.MasoomEventScheduler
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var rotationVectorSensor: Sensor? = null
    private var orientationSensor: Sensor? = null

    // Azimuth state holding current direction (0=North, 90=East...)
    private val azimuthState = MutableStateFlow(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        var keepSplashScreen = true
        lifecycleScope.launch {
            delay(50L) // Hide standard splash quickly
            keepSplashScreen = false
        }
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Setup Sensors
        try {
            val systemService = getSystemService(Context.SENSOR_SERVICE)
            if (systemService != null) {
                sensorManager = systemService as SensorManager
                rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
                if (rotationVectorSensor == null) {
                    // Fallback to old orientation sensor if gyro/rotation is not present
                    orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)
                }
            }
        } catch (t: Throwable) {
            android.util.Log.e("MainActivity", "Failed to setup sensorManager on this hardware.", t)
        }

        // Schedule daily check for 14 Masoomeen lunar calendar events
        MasoomEventScheduler.scheduleDailyCheck(this)

        setContent {
            val viewModel: PrayerViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val isDark = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> systemDark
            }

            val autoOccasionTheme by viewModel.autoOccasionTheme.collectAsState()
            val themeOverride by viewModel.themeOverride.collectAsState()

            // Calculate active occasion event type based on automatic detection or manual test overrides
            val activeOccasionType = remember(autoOccasionTheme, themeOverride) {
                when (themeOverride) {
                    "wiladat" -> com.example.data.EventType.WILADAT
                    "shahadat" -> com.example.data.EventType.SHAHADAT
                    else -> {
                        if (autoOccasionTheme) {
                            val calendar = java.util.Calendar.getInstance()
                            val year = calendar.get(java.util.Calendar.YEAR)
                            val month = calendar.get(java.util.Calendar.MONTH) + 1
                            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

                            val hijriDate = com.example.utils.HijriCalendarHelper.convertGregorianToHijri(year, month, day)
                            val targetDate = "${hijriDate.day} ${hijriDate.monthName}".lowercase()

                            val allEvents = com.example.data.MasoomeenData.generalNotableDays + com.example.data.MasoomeenData.list.flatMap { it.events }
                            val todaysEvents = allEvents.filter { 
                                val eventDates = it.dateStringHijri.split("/").map { d -> d.trim().lowercase() }
                                eventDates.any { d -> d == targetDate }
                            }
                            when {
                                todaysEvents.any { it.eventType == com.example.data.EventType.SHAHADAT } -> com.example.data.EventType.SHAHADAT
                                todaysEvents.any { it.eventType == com.example.data.EventType.WILADAT } -> com.example.data.EventType.WILADAT
                                else -> null
                            }
                        } else {
                            null
                        }
                    }
                }
            }

            val appFontName by viewModel.appFont.collectAsState()
            val languageCode by viewModel.appLanguage.collectAsState()
            
            val activeFontFamily = remember(appFontName, languageCode) {
                com.example.ui.theme.getAppFontFamily(appFontName, languageCode)
            }

            val activeLayoutDirection = remember(languageCode) {
                if (languageCode == "ar" || languageCode == "ur") {
                    androidx.compose.ui.unit.LayoutDirection.Rtl
                } else {
                    androidx.compose.ui.unit.LayoutDirection.Ltr
                }
            }

            var showComposeSplash by remember { mutableStateOf(true) }

            ShiaPulseTheme(
                darkTheme = isDark,
                eventType = activeOccasionType,
                appFontFamily = activeFontFamily,
                layoutDirection = activeLayoutDirection
            ) {
                val azimuth by azimuthState.collectAsState()

                if (showComposeSplash) {
                    AnimatedSplashScreen(onSplashFinish = { showComposeSplash = false })
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        DashboardScreen(
                            viewModel = viewModel,
                            azimuth = azimuth,
                            eventType = activeOccasionType,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!::sensorManager.isInitialized) return
        try {
            // Register orientation sensors
            rotationVectorSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            } ?: orientationSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        } catch (t: Throwable) {
            android.util.Log.e("MainActivity", "Error registering sensors", t)
        }
    }

    override fun onPause() {
        super.onPause()
        if (!::sensorManager.isInitialized) return
        try {
            // Unregister to save battery
            sensorManager.unregisterListener(this)
        } catch (t: Throwable) {
            android.util.Log.e("MainActivity", "Error unregistering sensors", t)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        try {
            val sensorType = event.sensor?.type ?: return
            if (sensorType == Sensor.TYPE_ROTATION_VECTOR) {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val orientationValues = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientationValues)
                val azimuthRad = orientationValues[0]
                val azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
                azimuthState.value = (azimuthDeg + 360f) % 360f
            } else if (sensorType == Sensor.TYPE_ORIENTATION) {
                // Legacy/Virtual fallback
                azimuthState.value = event.values[0]
            }
        } catch (t: Throwable) {
            android.util.Log.e("MainActivity", "Error handling sensor changed", t)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for current compass direction calculations
    }
}
