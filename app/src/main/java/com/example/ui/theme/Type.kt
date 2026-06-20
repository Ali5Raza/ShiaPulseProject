package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Arabic & English premium font families
val ArabicFontFamily = FontFamily.Serif
val EnglishFontFamily = FontFamily.SansSerif

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = ArabicFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 42.sp,
        lineHeight = 50.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = ArabicFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = ArabicFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = EnglishFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = EnglishFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = EnglishFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.15.sp
    ),
    titleLarge = TextStyle(
        fontFamily = EnglishFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleMedium = TextStyle(
        fontFamily = EnglishFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = EnglishFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = EnglishFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = EnglishFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = EnglishFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = EnglishFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = EnglishFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = EnglishFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
fun getTypographyWithFont(fontFamily: FontFamily): Typography {
    val platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = true)
    return Typography(
        displayLarge = Typography.displayLarge.copy(fontFamily = fontFamily, platformStyle = platformStyle),
        displayMedium = Typography.displayMedium.copy(fontFamily = fontFamily, platformStyle = platformStyle),
        displaySmall = Typography.displaySmall.copy(fontFamily = fontFamily, platformStyle = platformStyle),
        headlineLarge = Typography.headlineLarge.copy(fontFamily = fontFamily, platformStyle = platformStyle),
        headlineMedium = Typography.headlineMedium.copy(fontFamily = fontFamily, platformStyle = platformStyle),
        headlineSmall = Typography.headlineSmall.copy(fontFamily = fontFamily, platformStyle = platformStyle),
        titleLarge = Typography.titleLarge.copy(fontFamily = fontFamily, platformStyle = platformStyle),
        titleMedium = Typography.titleMedium.copy(fontFamily = fontFamily, platformStyle = platformStyle),
        titleSmall = Typography.titleSmall.copy(fontFamily = fontFamily, platformStyle = platformStyle),
        bodyLarge = Typography.bodyLarge.copy(fontFamily = fontFamily, platformStyle = platformStyle),
        bodyMedium = Typography.bodyMedium.copy(fontFamily = fontFamily, platformStyle = platformStyle),
        bodySmall = Typography.bodySmall.copy(fontFamily = fontFamily, platformStyle = platformStyle),
        labelLarge = Typography.labelLarge.copy(fontFamily = fontFamily, platformStyle = platformStyle),
        labelMedium = Typography.labelMedium.copy(fontFamily = fontFamily, platformStyle = platformStyle),
        labelSmall = Typography.labelSmall.copy(fontFamily = fontFamily, platformStyle = platformStyle)
    )
}
