package com.example.ui

import android.view.SoundEffectConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class BeadStyleObj(val base: Color, val high: Color, val shadow: Color)

val tasbeehBeadStyles = listOf(
    BeadStyleObj(Color(0xFFE0E0E0), Color.White, Color(0xFF757575)), // Silver
    BeadStyleObj(Color(0xFF303030), Color(0xFF757575), Color.Black), // Onyx
    BeadStyleObj(Color(0xFF4CAF50), Color(0xFFA5D6A7), Color(0xFF1B5E20)), // Emerald
    BeadStyleObj(Color(0xFFFFB300), Color(0xFFFFE082), Color(0xFFFF6F00)), // Amber
    BeadStyleObj(Color(0xFF6D4C41), Color(0xFFA1887F), Color(0xFF3E2723)) // Wood
)

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRealisticBead(
    center: Offset, 
    radius: Float, 
    style: BeadStyleObj
) {
    // Drop shadow
    drawCircle(
        color = Color.Black.copy(alpha = 0.3f),
        radius = radius + 2.dp.toPx(),
        center = center + Offset(0f, 4.dp.toPx())
    )
    
    // Main body gradient
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(style.base, style.shadow),
            center = center,
            radius = radius * 1.2f
        ),
        radius = radius,
        center = center
    )
    
    // Specular highlight
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(style.high.copy(alpha = 0.9f), Color.Transparent),
            center = center - Offset(radius * 0.35f, radius * 0.35f),
            radius = radius * 0.7f
        ),
        radius = radius,
        center = center
    )
    
    // Inner thread hole
    drawCircle(
        color = Color.Black.copy(alpha = 0.6f),
        radius = radius * 0.15f,
        center = center
    )
}

@Composable
fun TasbeehScreen(
    viewModel: PrayerViewModel,
    modifier: Modifier = Modifier
) {
    val selectedTasbeeh by viewModel.selectedTasbeeh.collectAsState()
    val tasbeehCount by viewModel.tasbeehCount.collectAsState()
    val tasbeehCycle by viewModel.tasbeehCycle.collectAsState()
    val records by viewModel.tasbeehRecords.collectAsState()
    val currentLang by viewModel.appLanguage.collectAsState()
    val globalAppFont by viewModel.appFont.collectAsState()
    val baseAppFontFamily = com.example.ui.theme.getAppFontFamily(globalAppFont, currentLang)

    val context = LocalContext.current
    val view = LocalView.current
    val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
    // Create ToneGenerator for reliable sound effect
    val toneGenerator = remember { 
        try {
            android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 100) 
        } catch (e: Exception) {
            null
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            toneGenerator?.release()
        }
    }

    val sharedPrefs = remember { context.getSharedPreferences("tasbeeh_prefs", android.content.Context.MODE_PRIVATE) }
    var dailyGoal by remember { mutableIntStateOf(sharedPrefs.getInt("daily_goal", 33)) }
    var showGoalDialog by remember { mutableStateOf(false) }

    var soundEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("sound_enabled", true)) }
    var selectedBeadStyle by remember { mutableIntStateOf(sharedPrefs.getInt("bead_style", 0)) }

    val todayDateStr = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()) }
    val todayCount = records.find { it.dateString == todayDateStr }?.totalCount ?: 0

    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    if (showGoalDialog) {
        var tempGoal by remember { mutableStateOf(dailyGoal.toString()) }
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Set Goal", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = tempGoal,
                    onValueChange = { tempGoal = it },
                    label = { Text("Goal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    tempGoal.toIntOrNull()?.let {
                        val validGoal = it.coerceAtLeast(1)
                        dailyGoal = validGoal
                        sharedPrefs.edit().putInt("daily_goal", validGoal).apply()
                    }
                    showGoalDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) { Text("Cancel") }
            }
        )
    }

    val tasbeehPhraseArabic = remember(selectedTasbeeh, tasbeehCycle) {
        if (selectedTasbeeh == "Tasbih Lady Fatima (sa)") {
            when (tasbeehCycle) {
                0 -> "اللهُ أَكْبَرُ"
                1 -> "الْحَمْدُ لِلَّهِ"
                2 -> "سُبْحَانَ اللَّهِ"
                else -> "تَمَّتْ"
            }
        } else {
            when (selectedTasbeeh) {
                "Salawat (100x)" -> "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَآلِ مُحَمَّدٍ"
                "Istighfar (100x)" -> "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ"
                else -> "ذِكْر"
            }
        }
    }

    val tasbeehPhraseEnglish = remember(selectedTasbeeh, tasbeehCycle) {
        if (selectedTasbeeh == "Tasbih Lady Fatima (sa)") {
            when (tasbeehCycle) {
                0 -> "Allahu Akbar\nGod is Great"
                1 -> "Alhamdulillah\nPraise be to God"
                2 -> "SubhanAllah\nGlory be to God"
                else -> "Completed!"
            }
        } else {
            when (selectedTasbeeh) {
                "Salawat (100x)" -> "Salawat\nO Allah, send blessings upon Muhammad and his progeny"
                "Istighfar (100x)" -> "Istighfar\nI seek forgiveness from God and turn to Him"
                else -> "Dhikir\nFree remembrance"
            }
        }
    }

    val targetGoal = remember(selectedTasbeeh, tasbeehCycle, dailyGoal) {
        if (selectedTasbeeh == "Tasbih Lady Fatima (sa)") {
            when (tasbeehCycle) {
                0 -> 34
                1 -> 33
                2 -> 33
                else -> 100
            }
        } else if (selectedTasbeeh == "Free Mode") {
            dailyGoal
        } else {
            100
        }
    }

    val loopCount = if (targetGoal > 0) (tasbeehCount / targetGoal) + 1 else 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.resetTasbeeh() }) {
                Icon(Icons.Default.Refresh, "Reset")
            }
            Text(
                text = "Loop $loopCount",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            IconButton(onClick = {
                soundEnabled = !soundEnabled
                sharedPrefs.edit().putBoolean("sound_enabled", soundEnabled).apply()
            }) {
                Icon(
                    imageVector = if (soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    contentDescription = "Toggle Sound"
                )
            }
        }

        // Count Display
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = String.format("%02d", tasbeehCount),
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F9D58) // Green count as in screenshot
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.clickable { showGoalDialog = true }.padding(8.dp)
            ) {
                Text(
                    text = "/ $targetGoal",
                    fontSize = 24.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.Edit, "Edit Goal", tint = Color.Gray, modifier = Modifier.size(18.dp))
            }
            
            Spacer(modifier = Modifier.height(40.dp))

            // Interactive Bead String Area
            val currentStyle = tasbeehBeadStyles.getOrElse(selectedBeadStyle) { tasbeehBeadStyles[0] }
            val beadPositionAnim = remember { androidx.compose.animation.core.Animatable(1f) }
            
            fun incrementAndAnimate() {
                // Haptic and Sound
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (soundEnabled) {
                    toneGenerator?.startTone(android.media.ToneGenerator.TONE_CDMA_PRESSHOLDKEY_LITE, 40)
                }
                viewModel.incrementTasbeeh { }
                // Smooth rapid animation
                scope.launch {
                    beadPositionAnim.snapTo(1f)
                    beadPositionAnim.animateTo(
                        0f, 
                        animationSpec = tween(150, easing = androidx.compose.animation.core.FastOutLinearInEasing)
                    )
                    beadPositionAnim.snapTo(1f)
                }
            }

            fun decrementWithSound() {
                if (tasbeehCount > 0) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (soundEnabled) {
                        toneGenerator?.startTone(android.media.ToneGenerator.TONE_PROP_BEEP2, 35)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = { /* Swipe ended */ }
                        ) { change, dragAmount ->
                            change.consume()
                            if (dragAmount.x < -20) {
                                // Swipe left
                                decrementWithSound()
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { incrementAndAnimate() }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    
                    // Draw Thread
                    drawLine(
                        color = Color.DarkGray,
                        start = Offset(0f, h/2),
                        end = Offset(w, h/2),
                        strokeWidth = 2.dp.toPx()
                    )
                    
                    val beadRadius = 24.dp.toPx()
                    
                    // Left piled beads
                    for (i in 0..3) {
                        drawRealisticBead(Offset(20.dp.toPx() + i * (beadRadius * 1.6f), h/2), beadRadius, currentStyle)
                    }
                    
                    // Right piled beads
                    for (i in 0..2) {
                        drawRealisticBead(Offset(w - 20.dp.toPx() - i * (beadRadius * 1.6f), h/2), beadRadius, currentStyle)
                    }
                    
                    // Sliding bead
                    val startX = w - 20.dp.toPx() - 3 * (beadRadius * 1.6f)
                    val endX = 20.dp.toPx() + 4 * (beadRadius * 1.6f)
                    val currentX = endX + (startX - endX) * beadPositionAnim.value
                    
                    if (beadPositionAnim.value < 1f || beadPositionAnim.targetValue < 1f) {
                        drawRealisticBead(Offset(currentX, h/2), beadRadius, currentStyle)
                    }
                }
            }

            Text(
                text = "Tap anywhere to begin\n(Haptic & Sound on tap)",
                textAlign = TextAlign.Center,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        // Bottom Menu / Selection Area
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                // Drag Handle
                Box(
                    modifier = Modifier.width(40.dp).height(4.dp).clip(CircleShape).background(Color.LightGray).align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Current Dhikr", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    TextButton(onClick = { /* Could open full list dialog */ }) {
                        Text("Presets", color = Color(0xFF0F9D58))
                    }
                }
                
                // Horizontal scrolling dhikr presets
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(viewModel.tasbeehPresets.size) { index ->
                        val preset = viewModel.tasbeehPresets[index]
                        val isSelected = selectedTasbeeh == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectTasbeeh(preset) },
                            label = { Text(preset.replace(" (sa)", "")) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0F9D58).copy(alpha = 0.1f),
                                selectedLabelColor = Color(0xFF0F9D58)
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = tasbeehPhraseArabic,
                            fontSize = 22.sp,
                            fontFamily = baseAppFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = tasbeehPhraseEnglish.split("\n")[0], // Just transcription
                            fontSize = 16.sp,
                            color = Color(0xFF0F9D58),
                            textAlign = TextAlign.End
                        )
                        Text(
                            text = tasbeehPhraseEnglish.split("\n").getOrElse(1) { "" },
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.End
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Bead Styles
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val styles = listOf("Silver", "Onyx", "Emerald", "Amber", "Wood")
                    items(styles.size) { index ->
                        val styleObj = tasbeehBeadStyles.getOrElse(index) { tasbeehBeadStyles[0] }
                        
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .clickable { 
                                    selectedBeadStyle = index
                                    sharedPrefs.edit().putInt("bead_style", index).apply()
                                }
                                .border(
                                    width = if (selectedBeadStyle == index) 3.dp else 0.dp,
                                    color = if (selectedBeadStyle == index) Color(0xFF0F9D58) else Color.Transparent,
                                    shape = CircleShape
                                )
                                .padding(4.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawRealisticBead(Offset(size.width/2f, size.height/2f), size.width/2f, styleObj)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

