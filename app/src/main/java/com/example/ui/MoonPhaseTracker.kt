package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.HijriCalendarHelper
import kotlin.math.abs

@Composable
fun MoonPhaseTracker(
    hijriDay: Int,
    hijriMonth: String,
    hijriYear: Int,
    fontScale: Float,
    languageCode: String,
    modifier: Modifier = Modifier
) {
    var showEvents by remember { mutableStateOf(false) }

    // Resolve moon phase names and spiritual context
    val phaseInfo = remember(hijriDay, languageCode) {
        getMoonPhaseInfo(hijriDay, languageCode)
    }

    // Load Shia dates in the current month
    val importantDates = remember(hijriMonth, languageCode) {
        getShiaDatesForMonth(hijriMonth, languageCode)
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("moon_phase_tracker_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Title block
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🌙",
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = com.example.utils.LocalizationUtility.get("lunar_phase_tracker", languageCode),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = com.example.utils.LocalizationUtility.get("lunar_phase_tracker_sub", languageCode),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Small indicator showing age out of 30
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$hijriDay / 30",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Split Row (Glow Moon on Left, Detailed Text and Context on Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Elegant Canvas Moon
                MoonPhaseCanvas(
                    day = hijriDay,
                    modifier = Modifier
                        .size(84.dp)
                        .testTag("moon_phase_canvas")
                )

                Spacer(modifier = Modifier.width(18.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = phaseInfo.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = (18 * fontScale).sp
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = phaseInfo.translatedName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = (14 * fontScale).sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = phaseInfo.significance,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = (12 * fontScale).sp,
                        lineHeight = (16 * fontScale).sp
                    )
                }
            }

            // Important Shia dates block
            if (importantDates.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showEvents = !showEvents }
                        .padding(vertical = 4.dp, horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Shia Highlights",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (languageCode) {
                                "ur" -> "${hijriMonth} کی شیعہ مناسبتیں"
                                "ar" -> "مناسبات شهر ${hijriMonth}"
                                else -> "Important Dates in $hijriMonth"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        imageVector = if (showEvents) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand calendar timeline",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                AnimatedVisibility(
                    visible = showEvents,
                    enter = expandVertically(animationSpec = tween(300)),
                    exit = shrinkVertically(animationSpec = tween(300))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        importantDates.forEach { event ->
                            val isPast = hijriDay > event.day
                            val isToday = hijriDay == event.day

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when {
                                            isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .border(
                                        width = if (isToday) 1.dp else 0.dp,
                                        color = if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(vertical = 8.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Day circle indicator
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isToday -> MaterialTheme.colorScheme.primary
                                                isPast -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                                else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = event.day.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = when {
                                            isToday -> MaterialTheme.colorScheme.onPrimary
                                            isPast -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            else -> MaterialTheme.colorScheme.secondary
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = event.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isPast) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f) else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (event.subText.isNotEmpty()) {
                                        Text(
                                            text = event.subText,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isPast) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.40f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                if (isToday) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFE53935))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = when (languageCode) {
                                                "ur" -> "آج"
                                                "ar" -> "اليوم"
                                                else -> "TODAY"
                                            },
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
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
}

@Composable
fun MoonPhaseCanvas(
    day: Int,
    modifier: Modifier = Modifier
) {
    var lastDay by remember { mutableStateOf(day) }

    val animatedDay by animateFloatAsState(
        targetValue = day.toFloat(),
        animationSpec = if (abs(day - lastDay) > 3) {
            snap()
        } else {
            tween(durationMillis = 500, easing = LinearOutSlowInEasing)
        },
        label = "MoonPhaseAnimation",
        finishedListener = { lastDay = day }
    )

    LaunchedEffect(day) {
        if (abs(day - lastDay) > 3) {
            lastDay = day
        }
    }

    val targetGlowColor = when {
        day == 15 -> Color(0xFFFFE082).copy(alpha = 0.4f)
        day in listOf(1, 30) -> Color(0xFFFFEA00).copy(alpha = 0.0f)
        else -> Color(0xFFFFEA00).copy(alpha = 0.15f)
    }

    val ringGlowColor by animateColorAsState(
        targetValue = targetGlowColor,
        animationSpec = if (abs(day - lastDay) > 3) {
            snap()
        } else {
            tween(durationMillis = 500, easing = LinearOutSlowInEasing)
        },
        label = "MoonGlowAnimation"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val minSize = minOf(width, height)
        val radius = minSize / 2f
        val center = Offset(width / 2f, height / 2f)

        // 1. Draw Space Sphere Background (deep starry indigo background)
        drawCircle(
            color = Color(0xFF0F172A),
            radius = radius,
            center = center
        )

        // 2. Draw lunar craters on the unlit base (delicate slate background)
        // This gives a nice cratered tactile sphere aesthetic even on the dark side of the moon
        val baseCraterColor = Color(0xFF1E293B)
        drawCircle(
            color = baseCraterColor,
            radius = radius * 0.20f,
            center = Offset(center.x - radius * 0.35f, center.y - radius * 0.3f)
        )
        drawCircle(
            color = baseCraterColor,
            radius = radius * 0.14f,
            center = Offset(center.x + radius * 0.4f, center.y + radius * 0.35f)
        )
        drawCircle(
            color = baseCraterColor,
            radius = radius * 0.11f,
            center = Offset(center.x + radius * 0.18f, center.y - radius * 0.45f)
        )

        val age = animatedDay.coerceIn(1f, 30f)

        // 3. Build the illuminated crescent/gibbous path dynamically
        val litPath = Path().apply {
            val rect = Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius)

            if (age >= 14.95f && age <= 15.05f) {
                // Full moon
                addOval(rect)
            } else if (age <= 1.05f || age >= 29.95f) {
                // New Moon: dark canvas only
            } else if (age < 15f) {
                // Waxing Phase (expanding light on the RIGHT)
                // Draw right hemisphere semicircle
                arcTo(rect, -90f, 180f, false)

                // Draw semi-ellipse terminator back to the top
                val scale = (age - 7.5f) / 7.5f // -1.0 (day 1) to 0.0 (day 7.5) to +1.0 (day 15)
                val ellipseWidth = radius * scale

                cubicTo(
                    center.x + ellipseWidth, center.y + radius * 0.5f,
                    center.x + ellipseWidth, center.y - radius * 0.5f,
                    center.x, center.y - radius
                )
                close()
            } else {
                // Waning Phase (shrinking light on the LEFT)
                // Draw left hemisphere semicircle
                arcTo(rect, 90f, 180f, false)

                // Draw semi-ellipse terminator back to bottom
                val scale = (22.5f - age) / 7.5f // +1.0 (day 15) to 0.0 (day 22.5) to -1.0 (day 30)
                val ellipseWidth = radius * scale

                cubicTo(
                    center.x + ellipseWidth, center.y - radius * 0.5f,
                    center.x + ellipseWidth, center.y + radius * 0.5f,
                    center.x, center.y + radius
                )
                close()
            }
        }

        // 4. Fill illuminated path with custom radiant moonlit gradient & glowing highlights
        if (age > 1f && age < 30f) {
            val moonBrush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFFDFA), // bright white hot core
                    Color(0xFFFFF59D), // radiant light yellow
                    Color(0xFFFBC02D)  // rich golden limb
                ),
                center = center,
                radius = radius
            )

            drawPath(
                path = litPath,
                brush = moonBrush
            )

            // Draw elevated craters within illuminated region only
            val litCraterColor = Color(0xFFF57F17).copy(alpha = 0.18f)
            clipPath(litPath) {
                drawCircle(
                    color = litCraterColor,
                    radius = radius * 0.20f,
                    center = Offset(center.x - radius * 0.35f, center.y - radius * 0.3f)
                )
                drawCircle(
                    color = litCraterColor,
                    radius = radius * 0.14f,
                    center = Offset(center.x + radius * 0.4f, center.y + radius * 0.35f)
                )
                drawCircle(
                    color = litCraterColor,
                    radius = radius * 0.11f,
                    center = Offset(center.x + radius * 0.18f, center.y - radius * 0.45f)
                )
                drawCircle(
                    color = litCraterColor,
                    radius = radius * 0.09f,
                    center = Offset(center.x - radius * 0.45f, center.y + radius * 0.4f)
                )
            }
        }

        // 5. Ambient glowing soft ring
        if (ringGlowColor.alpha > 0.005f) {
            drawCircle(
                color = ringGlowColor,
                radius = radius + 3.dp.toPx(),
                center = center,
                style = Stroke(width = 1.6f.dp.toPx())
            )
        }
    }
}

// Data class to represent localized moon phase info
data class LocalizedPhaseInfo(
    val name: String,
    val translatedName: String,
    val significance: String
)

private fun getMoonPhaseInfo(day: Int, lang: String): LocalizedPhaseInfo {
    val age = day.coerceIn(1, 30)
    return when (age) {
        1 -> LocalizedPhaseInfo(
            name = "Hilal (New Crescent)",
            translatedName = if (lang == "ur") "ہلال (نیا چاند)" else if (lang == "ar") "الهلال (بداية الشهر)" else "New Moon Crescent",
            significance = if (lang == "ur") "پہلی رات کا ہلال۔ نئی اسلامی تاریخ کے آغاز پر کثرت سے صدقہ دینا اور دعا کرنا مستحب ہے۔"
            else if (lang == "ar") "رؤية هلال أول الشهر. ينصح بقراءة أدعية الكلم الحسن وتقديم الصدقات لدفع البلاء."
            else "Beginning of the Islamic month. Recommended to offer charity and recite the new moon Dua to seek blessings."
        )
        in 2..6 -> LocalizedPhaseInfo(
            name = "Waxing Crescent",
            translatedName = if (lang == "ur") "ہلالِ متزاید (بڑھتا چاند)" else if (lang == "ar") "الهلال المتزايد" else "Hilal-e-Mutazayid",
            significance = if (lang == "ur") "ابتدائی ایام۔ اسلامی تاریخ کے اعمال بڑھنے کے ساتھ نمازوں میں روحانیت حاصل کریں۔"
            else if (lang == "ar") "بدء زيادة الإشعاع. يستمر المؤمن في متابعة صلواته ويومه بحضور قلبي."
            else "Early days of the crescent. Focus on establishing core spiritual habits as the lunar light grows."
        )
        in 7..8 -> LocalizedPhaseInfo(
            name = "First Quarter (Half Moon)",
            translatedName = if (lang == "ur") "تربیعِ اول (نصف چاند)" else if (lang == "ar") "التربيع الأول (نصف القمر)" else "Yowm al-Tarbie'",
            significance = if (lang == "ur") "چاند کا نصف حصہ روشن ہے۔ علم و فہم میں توازن پیدا کرنے اور محاسبہ نفس کا مبارک دن۔"
            else if (lang == "ar") "القمر نصف مضاء. نهار رائع لمحاسبة النفس وإقامة صلوات النوافل."
            else "Perfectly half illuminated. A symbolic checkpoint of balance to review your monthly progress and prayers."
        )
        in 9..13 -> LocalizedPhaseInfo(
            name = "Waxing Gibbous",
            translatedName = if (lang == "ur") "قبل از ہمہ روشن چاند" else if (lang == "ar") "الأحدب المتزايد" else "Approaching Complete Light",
            significance = if (lang == "ur") "چاند مکمل ہونے کے قریب۔ ایام البیض (13، 14، 15) کے روزے اور دعاؤں کے لیے تیاری کریں۔"
            else if (lang == "ar") "اقتراب اكتمال النور. استعد للأيام البيض المباركة لأصحاب الفضل."
            else "Approaching absolute brilliance. Prepare for the blessed Nights of Al-Beedh (13th, 14th, and 15th) for special fasts."
        )
        in 14..15 -> LocalizedPhaseInfo(
            name = "Badr (Full Moon)",
            translatedName = if (lang == "ur") "بدر (مکمل روشن چاند)" else if (lang == "ar") "البدر (القمر الكامل)" else "Nights of Al-Beedh",
            significance = if (lang == "ur") "آسمان بقعہ نور۔ ایام البیض مستحب روزوں، توبہ و استغفار اور نمازوں کے سب سے متبرک اوقات۔"
            else if (lang == "ar") "ليالي البيض المباركة. يستحب صيامها وقراءة دعاء المجير تذللاً لله سبحانه."
            else "Fully illuminated peak. The highly emphasized Nights of Al-Beedh. Fasting and praying is deeply recommended."
        )
        in 16..21 -> LocalizedPhaseInfo(
            name = "Waning Gibbous",
            translatedName = if (lang == "ur") "مائل بہ زوال چاند" else if (lang == "ar") "الأحدب المتناقص" else "Declining Illuminance",
            significance = if (lang == "ur") "روشنی بتدریج کم ہو رہی ہے۔ استقلال اور تقویٰ کے ساتھ مستحب اعمال میں مگن رہیں۔"
            else if (lang == "ar") "انخفاض تدريجي في النور المضاء. يواصل المؤمن طاعته واستحضار الشكر."
            else "Light begins to recede. Stay persistent in your spiritual journey and constant Shia prayers."
        )
        in 22..23 -> LocalizedPhaseInfo(
            name = "Third Quarter (Half Moon)",
            translatedName = if (lang == "ur") "تربیعِ ثانی (نصف زائل)" else if (lang == "ar") "التربيع الثاني" else "Second Half Split",
            significance = if (lang == "ur") "آخری نصف چاند۔ استغفار اور خدا کی حضور توبہ کرنے کا ایک توازنی لمحہ۔"
            else if (lang == "ar") "القمر يتناقص إلى النصف. يستمر الدعاء والتضرع لله غافر الذنوب."
            else "Waning half moon. An ideal physical bookmark for humility, repentance (Istighfar), and deep contemplation."
        )
        in 24..28 -> LocalizedPhaseInfo(
            name = "Waning Crescent",
            translatedName = if (lang == "ur") "آخری ہلال" else if (lang == "ar") "الهلال المتناقص" else "Late Lunar Phase",
            significance = if (lang == "ur") "ماہ کا آخری باریک چاند۔ راتوں کی عبادت اور باطنی پاکیزگی بڑھائیں۔"
            else if (lang == "ar") "نهايات الشهر المبارك. مناسب لتطهير النية ومراجعة الأذكار والخصال المؤمنة."
            else "A thin sliver left in the sky. Direct your focus inward towards late-night prayers and internal purity."
        )
        else -> LocalizedPhaseInfo(
            name = "Mahaq (Dark Moon)",
            translatedName = if (lang == "ur") "محاق (تاریک راتیں)" else if (lang == "ar") "المحاق (نهاية الشهر)" else "Crescent Search Phase",
            significance = if (lang == "ur") "مکمل اندھیرا۔ چاند چھپ گیا ہت۔ نئے اسلامی مہینے کی رویتِ ہلال کے لیے تیار رہیں۔"
            else if (lang == "ar") "القمر ظلمة تامة. لحظة تطلع واستعداد لرصد هلال الشهر الجديد."
            else "Completely dark moon phase. The moon has finished its cycle. Be prepared to search for the new moon crescent (Hilal)."
        )
    }
}

data class ShiaEvent(val day: Int, val title: String, val subText: String = "")

private fun getShiaDatesForMonth(monthName: String, lang: String): List<ShiaEvent> {
    return when (monthName) {
        "Muharram" -> listOf(
            ShiaEvent(1, if (lang == "ur") "اسلامی نیا سال" else "Islamic New Year", if (lang == "ur") "عزاداری کا آغاز" else "Start of mourning for Imam Hussain (as)"),
            ShiaEvent(2, if (lang == "ur") "امام حسین (ع) کا ورودِ کربلا" else "Arrival of Imam Hussain (as) in Karbala", "61 AH"),
            ShiaEvent(7, if (lang == "ur") "پانی بند ہونا" else "Water Cut Off from Ahlul Bayt tents", "Tragic blockade in Karbala"),
            ShiaEvent(10, if (lang == "ur") "یوم عاشورہ (شہادت امام حسین)" else "Day of Ashura", if (lang == "ur") "شہادتِ عظمیٰ سید الشہداء (ع)" else "Martyrdom of Imam Hussain (as)"),
            ShiaEvent(25, if (lang == "ur") "شہادت امام زین العابدین (ع)" else "Martyrdom of Imam Zain-ul-Abideen (as)", "95 AH")
        )
        "Safar" -> listOf(
            ShiaEvent(7, if (lang == "ur") "شہادت امام حسن مجتبیٰ (ع)" else "Martyrdom of Imam Hassan al-Mujtaba (as)", "50 AH"),
            ShiaEvent(20, if (lang == "ur") "چہلم / اربعینِ حسینی" else "Arba'een of Imam Hussain (as)", if (lang == "ur") "کربلا میں اہل حرم کی واپسی" else "40th Day of Karbala Tragedy"),
            ShiaEvent(28, if (lang == "ur") "رحلتِ رسول اکرم (ص)" else "Demise of Prophet Muhammad (saw)", if (lang == "ur") "سایہ رحمت اٹھ گیا" else "End of Prophetic era (11 AH)"),
            ShiaEvent(30, if (lang == "ur") "شہادت امام علی رضا (ع)" else "Martyrdom of Imam Ali al-Rida (as)", "203 AH")
        )
        "Rabi' al-Awwal" -> listOf(
            ShiaEvent(8, if (lang == "ur") "شہادت امام حسن عسکری (ع)" else "Martyrdom of Imam Hassan al-Askari (as)", "Start of Ghaibah of Imam Mahdi (atfs)"),
            ShiaEvent(9, if (lang == "ur") "عیدِ زہرا (س) / تاج پوشی" else "Eid-e-Zahra (sa)", "Celebration of Ahlul Bayt"),
            ShiaEvent(17, if (lang == "ur") "ولادت رسولِ خدا (ص) و امام صادق " else "Milad-un-Nabi (saw) & Imam Jafar al-Sadiq (as)", "Spiritual light of creation")
        )
        "Rabi' al-Thani" -> listOf(
            ShiaEvent(8, if (lang == "ur") "ولادت امام حسن عسکری (ع)" else "Birthday of Imam Hassan al-Askari (as)", "232 AH"),
            ShiaEvent(10, if (lang == "ur") "وفات بی بی معصومہ قم (س)" else "Demise of Bibi Ma'sooma-e-Qum (sa)", "Sister of Imam Rida (as)")
        )
        "Jumada al-Awwal" -> listOf(
            ShiaEvent(5, if (lang == "ur") "ولادت سیدہ زینب (س)" else "Birthday of Bibi Zainab (sa)", "Hero of Karbala and resilience"),
            ShiaEvent(13, if (lang == "ur") "شہادت حضرت فاطمہ زہرا (س)" else "Martyrdom of Bibi Fatima Zahra (sa)", "Ayyam-e-Fatimiya (First Narration)")
        )
        "Jumada al-Thani" -> listOf(
            ShiaEvent(3, if (lang == "ur") "شہادت بی بی فاطمہ زہرا (س)" else "Martyrdom of Bibi Fatima Zahra (sa)", "Ayyam-e-Fatimiya (Famous Narration)"),
            ShiaEvent(20, if (lang == "ur") "ولادتِ حضرت فاطمہ زہرا (س)" else "Birthday of Bibi Fatima Zahra (sa)", "Mother of Imams")
        )
        "Rajab" -> listOf(
            ShiaEvent(1, if (lang == "ur") "ولادت امام محمد باقر (ع)" else "Birthday of Imam Muhammad al-Baqir (as)", "57 AH"),
            ShiaEvent(10, if (lang == "ur") "ولادت امام محمد تقی الجواد (ع)" else "Birthday of Imam Muhammad al-Taqi (as)", "195 AH"),
            ShiaEvent(13, if (lang == "ur") "ولادت امیر المومنین علی (ع)" else "Birthday of Amir al-Mu'minin Imam Ali (as)", "Born inside the Holy Kaaba"),
            ShiaEvent(25, if (lang == "ur") "شہادت امام موسیٰ کاظم (ع)" else "Martyrdom of Imam Musa al-Kadhim (as)", "183 AH"),
            ShiaEvent(27, if (lang == "ur") "عیدِ مبعث" else "Eid al-Bi'thah", "First revelation of Prophet Muhammad (saw)")
        )
        "Sha'ban" -> listOf(
            ShiaEvent(3, if (lang == "ur") "ولادت امام حسین (ع)" else "Birthday of Imam Hussain (as)", "3 AH - Joy of Fatima (sa)"),
            ShiaEvent(4, if (lang == "ur") "ولادت حضرت غازی عباس (ع)" else "Birthday of Hazrat Abul Fazl al-Abbas (as)", "Defender of Karbala"),
            ShiaEvent(5, if (lang == "ur") "ولادت امام زین العابدین (ع)" else "Birthday of Imam Sajjad (as)", "Master of adorers"),
            ShiaEvent(11, if (lang == "ur") "ولادت حضرت علی اکبر (ع)" else "Birthday of Hazrat Ali Akbar (as)", "Resemblance of the Prophet"),
            ShiaEvent(15, if (lang == "ur") "ولادتِ باسعادت امام مہدی (عج)" else "Birthday of Imam al-Mahdi (atfs)", "Shab-e-Barat - Savior of humanity")
        )
        "Ramadan" -> listOf(
            ShiaEvent(10, if (lang == "ur") "وفات بی بی خدیجہ الکبریٰ (س)" else "Demise of Bibi Khadija (sa)", "Benefactor of Islam"),
            ShiaEvent(15, if (lang == "ur") "ولادت امام حسن مجتبیٰ (ع)" else "Birthday of Imam Hassan al-Mujtaba (as)", "3 AH"),
            ShiaEvent(19, if (lang == "ur") "ضربتِ محرابِ علی (ع)" else "Strike on Imam Ali (as) inside Kufa Mosque", "Laylatul Qadr 19"),
            ShiaEvent(21, if (lang == "ur") "شہادت امیر المومنین علی (ع)" else "Martyrdom of Imam Ali (as)", "Laylatul Qadr 21"),
            ShiaEvent(23, if (lang == "ur") "شبِ قدرِ کبریٰ" else "Laylatul Qadr (23rd Night Peak)", "Most emphasized night of destiny")
        )
        "Shawwal" -> listOf(
            ShiaEvent(1, if (lang == "ur") "عید الفطر" else "Eid al-Fitr", "Day of thanksgiving and reward"),
            ShiaEvent(8, if (lang == "ur") "تخریجِ جنت البقیع" else "Destruction of Jannat-ul-Baqi", "1344 AH - Mourning of shrines"),
            ShiaEvent(25, if (lang == "ur") "شہادت امام جعفر صادق (ع)" else "Martyrdom of Imam Jafar al-Sadiq (as)", "148 AH - Leader of Shia school")
        )
        "Dhu al-Qa'dah" -> listOf(
            ShiaEvent(1, if (lang == "ur") "ولادت بی بی معصومہ قم (س)" else "Birthday of Bibi Ma'sooma-e-Qum (sa)", "173 AH"),
            ShiaEvent(11, if (lang == "ur") "ولادت امام علی رضا (ع)" else "Birthday of Imam Ali al-Rida (as)", "148 AH"),
            ShiaEvent(29, if (lang == "ur") "شہادت امام محمد تقی الجواد (ع)" else "Martyrdom of Imam Muhammad al-Taqi (as)", "220 AH")
        )
        "Dhu al-Hijjah" -> listOf(
            ShiaEvent(1, if (lang == "ur") "تزویج علی (ع) و فاطمہ (س)" else "Marriage of Imam Ali (as) and Bibi Fatima (sa)", "Divine sacred union"),
            ShiaEvent(7, if (lang == "ur") "شہادت امام محمد باقر (ع)" else "Martyrdom of Imam Muhammad al-Baqir (as)", "114 AH"),
            ShiaEvent(9, if (lang == "ur") "روزِ عرفہ" else "Day of 'Arafah & Muslim ibn Aqeel (as) martyrdom", "Day of supplication"),
            ShiaEvent(10, if (lang == "ur") "عید الاضحیٰ" else "Eid al-Adha", "Sacred Feast of Sacrifice"),
            ShiaEvent(18, if (lang == "ur") "عیدِ غدیرِ خم" else "Eid al-Ghadir (Declaration of Wilayah)", "Completion of divine religion"),
            ShiaEvent(24, if (lang == "ur") "عیدِ مباہلہ" else "Eid al-Mubahalah", "Victory of Ahlul Bayt truth (10 AH)")
        )
        else -> emptyList()
    }
}
