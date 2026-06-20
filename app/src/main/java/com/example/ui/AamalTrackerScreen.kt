package com.example.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.AamalCompletion
import com.example.data.AamalCustomActivity
import java.text.SimpleDateFormat
import java.util.*

// Formats
private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AamalTrackerScreen(
    viewModel: PrayerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang by viewModel.appLanguage.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme() || (viewModel.themeMode.collectAsStateWithLifecycle().value == "dark")
    
    // DB States
    val customAamal by viewModel.allCustomAamal.collectAsStateWithLifecycle()
    val todayCompletedList by viewModel.todayAamalCompletions.collectAsStateWithLifecycle()
    val allCompletions by viewModel.allAamalCompletions.collectAsStateWithLifecycle()
    val rawSelectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val activeDateStr = sdf.format(rawSelectedDate.time)
    
    // UI Dialog States
    var showAddCustomDialog by remember { mutableStateOf(false) }
    var selectedDetailItem by remember { mutableStateOf<AamalDef?>(null) }
    var selectedDetailCustom by remember { mutableStateOf<AamalCustomActivity?>(null) }
    
    // Selected Categories Visibility Filter (stored in SharedPreferences)
    val sharedPrefs = remember { context.getSharedPreferences("aamal_filters", Context.MODE_PRIVATE) }
    var visibleCategories by remember {
        mutableStateOf(
            AamalCategory.values().associateWith { cat ->
                sharedPrefs.getBoolean("cat_${cat.name}", true)
            }
        )
    }
    
    // Sync category toggle persistency
    fun toggleCategoryVisibility(category: AamalCategory) {
        val currentVal = visibleCategories[category] ?: true
        val newVal = !currentVal
        visibleCategories = visibleCategories.toMutableMap().apply { put(category, newVal) }
        sharedPrefs.edit().putBoolean("cat_${category.name}", newVal).apply()
    }

    // Default checklist objects defined statically below
    val defaultList = remember { getDefaultAamalList() }
    
    // Calculate statistics
    val todayRequiredDefaultList = defaultList.filter { visibleCategories[it.category] == true }
    val todayRequiredCustomList = customAamal.filter { it.isEnabled && visibleCategories[AamalCategory.CUSTOM] == true }
    
    val totalRequiredCount = todayRequiredDefaultList.size + todayRequiredCustomList.size
    val totalCompletedCount = remember(todayCompletedList, todayRequiredDefaultList, todayRequiredCustomList) {
        val completedIds = todayCompletedList.filter { it.isCompleted }.map { it.activityId }.toSet()
        val defaultCompleted = todayRequiredDefaultList.count { it.id in completedIds }
        val customCompleted = todayRequiredCustomList.count { it.id in completedIds }
        defaultCompleted + customCompleted
    }
    
    val completionPercentage = if (totalRequiredCount > 0) {
        (totalCompletedCount.toFloat() / totalRequiredCount.toFloat())
    } else 0f

    val currentStreak = remember(allCompletions, customAamal, defaultList) {
        calculateCurrentStreak(allCompletions, customAamal, defaultList.size)
    }

    // Main layout
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize().testTag("aamal_tracker_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Bar
            TopAppBar(
                title = {
                    Text(
                        text = when (currentLang) {
                            "ur" -> "اعمالِ روزانہ"
                            "ar" -> "الأعمال اليومية"
                            "fa" -> "اعمال روزانه"
                            "hi" -> "आमले रोज़ाना"
                            else -> "Aamal-e-Rozana"
                        },
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("aamal_back_btn")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddCustomDialog = true },
                        modifier = Modifier.testTag("aamal_add_custom_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Custom Act",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // Dynamic Scrollable Body
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Visual Summary Panel Card
                item {
                    DailyProgressCard(
                        percentage = completionPercentage,
                        completed = totalCompletedCount,
                        total = totalRequiredCount,
                        streak = currentStreak,
                        languageCode = currentLang
                    )
                }

                // Category Quick Filter Box
                item {
                    CategoryFilterPanel(
                        visibleCategories = visibleCategories,
                        onToggle = { toggleCategoryVisibility(it) },
                        languageCode = currentLang
                    )
                }

                // Checklists partitioned by Categories
                AamalCategory.values().forEach { category ->
                    val isVisible = visibleCategories[category] ?: true
                    if (isVisible) {
                        val isCustom = (category == AamalCategory.CUSTOM)
                        val categoryItems = defaultList.filter { it.category == category }
                        
                        if (isCustom && todayRequiredCustomList.isNotEmpty()) {
                            item {
                                CategorySectionHeader(category, currentLang)
                            }
                            items(todayRequiredCustomList) { customAct ->
                                val isChecked = todayCompletedList.any { it.activityId == customAct.id && it.isCompleted }
                                AamalCardItem(
                                    titleStr = customAct.title,
                                    subtitleStr = when (currentLang) {
                                        "ur" -> "صارف کا کسٹم عمل"
                                        "ar" -> "عمل مخصص مضاف"
                                        "fa" -> "عمل سفارشی کاربر"
                                        "hi" -> "कस्टम कार्य"
                                        else -> "Custom user-generated act"
                                    },
                                    isChecked = isChecked,
                                    onCheckChange = { checked ->
                                        viewModel.toggleAamalCompletion(
                                            activityId = customAct.id,
                                            isCompleted = checked,
                                            dateStr = activeDateStr
                                        )
                                    },
                                    onInfoClick = {
                                        selectedDetailCustom = customAct
                                    }
                                )
                            }
                        } else if (!isCustom && categoryItems.isNotEmpty()) {
                            item {
                                CategorySectionHeader(category, currentLang)
                            }
                            items(categoryItems) { item ->
                                val isChecked = todayCompletedList.any { it.activityId == item.id && it.isCompleted }
                                AamalCardItem(
                                    titleStr = item.getTitle(currentLang),
                                    subtitleStr = item.getSubtitle(currentLang),
                                    isChecked = isChecked,
                                    onCheckChange = { checked ->
                                        viewModel.toggleAamalCompletion(
                                            activityId = item.id,
                                            isCompleted = checked,
                                            dateStr = activeDateStr
                                        )
                                    },
                                    onInfoClick = {
                                        selectedDetailItem = item
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Extra Spacing bottom
                item {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }

    // Add Custom Act dialog
    if (showAddCustomDialog) {
        AddCustomActDialog(
            languageCode = currentLang,
            onDismiss = { showAddCustomDialog = false },
            onSave = { title ->
                viewModel.addCustomAamal(title, "Custom", activeDateStr)
                showAddCustomDialog = false
                Toast.makeText(context, "Added custom routine!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Details/Rulings Dialogue for Default Acts
    selectedDetailItem?.let { item ->
        AamalDetailDialog(
            item = item,
            languageCode = currentLang,
            onDismiss = { selectedDetailItem = null }
        )
    }

    // Details Dialogue for Custom Acts
    selectedDetailCustom?.let { act ->
        CustomAamalDetailDialog(
            act = act,
            languageCode = currentLang,
            onDismiss = { selectedDetailCustom = null },
            onDelete = {
                viewModel.deleteCustomAamal(act.id)
                selectedDetailCustom = null
                Toast.makeText(context, "Routine deleted", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// Visual layout components
@Composable
fun DailyProgressCard(
    percentage: Float,
    completed: Int,
    total: Int,
    streak: Int,
    languageCode: String
) {
    val percentString = "${(percentage * 100).toInt()}%"
    
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("aamal_progress_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular widget with drawBehind
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(90.dp)
                    .drawBehind {
                        // Background circle track
                        drawArc(
                            color = Color.White.copy(alpha = 0.28f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                        // Progress arc
                        drawArc(
                            color = Color(0xFFFFD54F), // Premium gold
                            startAngle = -90f,
                            sweepAngle = percentage * 360f,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
            ) {
                Text(
                    text = percentString,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = when (languageCode) {
                        "ur" -> "آج کی روحانی پیشرفت"
                        "ar" -> "التقدم الروحي اليوم"
                        "fa" -> "پیشرفت معنوی امروز"
                        "hi" -> "आज की प्रगति"
                        else -> "Today's Spiritual Progress"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = if (total == 0) {
                        when (languageCode) {
                            "ur" -> "کوئی اعمال ابھی منتخب نہیں"
                            else -> "No tasks listed for today"
                        }
                    } else {
                        when (languageCode) {
                            "ur" -> "$completed میں سے $total اعمال مکمل"
                            "ar" -> "تم إكمال $completed من أصل $total"
                            "fa" -> "$completed از $total کار انجام شد"
                            "hi" -> "$completed میں سے $total پورے"
                            else -> "Completed $completed of $total acts"
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
                )

                // Streak Badge
                if (streak > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color.Black.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Streak",
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (languageCode) {
                                    "ur" -> "مسلسل روحانی تسلسل: $streak دن 🔥"
                                    "ar" -> "سلسلة الأيام الروحية: $streak أيام 🔥"
                                    "fa" -> "تداوم معنوی: $streak روز 🔥"
                                    "hi" -> "लगातार आध्यात्मिक दिन: $streak 🔥"
                                    else -> "Spiritual Streak: $streak days 🔥"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryFilterPanel(
    visibleCategories: Map<AamalCategory, Boolean>,
    onToggle: (AamalCategory) -> Unit,
    languageCode: String
) {
    Column {
        Text(
            text = when (languageCode) {
                "ur" -> "دستور العمل کے حصے فلٹر کریں:"
                "ar" -> "تصفية الأقسام اليومية:"
                "fa" -> "فیلتر کردن بخش اعمال:"
                "hi" -> "श्रेणी चुने:"
                else -> "Toggle Active Sections:"
            },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AamalCategory.values().forEach { category ->
                val isActive = visibleCategories[category] ?: true
                FilterChip(
                    selected = isActive,
                    onClick = { onToggle(category) },
                    label = {
                        Text(
                            text = category.getLabel(languageCode),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("aamal_filter_chip_${category.name.lowercase()}")
                )
            }
        }
    }
}

@Composable
fun CategorySectionHeader(category: AamalCategory, languageCode: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category.getLabel(languageCode),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Divider(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AamalCardItem(
    titleStr: String,
    subtitleStr: String,
    isChecked: Boolean,
    onCheckChange: (Boolean) -> Unit,
    onInfoClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isChecked) MaterialTheme.colorScheme.primary.copy(alpha = 0.38f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckChange(!isChecked) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ripple checkbox
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckChange,
                modifier = Modifier
                    .scale(1.1f)
                    .testTag("aamal_checkbox_${titleStr.replace(" ", "_").lowercase()}")
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = titleStr,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                if (subtitleStr.isNotEmpty()) {
                    Text(
                        text = subtitleStr,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }
            }

            IconButton(
                onClick = onInfoClick,
                modifier = Modifier.testTag("aamal_info_btn_${titleStr.replace(" ", "_").lowercase()}")
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Details",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.78f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// Dialog for adding custom act
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomActDialog(
    languageCode: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (languageCode) {
                        "ur" -> "ایک نیا کار شامل کریں"
                        "ar" -> "إضافة عمل مخصص جديد"
                        "fa" -> "افزودن کار جدید"
                        "hi" -> "नया कार्य जोड़ें"
                        else -> "Add Custom Routine"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(text = "e.g., Read 1 page of Nahjul Balagha")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_custom_input_field"),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = when (languageCode) {
                                "ur" -> "منسوخ"
                                else -> "Cancel"
                            }
                        )
                    }
                    Button(
                        onClick = {
                            if (textInput.trim().isNotEmpty()) {
                                onSave(textInput.trim())
                            }
                        },
                        enabled = textInput.trim().isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_custom_activity_btn")
                    ) {
                        Text(
                            text = when (languageCode) {
                                "ur" -> "محفوظ کریں"
                                else -> "Save"
                            }
                        )
                    }
                }
            }
        }
    }
}

// Dialog sharing button
fun shareAamalRoutineText(context: Context, label: String, ar: String, enDesc: String) {
    val shareBody = """
        🕌 Aamal-e-Rozana Routine Details 🕌
        ✨ Act: $label
        
        📖 Arabic: $ar
        📝 Description & Reference:
        $enDesc
        
        Shared via Shia Pulse App.
    """.trimIndent()
    val sendIntent = android.content.Intent().apply {
        action = android.content.Intent.ACTION_SEND
        putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
        type = "text/plain"
    }
    context.startActivity(android.content.Intent.createChooser(sendIntent, "Share Aamal Routine Details"))
}

// Detail Dialog for default core items
@Composable
fun AamalDetailDialog(
    item: AamalDef,
    languageCode: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var personalNotes by remember {
        mutableStateOf(
            context.getSharedPreferences("aamal_item_notes", Context.MODE_PRIVATE)
                .getString("notes_${item.id}", "") ?: ""
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 4.dp)
                .testTag("aamal_detail_dialog_${item.id}")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Title and category badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.getTitle(languageCode),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            shareAamalRoutineText(
                                context,
                                item.getTitle(languageCode),
                                item.arabic,
                                item.getFarsiDescription() // default descriptive notes
                            )
                        },
                        modifier = Modifier.testTag("share_aamal_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share details",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = item.category.getLabel(languageCode),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Shia Ja'fari Fiqh timings and descriptions
                Text(
                    text = when (languageCode) {
                        "ur" -> "وضاحت اور احکام / حوالہ:"
                        "ar" -> "الشرح والأحكام / المرجع:"
                        else -> "Explanation, Rulings & Authentic Reference:"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.getDetailedNotes(languageCode),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (item.arabic.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "عربي (Arabic):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.arabic,
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Right,
                        lineHeight = 34.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Reference Citation label
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = when (languageCode) {
                        "ur" -> "مستند کتاب:"
                        "ar" -> "الكتاب المعتمد:"
                        else -> "Authentic Ja'fari Fiqh Reference:"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = item.reference,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Personal Reflections Notes Field
                Spacer(modifier = Modifier.height(20.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = when (languageCode) {
                        "ur" -> "ذاتی عکاسی اور نوٹس:"
                        else -> "Personal Reflections & Notes:"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = personalNotes,
                    onValueChange = { notes ->
                        personalNotes = notes
                        context.getSharedPreferences("aamal_item_notes", Context.MODE_PRIVATE)
                            .edit()
                            .putString("notes_${item.id}", notes)
                            .apply()
                    },
                    placeholder = {
                        Text(text = "Capture spiritually enlightening thoughts or reflections here...")
                    },
                    modifier = Modifier.fillMaxWidth().testTag("personal_notes_input"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when (languageCode) {
                            "ur" -> "بند کریں"
                            "ar" -> "إغلاق"
                            else -> "Dismiss"
                        }
                    )
                }
            }
        }
    }
}

// Custom Aamal Detail Dialog
@Composable
fun CustomAamalDetailDialog(
    act: AamalCustomActivity,
    languageCode: String,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = act.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = when (languageCode) {
                        "ur" -> "ایک صارف مضاف عمل"
                        else -> "Custom user routine created locally."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Created date: ${act.dateAdded}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Close")
                    }
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier.weight(1f).testTag("delete_custom_activity_btn")
                    ) {
                        Text(
                            text = when (languageCode) {
                                "ur" -> "حذف کریں"
                                else -> "Delete"
                            }
                        )
                    }
                }
            }
        }
    }
}

// Category Struct
enum class AamalCategory {
    OBLIGATORY,
    NAWAFIL,
    ADHKAR,
    DUAS_ZIYARAT,
    MUSTAHAB,
    CUSTOM;

    fun getLabel(languageCode: String): String {
        return when (this) {
            OBLIGATORY -> when (languageCode) {
                "ur" -> "واجب نمازیں"
                "ar" -> "الصلوات الواجبة"
                "fa" -> "نمازهای واجب"
                "hi" -> "अनिवार्य नमाज़"
                else -> "Obligatory Salat"
            }
            NAWAFIL -> when (languageCode) {
                "ur" -> "مستحب نوافل"
                "ar" -> "النوافل اليومية"
                "fa" -> "نمازهای نافله"
                "hi" -> "नवाफिल नमाज़"
                else -> "Recommended Nawafil"
            }
            ADHKAR -> when (languageCode) {
                "ur" -> "روزانہ کے اذکار"
                "ar" -> "الأذكار والتسبيح"
                "fa" -> "اذکار روزانه"
                "hi" -> "दैनिक अज़कार"
                else -> "Daily Adhkar"
            }
            DUAS_ZIYARAT -> when (languageCode) {
                "ur" -> "دعائیں اور زیارات"
                "ar" -> "الأدعية والزيارات"
                "fa" -> "دعاها و زیارات"
                "hi" -> "दुआ और ज़ियारत"
                else -> "Daily Duas & Ziyarats"
            }
            MUSTAHAB -> when (languageCode) {
                "ur" -> "دیگر مستحب اعمال"
                "ar" -> "الأعمال المستحبة"
                "fa" -> "اعمال مستحبی"
                "hi" -> "अन्य मुस्तहब कार्य"
                else -> "Daily Mustahab Acts"
            }
            CUSTOM -> when (languageCode) {
                "ur" -> "ذاتی اعمال"
                "ar" -> "أعمالي الخاصة"
                "fa" -> "اعمال شخصی"
                "hi" -> "निजी कार्य"
                else -> "Custom Routine"
            }
        }
    }
}

// Class representing an Aamal routine checklist item details
data class AamalDef(
    val id: String,
    val category: AamalCategory,
    val titleEn: String,
    val titleUr: String,
    val titleAr: String,
    val subEn: String,
    val subUr: String,
    val arabic: String = "",
    val reference: String = "Mafatih al-Jinan",
    // Elaborated explanations based on authentic Shia (Ja'fari) Fiqh rulings
    val descEn: String,
    val descUr: String
) {
    fun getTitle(languageCode: String): String {
        return when (languageCode) {
            "ur" -> titleUr
            "ar" -> titleAr
            "fa" -> titleUr // Persian fallback to ur or fallback
            "hi" -> titleEn
            else -> titleEn
        }
    }

    fun getSubtitle(languageCode: String): String {
        return when (languageCode) {
            "ur" -> subUr
            else -> subEn
        }
    }

    fun getDetailedNotes(languageCode: String): String {
        return when (languageCode) {
            "ur" -> descUr
            else -> descEn
        }
    }

    fun getFarsiDescription(): String {
        return descEn
    }
}

// Helper algorithm to calculate consistent 100% completions streak over consecutive days.
fun calculateCurrentStreak(
    allCompletions: List<AamalCompletion>,
    customAamal: List<AamalCustomActivity>,
    defaultAamalCount: Int
): Int {
    if (allCompletions.isEmpty()) return 0
    
    val completedByDate = allCompletions
        .filter { it.isCompleted }
        .groupBy { it.dateString }
        
    val today = Calendar.getInstance()
    var streak = 0
    val checkCal = Calendar.getInstance()
    
    var isTodayPerfect = false
    val todayStr = sdf.format(today.time)
    val todayCompletionsCount = completedByDate[todayStr]?.size ?: 0
    val activeCustomCount = customAamal.filter { it.isEnabled }.size
    val totalRequiredToday = defaultAamalCount + activeCustomCount
    
    if (todayCompletionsCount >= totalRequiredToday && totalRequiredToday > 0) {
        isTodayPerfect = true
    }
    
    var currentCheck = today
    if (!isTodayPerfect) {
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        currentCheck = yesterday
    }
    
    while (true) {
        val checkStr = sdf.format(currentCheck.time)
        val dayCompletionsCount = completedByDate[checkStr]?.size ?: 0
        val activeCustoms = customAamal.filter { it.isEnabled }.size
        val reqCount = defaultAamalCount + activeCustoms
        
        if (dayCompletionsCount >= reqCount && reqCount > 0) {
            streak++
            currentCheck.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            break
        }
    }
    
    if (isTodayPerfect && streak == 0) {
        streak = 1
    } else if (isTodayPerfect) {
        streak++
    }
    
    return streak
}

// Static definition of Shia Daily Routine checklists
private fun getDefaultAamalList(): List<AamalDef> {
    return listOf(
        // Obligatory Salat
        AamalDef(
            id = "salat_fajr",
            category = AamalCategory.OBLIGATORY,
            titleEn = "Fajr Prayer",
            titleUr = "نمازِ فجر",
            titleAr = "صلاة الفجر",
            subEn = "2 Rak'ahs, Dawn to Sunrise",
            subUr = "2 رکعت، فجر کا واجب سحر",
            arabic = "صَلَّى اللَّه عَلَى مُحَمَّد وَآلِ مُحَمَّد",
            reference = "Wasa'il al-Shia",
            descEn = "Obligatory morning prayer consisting of 2 Rak'ahs. The time starts precisely at subh al-sadiq (true dawn) and culminates with sunrise.", 
            descUr = "صبح کی واجب نماز جس کی 2 رکعت ہیں۔ وقت صبح صادق سے شروع ہوتا ہے اور سورج طلوع ہونے تک رہتا ہے۔"
        ),
        AamalDef(
            id = "salat_dhuhr",
            category = AamalCategory.OBLIGATORY,
            titleEn = "Dhuhr Prayer",
            titleUr = "نمازِ ظہر",
            titleAr = "صلاة الظهر",
            subEn = "4 Rak'ahs, Midday",
            subUr = "4 رکعت، زوالِ آفتاب کے بعد",
            arabic = "",
            reference = "Wasa'il al-Shia",
            descEn = "Obligatory midday prayer consisting of 4 Rak'ahs. Under Ja'fari jurisprudence, its specific time begins when the sun is exactly at midday zenith.", 
            descUr = "زوالِ آفتاب کے بعد کی واجب نماز جس کی 4 رکعتیں ہیں۔"
        ),
        AamalDef(
            id = "salat_asr",
            category = AamalCategory.OBLIGATORY,
            titleEn = "Asr Prayer",
            titleUr = "نمازِ عصر",
            titleAr = "صلاة العصر",
            subEn = "4 Rak'ahs, Afternoon",
            subUr = "4 رکعت، ظہر کے بعد",
            arabic = "",
            reference = "Wasa'il al-Shia",
            descEn = "Obligatory afternoon prayer consisting of 4 Rak'ahs. Its time follows the dhuhr prayer and concludes before sunset.", 
            descUr = "عصر کی 4 رکعت واجب نماز جو ظہر پڑھنے کے بعد شروع ہوتی ہے۔"
        ),
        AamalDef(
            id = "salat_maghrib",
            category = AamalCategory.OBLIGATORY,
            titleEn = "Maghrib Prayer",
            titleUr = "نمازِ مغرب",
            titleAr = "صلاة المغرب",
            subEn = "3 Rak'ahs, Sunset",
            subUr = "3 رکعت، غروبِ آفتاب پر سرخی ختم ہونے پر",
            arabic = "",
            reference = "Wasa'il al-Shia",
            descEn = "Obligatory sunset prayer consisting of 3 Rak'ahs. Under Shia rule, the time begins once the eastern redness (Ghurub al-Shams) has fully vanished past the horizon, roughly 15-20 minutes after sunset.", 
            descUr = "3 رکعت واجب نماز۔ جعفرِی فقه کے مطابق سورج مغرب میں غروب ہو کر جب افق پر مشرقی سرخی ختم ہو جائے تب شروع ہوتی ہے۔"
        ),
        AamalDef(
            id = "salat_isha",
            category = AamalCategory.OBLIGATORY,
            titleEn = "Isha Prayer",
            titleUr = "نمازِ عشاء",
            titleAr = "صلاة العشاء",
            subEn = "4 Rak'ahs, Nightfall",
            subUr = "4 رکعت، مغرب کے بعد آدھی رات تک",
            arabic = "",
            reference = "Wasa'il al-Shia",
            descEn = "Obligatory night prayer consisting of 4 Rak'ahs. Can be performed right after Maghrib up until midnight.",
            descUr = "عشاء کی 4 رکعت واجب نماز جو مغرب کے بعد سے شرعی آدھی رات تک پڑھی جا سکتی ہے۔"
        ),

        // Nawafil
        AamalDef(
            id = "nafilah_fajr",
            category = AamalCategory.NAWAFIL,
            titleEn = "Fajr Nafilah",
            titleUr = "نافلہ فجر",
            titleAr = "نافلة الفجر",
            subEn = "2 Rak'ahs before Fajr",
            subUr = "سحر کی 2 رکعت مستحب نماز",
            arabic = "",
            reference = "Mafatih al-Jinan",
            descEn = "Highly recommended 2 Rak'ah prayers performed before the Fajr prayer. It yields tremendous spiritual blessings.",
            descUr = "نمازِ فجر سے پہلے پڑھی جانے والی 2 رکعت مستحب نماز۔ اس کے عظیم فضائل مروی ہیں۔"
        ),
        AamalDef(
            id = "nafilah_dhuhr",
            category = AamalCategory.NAWAFIL,
            titleEn = "Dhuhr Nafilah",
            titleUr = "نافلہ ظہر",
            titleAr = "نافلة الظهر",
            subEn = "8 Rak'ahs before Dhuhr",
            subUr = "ظہر سے پہلے 8 رکعت مستحب نماز",
            arabic = "",
            reference = "Mafatih al-Jinan",
            descEn = "Highly recommended 8 Rak'ahs (divided as 4 sets of 2 Rak'ah) performed before the Dhuhr prayer.",
            descUr = "ظہر سے پہلے 8 رکعت مستحب نماز، جو 2-2 رکعتوں کے چار حصوں میں پڑھی جاتی ہے۔"
        ),
        AamalDef(
            id = "nafilah_asr",
            category = AamalCategory.NAWAFIL,
            titleEn = "Asr Nafilah",
            titleUr = "نافلہ عصر",
            titleAr = "نافلة العصر",
            subEn = "8 Rak'ahs before Asr",
            subUr = "عصر سے پہلے 8 رکعت مستحب نماز",
            arabic = "",
            reference = "Mafatih al-Jinan",
            descEn = "Highly recommended 8 Rak'ahs (divided as 4 sets of 2 Rak'ah) performed before the Asr prayer.",
            descUr = "عصر سے پہلے پڑھی جانے والی 8 رکعت مستحب نوافل جو 2-2 کر کے چار سلام کے ساتھ پڑھی جاتی ہے۔"
        ),
        AamalDef(
            id = "nafilah_maghrib",
            category = AamalCategory.NAWAFIL,
            titleEn = "Maghrib Nafilah",
            titleUr = "نافلہ مغرب",
            titleAr = "نافلة المغرب",
            subEn = "4 Rak'ahs after Maghrib",
            subUr = "مغرب کے بعد 4 رکعت مستحب نماز",
            arabic = "",
            reference = "Mafatih al-Jinan",
            descEn = "Highly recommended 4 Rak'ah prayers performed after finishing the Maghrib prayer.",
            descUr = "مغرب کی نماز کے بعد پڑھی جانے والی 4 رکعت مستحب نماز جو 2-2 رکعت کر کے پڑھی جاتی ہے۔"
        ),
        AamalDef(
            id = "nafilah_isha",
            category = AamalCategory.NAWAFIL,
            titleEn = "Isha Nafilah (Watira)",
            titleUr = "نافلہ عشاء / وتیرا",
            titleAr = "نافلة العشاء (الوتيرة)",
            subEn = "2 Rak'ahs seated after Isha",
            subUr = "عشاء کے بعد بیٹھ کر 2 رکعت نماز",
            arabic = "",
            reference = "Mafatih al-Jinan",
            descEn = "Highly recommended 2 Rak'ah prayers performed sitting down after finishing the Isha obligatory prayer. It counts spiritually as 1 standing Rak'ah.",
            descUr = "نمازِ عشاء کے بعد بیٹھ کر پڑھی جانے والی 2 رکعت مستحب نماز جسے وتیرا کہا جاتا ہے۔"
        ),
        AamalDef(
            id = "nafilah_tahajjud",
            category = AamalCategory.NAWAFIL,
            titleEn = "Night Prayer (Salat al-Layl)",
            titleUr = "نمازِ شب (تہجد)",
            titleAr = "صلاة الليل",
            subEn = "11 Rak'ahs, Midnight to Dawn",
            subUr = "11 رکعت، نصف شب سے فجر تک",
            arabic = "",
            reference = "Mafatih al-Jinan",
            descEn = "The glorious Night Prayer consists of 11 Rak'ahs: 8 Rak'ahs of Nafilah (4 sets of 2), 2 Rak'ahs of Salat al-Shafa, and 1 Rak'ah of Salat al-Witr with long supplications in Qunut.",
            descUr = "آخری شب کی انتہائی فضیلت والی نماز، مجموعی طور پر 11 رکعتیں ہیں۔ 8 رکعت نافلہ شب، 2 رکعت شفع اور 1 رکعت نمازِ وتر جس کے قنوت میں استغفار کیا جاتا ہے۔"
        ),

        // Adhkar
        AamalDef(
            id = "adhkar_fatima",
            category = AamalCategory.ADHKAR,
            titleEn = "Tasbeeh of Lady Fatima (sa)",
            titleUr = "تسبیح حضرتِ فاطمہ زہراؑ",
            titleAr = "تسبيح السيدة فاطمة الزهراء عليها السلام",
            subEn = "34x Allahu Akbar, 33x Alhamd.., 33x Subhan..",
            subUr = "34 مرتبہ تکبیر، 33 مرتبہ حمد، 33 مرتبہ تسبیح",
            arabic = "الله أكبر (٣٤) ، الحمد لله (٣٣) ، سبحان الله (٣٣)",
            reference = "Al-Kafi",
            descEn = "The legendary spiritual gift of the Holy Prophet (saw) to his beloved daughter Hazrat Fatima-al-Zahra (sa). To be recited after each obligatory prayer to earn tremendous rewards.",
            descUr = "رسول اللہؐ کا حضرت بی بی فاطمہ زہراؑ کو دیا گیا عظیم تحفہ۔ ہر واجب نماز کے بعد اسے پڑھنا گناہوں کی بخشش کا باعث ہے۔"
        ),
        AamalDef(
            id = "adhkar_salawat",
            category = AamalCategory.ADHKAR,
            titleEn = "Durood / Salawat",
            titleUr = "درود شریف (صلوات)",
            titleAr = "الصلوات على محمد وآل محمد",
            subEn = "At least 100 times daily",
            subUr = "روزانہ کم از کم 100 مرتبہ",
            arabic = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَآلِ مُحَمَّدٍ",
            reference = "Mafatih al-Jinan",
            descEn = "Invoking divine blessings upon Prophet Muhammad (saw) and his pure Ahlul Bayt (as). Extremely powerful and removes all difficulties.",
            descUr = "حضرت محمد مصطفیٰؐ اور ان کی آلِ پاکؑ پر کثرت سے درود شریف بھیجنا دلی حاجات کی تکمیل کرتا ہے۔"
        ),
        AamalDef(
            id = "adhkar_ayat_kursi",
            category = AamalCategory.ADHKAR,
            titleEn = "Ayat-ul-Kursi",
            titleUr = "آیتہ الکرسی",
            titleAr = "آية الكرسي",
            subEn = "Recitation after prayers",
            subUr = "ہر نماز کے بعد تلاوت",
            arabic = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ...",
            reference = "Holy Quran (2:255-257)",
            descEn = "Recitation of the greatest verse of the Holy Quran, securing angelic protection and massive rewards after prayers.",
            descUr = "قرآن مجید کی انتہائی بابرکت آیت۔ ہر نماز کے بعد اس کی تلاوت شیطان کے اثرات و بلاؤں سے بچاتی ہے۔"
        ),

        // Duas & Ziyarat
        AamalDef(
            id = "dua_parents",
            category = AamalCategory.DUAS_ZIYARAT,
            titleEn = "Dua for Parents",
            titleUr = "والدین کے لیے دعا",
            titleAr = "دعاء للوالدين",
            subEn = "From Sahifa Sajjadiya",
            subUr = "صحیفہ سجادیہ سے دعا نمبر 24",
            arabic = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ عَبْدِكَ وَرَسُولِكَ...",
            reference = "Sahifa al-Sajjadiya (Dua 24)",
            descEn = "The magnificent supplication of Imam Zainul Abideen (as) for showing gratitude and seeking absolute forgiveness and mercy for one's parents.",
            descUr = "امام علی سجاد علیہ السلام کی اپنے والدین کی خیر و عافیت اور درجات کی بلندی کے لیے مانگی گئی خوبصورت دعا۔"
        ),
        AamalDef(
            id = "dua_ziyarat_weekday",
            category = AamalCategory.DUAS_ZIYARAT,
            titleEn = "Weekday Ziyarat of Masoomeen",
            titleUr = "زیارتِ ایامِ ہفتہ",
            titleAr = "زيارة المعصومين لأيام الأسبوع",
            subEn = "Special daily Ziyarat based on day",
            subUr = "ہفتے کے ہر دن کے لیے مخصوص معصوم کی زیارت",
            arabic = "",
            reference = "Mafatih al-Jinan",
            descEn = "Each day of the week is dedicated to the spiritual visits of specific Masoomeen (as). E.g., Saturday is for Holy Prophet (saw), Sunday for Imam Ali (as) & Lady Fatima (sa), Friday for Imam Mahdi (as).",
            descUr = "ہفتے کے ہر دن مخصوص معصومینِ پاکؑ کی منسوب زیارت پڑھنا قربتِ الہیٰ اور شفاعت کا وسیلہ ہے۔"
        ),

        // Mustahab
        AamalDef(
            id = "mustahab_sadaqah",
            category = AamalCategory.MUSTAHAB,
            titleEn = "Give Charity (Sadaqah)",
            titleUr = "صدقہ دینا",
            titleAr = "إعطاء الصدقة",
            subEn = "Spiritual cure & protection",
            subUr = "آفتوں کی دوری اور خیرات",
            arabic = "",
            reference = "Nahjul Balagha",
            descEn = "Giving even a very small amount of charity (Sadaqah) daily to help the poor and ward off evil calamities.",
            descUr = "روزانہ کی بنیاد پر غریبوں کی خوشحالی کے لیے بلا ضرورت صدقہ و خیرات نکالنا جس سے ستر بلائیں ٹلتی ہیں۔"
        ),
        AamalDef(
            id = "mustahab_quran",
            category = AamalCategory.MUSTAHAB,
            titleEn = "Recite Holy Quran",
            titleUr = "قرآن مجید کی تلاوت",
            titleAr = "تلاوة القرآن الكريم",
            subEn = "Minimum 50 verses daily",
            subUr = "روزانہ کم از کم 50 آیات",
            arabic = "",
            reference = "Al-Kafi",
            descEn = "Reciting at least 50 verses of the Holy Quran daily as recommended by Imam Jafar al-Sadiq (as).",
            descUr = "امام جعفر صادقؑ کی تاکید کے مطابق روزانہ تلاوتِ قرآنِ مجید کے کم از کم 50 مقامات کی تلاوت۔"
        )
    )
}
