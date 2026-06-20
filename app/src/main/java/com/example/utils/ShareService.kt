package com.example.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import androidx.core.content.FileProvider
import com.example.api.ShiaQuote
import com.example.api.UrduTranslations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ShareService {

    /**
     * Standardizes sharing a ShiaQuote as an elegantly styled image card.
     */
    fun shareQuoteCard(
        context: Context,
        quote: ShiaQuote,
        includeArabic: Boolean,
        includeEnglish: Boolean,
        includeUrdu: Boolean,
        fileNamePrefix: String = "shiapulse_quote",
        chooserTitle: String = "Share Card",
        scope: CoroutineScope
    ) {
        scope.launch(Dispatchers.IO) {
            val urduTrans = if (includeUrdu) UrduTranslations.getUrdu(quote, context) else null
            val arabic = if (includeArabic) quote.arabic else null
            val english = if (includeEnglish) quote.english else null

            // Generate the bitmap using standard serialization format
            val bitmap = generateQuoteCardBitmap(context, arabic, english, urduTrans, quote.source)
            
            var uri: Uri? = null

            // Priority 1: MediaStore (Modern way, saves directly to user's gallery in ShiaPulse album)
            try {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "${fileNamePrefix}_${System.currentTimeMillis()}.png")
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ShiaPulse")
                    }
                }
                uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                }
            } catch (e: Exception) {
                Log.e("ShiaPulse", "MediaStore insert failed, falling back to FileProvider: ${e.message}", e)
                uri = null
            }

            // Priority 2: FileProvider fallback (Guaranteed to work on all SDK levels without extra permissions)
            if (uri == null) {
                try {
                    val cachePath = File(context.cacheDir, "shared_images")
                    if (!cachePath.exists()) {
                        cachePath.mkdirs()
                    }
                    val file = File(cachePath, "${fileNamePrefix}_${System.currentTimeMillis()}.png")
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    
                    val authority = "${context.packageName}.fileprovider"
                    uri = FileProvider.getUriForFile(context, authority, file)
                } catch (e: Exception) {
                    Log.e("ShiaPulse", "FileProvider share failed: ${e.message}", e)
                }
            }

            // Trigger the share chooser
            if (uri != null) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                withContext(Dispatchers.Main) {
                    try {
                        context.startActivity(Intent.createChooser(shareIntent, chooserTitle))
                    } catch (e: Exception) {
                        Log.e("ShiaPulse", "Failed to start activity: ${e.message}", e)
                    }
                }
            }
        }
    }

    /**
     * Standardizes sharing a ShiaQuote as formatted plain text.
     */
    fun shareQuoteText(
        context: Context,
        quote: ShiaQuote,
        isUrdu: Boolean = false,
        chooserTitle: String = "Share Item"
    ) {
        val urduTrans = UrduTranslations.getUrdu(quote, context)
        val shareText = """
            ✨ Daily Guidance ✨
            
            📖 Arabic: ${quote.arabic}
            
            🇬🇧 English: ${quote.english}
            
            🇵🇰 اردو: $urduTrans
            
            Source: ${quote.source}
            — via Shia Pulse (Guidance Portal)
        """.trimIndent()

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Daily Guidance")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        try {
            context.startActivity(Intent.createChooser(shareIntent, chooserTitle))
        } catch (e: Exception) {
            Log.e("ShiaPulse", "Failed to share plain text: ${e.message}", e)
        }
    }

    /**
     * Centralized graphics serialization block to generate the card image bitmap.
     * Ensures consistent padding, colors, typeface scaling, and background.
     */
    fun generateQuoteCardBitmap(
        context: Context,
        arabicText: String?,
        englishText: String?,
        urduText: String?,
        source: String
    ): Bitmap {
        val width = 1080
        val padding = 100 // Beautiful, generous internal padding
        val innerWidth = width - padding * 2

        // Soft, premium fonts and high-contrast styling for the elegant light theme
        val textPaintArabic = TextPaint().apply {
            color = Color.parseColor("#065F46") // Deep Islamic Emerald Green for Arabic
            textSize = 70f
            isAntiAlias = true
            typeface = Typeface.create("serif", Typeface.BOLD)
        }

        val textPaintTranslation = TextPaint().apply {
            color = Color.parseColor("#0F172A") // Deep Charcoal Slate for English
            textSize = 44f
            isAntiAlias = true
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }

        val textPaintUrdu = TextPaint().apply {
            color = Color.parseColor("#1E293B") // Elegant Dark Slate for Urdu Nastaliq
            textSize = 52f
            isAntiAlias = true
            typeface = Typeface.create("serif", Typeface.NORMAL)
        }

        val textPaintSourceTP = TextPaint().apply {
            color = Color.parseColor("#B55D00") // Warm terracotta/gold style reference
            textSize = 38f
            isAntiAlias = true
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }

        val textPaintBrandingLabel = Paint().apply {
            color = Color.parseColor("#475569") // Soft modern slate for ShiaPulse label
            textSize = 38f
            isAntiAlias = true
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }

        // Layout creation for perfect word wrapping
        val arabicLayout = arabicText?.let {
            StaticLayout.Builder.obtain(it, 0, it.length, textPaintArabic, innerWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1.3f)
                .build()
        }

        val englishLayout = englishText?.let {
            StaticLayout.Builder.obtain(it, 0, it.length, textPaintTranslation, innerWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1.25f)
                .build()
        }

        val urduLayout = urduText?.let {
            StaticLayout.Builder.obtain(it, 0, it.length, textPaintUrdu, innerWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1.3f)
                .build()
        }

        val sourceLayout = StaticLayout.Builder.obtain(source, 0, source.length, textPaintSourceTP, innerWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .build()

        // Calculate total canvas height dynamically
        var totalHeight = 100 // Top padding above card
        
        if (arabicLayout != null) totalHeight += arabicLayout.height + 60
        if (englishLayout != null) totalHeight += englishLayout.height + 60
        if (urduLayout != null) totalHeight += urduLayout.height + 60
        totalHeight += sourceLayout.height + 120 // Space after source to divider
        totalHeight += 100 // Space for android icon & brand name
        totalHeight += 100 // Bottom padding

        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw Alabaster Base Background
        canvas.drawColor(Color.parseColor("#FAF9F6")) // Beautiful natural Alabaster base

        // Decodes and overlays the mosque background
        try {
            val bgResId = com.example.R.drawable.mosque_background_1780218634256
            val rawBg = android.graphics.BitmapFactory.decodeResource(context.resources, bgResId)
            if (rawBg != null) {
                val srcRect = android.graphics.Rect(0, 0, rawBg.width, rawBg.height)
                val dstRect = android.graphics.Rect(0, 0, width, totalHeight)
                val filterPaint = Paint(Paint.FILTER_BITMAP_FLAG)
                canvas.drawBitmap(rawBg, srcRect, dstRect, filterPaint)
                rawBg.recycle()
            }
        } catch (e: Exception) {
            Log.e("ShiaPulse", "Failed to load mosque background pattern: ${e.message}")
        }

        // Draw a light frosting tint over the background to keep the mosque pattern subtle
        // and ensure the text is highly readable with high accessibility contrast.
        canvas.drawColor(Color.argb(230, 250, 249, 246))

        // Draw the Inner Card Frame (Card Bounds offset by 45 pixels)
        val cardRect = android.graphics.RectF(45f, 45f, width - 45f, totalHeight - 45f)
        
        // 1. Opaque card background fill
        val cardBgPaint = Paint().apply {
            color = Color.argb(248, 255, 255, 255) // Solid white card top-layer
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(cardRect, 32f, 32f, cardBgPaint)

        // 2. Beautiful double golden custom border around the card content
        val goldBorderPaint = Paint().apply {
            color = Color.parseColor("#C5A880") // Premium Sand Gold
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawRoundRect(cardRect, 32f, 32f, goldBorderPaint)

        val innerBorderRect = android.graphics.RectF(55f, 55f, width - 55f, totalHeight - 55f)
        val thinGoldBorderPaint = Paint().apply {
            color = Color.parseColor("#E5D5C0") // Lighter sand gold line
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        canvas.drawRoundRect(innerBorderRect, 24f, 24f, thinGoldBorderPaint)

        // Render contents sequentially
        var currentY = padding + 40

        if (arabicLayout != null) {
            canvas.save()
            canvas.translate(padding.toFloat(), currentY.toFloat())
            arabicLayout.draw(canvas)
            canvas.restore()
            currentY += arabicLayout.height + 60
        }

        if (englishLayout != null) {
            canvas.save()
            canvas.translate(padding.toFloat(), currentY.toFloat())
            englishLayout.draw(canvas)
            canvas.restore()
            currentY += englishLayout.height + 60
        }

        if (urduLayout != null) {
            canvas.save()
            canvas.translate(padding.toFloat(), currentY.toFloat())
            urduLayout.draw(canvas)
            canvas.restore()
            currentY += urduLayout.height + 60
        }

        // Draw Source details
        canvas.save()
        canvas.translate(padding.toFloat(), currentY.toFloat())
        sourceLayout.draw(canvas)
        canvas.restore()
        currentY += sourceLayout.height + 60

        // Draw clean divider before Android icon & brand label
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#E2E8F0") // Soft grey
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(150f, currentY.toFloat(), width - 150f, currentY.toFloat(), dividerPaint)
        
        currentY += 50 // Advance past divider

        // Bottom horizontal row: Android System Icon + brand label "ShiaPulse"
        val brandText = "ShiaPulse"
        val labelWidth = textPaintBrandingLabel.measureText(brandText)
        val iconSize = 56f
        val itemSpacing = 16f
        val totalRowWidth = iconSize + itemSpacing + labelWidth
        val startX = (width - totalRowWidth) / 2f

        // Safely load the official Android icon drawable
        val androidIcon = try {
            val drawable = androidx.core.content.res.ResourcesCompat.getDrawable(
                context.resources,
                android.R.drawable.sym_def_app_icon,
                null
            )
            if (drawable != null) {
                val bmp = Bitmap.createBitmap(iconSize.toInt(), iconSize.toInt(), Bitmap.Config.ARGB_8888)
                val canvasBmp = Canvas(bmp)
                drawable.setBounds(0, 0, iconSize.toInt(), iconSize.toInt())
                drawable.draw(canvasBmp)
                bmp
            } else null
        } catch (e: Exception) {
            Log.e("ShiaPulse", "Failed to decode android system app icon: ${e.message}")
            null
        }

        // Draw Android Icon
        if (androidIcon != null) {
            val iconDst = android.graphics.RectF(startX, currentY.toFloat(), startX + iconSize, currentY.toFloat() + iconSize)
            canvas.drawBitmap(androidIcon, null, iconDst, Paint(Paint.FILTER_BITMAP_FLAG))
            androidIcon.recycle()
        }

        // Draw "ShiaPulse" Brand text next to the icon, vertically centered
        val brandTextY = currentY.toFloat() + iconSize / 2f + (textPaintBrandingLabel.textSize / 2.7f)
        canvas.drawText(
            brandText,
            startX + iconSize + itemSpacing,
            brandTextY,
            textPaintBrandingLabel
        )

        return bitmap
    }
}
