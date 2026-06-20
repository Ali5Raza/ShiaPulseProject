package com.example.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.example.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Dynamic Google Fonts with beautiful native system fallback typefaces.
val AmiriFont = FontFamily(
    androidx.compose.ui.text.googlefonts.Font(googleFont = GoogleFont("Amiri"), fontProvider = provider),
    androidx.compose.ui.text.font.Font(DeviceFontFamilyName("serif"), weight = FontWeight.Normal)
)

val NotoNaskhArabicFont = FontFamily(
    androidx.compose.ui.text.googlefonts.Font(googleFont = GoogleFont("Noto Naskh Arabic"), fontProvider = provider),
    androidx.compose.ui.text.font.Font(DeviceFontFamilyName("sans-serif"), weight = FontWeight.Normal)
)

val NotoNastaliqUrduFont = FontFamily(
    androidx.compose.ui.text.googlefonts.Font(googleFont = GoogleFont("Noto Nastaliq Urdu"), fontProvider = provider),
    androidx.compose.ui.text.font.Font(DeviceFontFamilyName("noto-nastaliq-urdu"), weight = FontWeight.Normal)
)

fun getAppFontFamily(fontName: String): FontFamily? {
    return when(fontName) {
        "Amiri" -> AmiriFont
        "Noto Naskh Arabic" -> NotoNaskhArabicFont
        "Jameel Noori" -> NotoNastaliqUrduFont
        "Nafees Web" -> NotoNastaliqUrduFont
        "Urdu Typesetting" -> NotoNastaliqUrduFont
        else -> null
    }
}

/**
 * Centered dynamic font family resolver for ShiaPulse app.
 * Resolves font based on selected font setting AND the chosen active language.
 */
fun getAppFontFamily(fontName: String, languageCode: String?): FontFamily {
    if (fontName != "system" && fontName.isNotEmpty()) {
        val resolved = when(fontName) {
            "Amiri" -> AmiriFont
            "Noto Naskh Arabic", "Noto Naskh" -> NotoNaskhArabicFont
            "Jameel Noori", "Nafees Web", "Urdu Typesetting" -> NotoNastaliqUrduFont
            else -> null
        }
        if (resolved != null) return resolved
    }
    
    // Fall back dynamically based on active language Code
    return when (languageCode) {
        "ur" -> NotoNastaliqUrduFont  // Jameel Noori equivalent (Nastaleeq style)
        "ar" -> AmiriFont            // Amiri style
        else -> NotoNaskhArabicFont  // Noto Naskh style
    }
}



