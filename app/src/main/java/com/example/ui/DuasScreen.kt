package com.example.ui

import android.media.MediaPlayer
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.LocalizationUtility
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import org.json.JSONArray

data class DuaLine(
    val arabic: String,
    val translation: String, // English translation
    val transliteration: String,
    val urduTranslation: String? = null,
    val farsiTranslation: String? = null
)

data class ShiaDua(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val relevance: String,
    val audioUrl: String,
    val lines: List<DuaLine>
)

// Real-time translation client using Gemini 3.5 Flash API
suspend fun translateWithGemini(arabicText: String, englishText: String, targetLanguage: String): String = withContext(Dispatchers.IO) {
    val apiKey = com.example.BuildConfig.GEMINI_API_KEY
    if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
        return@withContext "Error: Please add GEMINI_API_KEY in the Secrets Panel."
    }

    val client = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    // Prompt optimized for direct, beautiful Quranic/Dua translation
    val prompt = "Translate this line from an Islamic prayer or supplication into the language \"$targetLanguage\". Provide ONLY the translated text. Do not provide any introduction, explanation, transliteration, phonetic notation, or notes. Just output the translation itself.\nArabic Verse: \"$arabicText\"\nEnglish Meaning: \"$englishText\""

    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

    val requestJson = JSONObject().apply {
        put("contents", JSONArray().put(JSONObject().apply {
            put("parts", JSONArray().put(JSONObject().apply {
                put("text", prompt)
            }))
        }))
    }

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val body = requestJson.toString().toRequestBody(mediaType)

    val request = Request.Builder()
        .url(url)
        .post(body)
        .build()

    try {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return@withContext "Error: API responded with status code ${response.code}"
            }
            val responseString = response.body?.string() ?: return@withContext "Error: Empty response"
            val jsonObject = JSONObject(responseString)
            val candidates = jsonObject.getJSONArray("candidates")
            val candidate = candidates.getJSONObject(0)
            val content = candidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val resultText = parts.getJSONObject(0).getString("text").trim()
            return@withContext resultText
        }
    } catch (e: Exception) {
        return@withContext "Error: ${e.message ?: "Failed to connect to translation service"}"
    }
}

fun loadDuasFromAssets(context: android.content.Context): List<ShiaDua> {
    try {
        val inputStream = context.assets.open("duas.json")
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val jsonArray = org.json.JSONArray(jsonString)
        val list = mutableListOf<ShiaDua>()
        for (i in 0 until jsonArray.length()) {
            val duaObj = jsonArray.getJSONObject(i)
            val linesArray = duaObj.getJSONArray("lines")
            val linesList = mutableListOf<DuaLine>()
            for (j in 0 until linesArray.length()) {
                val lineObj = linesArray.getJSONObject(j)
                linesList.add(
                    DuaLine(
                        arabic = lineObj.getString("arabic"),
                        translation = lineObj.getString("translation"),
                        transliteration = lineObj.getString("transliteration"),
                        urduTranslation = lineObj.optString("urduTranslation").takeIf { it.isNotEmpty() },
                        farsiTranslation = lineObj.optString("farsiTranslation").takeIf { it.isNotEmpty() }
                    )
                )
            }
            list.add(
                ShiaDua(
                    id = duaObj.getString("id"),
                    title = duaObj.getString("title"),
                    category = duaObj.getString("category"),
                    description = duaObj.getString("description"),
                    relevance = duaObj.getString("relevance"),
                    audioUrl = duaObj.getString("audioUrl"),
                    lines = linesList
                )
            )
        }
        if (list.isNotEmpty()) {
            return list
        }
    } catch (e: Exception) {
        android.util.Log.e("DuasScreen", "Error loading duas.json: ${e.message}", e)
    }
    return emptyList()
}

val shiaDuasList: List<ShiaDua> = emptyList()

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DuasScreen(
    viewModel: PrayerViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onOpenZiyaratsMap: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val currentLang by viewModel.appLanguage.collectAsState()
    val globalAppFont by viewModel.appFont.collectAsState()
    val baseAppFontFamily = com.example.ui.theme.getAppFontFamily(globalAppFont, currentLang)
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var activeReadingDua by remember { mutableStateOf<ShiaDua?>(null) }

    val loadedDuas = remember {
        val fromJson = loadDuasFromAssets(context)
        android.util.Log.d("DuasScreen", "Loaded ${fromJson.size} Duas from assets (duas.json).")
        val filtered = fromJson.filter { it.id != "ashura" && it.id != "waritha" }
        if (filtered.isNotEmpty()) {
            filtered
        } else {
            android.util.Log.e("DuasScreen", "Failed to load Duas from assets! Falling back to static dues list.")
            shiaDuasList
        }
    }

    val favoriteDuaIds by viewModel.favoriteDuaIds.collectAsState()
    val categories = listOf("All", "Famed Supplications", "Weekly Supplications", "Daily Supplications", "Holy Ziyarats", "Bookmarked")

    // Filter list based on checks
    val filteredDuas = remember(loadedDuas, searchQuery, selectedCategory, favoriteDuaIds) {
        loadedDuas.filter { dua ->
            val matchesSearch = dua.title.contains(searchQuery, ignoreCase = true) || 
                            dua.description.contains(searchQuery, ignoreCase = true)
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Bookmarked" -> favoriteDuaIds.contains(dua.id)
                else -> dua.category == selectedCategory
            }
            matchesSearch && matchesCategory
        }
    }

    if (activeReadingDua != null) {
        DuaReadingView(
            viewModel = viewModel,
            dua = activeReadingDua!!,
            onBack = { activeReadingDua = null }
        )
    } else {
        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                LocalizationUtility.get("duas_ziyarats", currentLang),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                LocalizationUtility.get("duas_ziyarats_sub", currentLang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(LocalizationUtility.get("search_duas", currentLang)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                            }
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Category Chips List Row
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            categories.take(3).forEach { cat ->
                                val isSelected = cat == selectedCategory
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = {
                                        val localizedCat = when (cat) {
                                            "All" -> LocalizationUtility.get("cat_all", currentLang)
                                            "Famed Supplications" -> LocalizationUtility.get("cat_famed", currentLang)
                                            "Weekly Supplications" -> LocalizationUtility.get("cat_weekly", currentLang)
                                            "Daily Supplications" -> LocalizationUtility.get("cat_daily", currentLang)
                                            "Holy Ziyarats" -> LocalizationUtility.get("cat_ziyarats", currentLang)
                                            "Bookmarked" -> if (currentLang == "ur") "محفوظ شدہ" else "Bookmarked"
                                            else -> cat
                                        }
                                        Text(localizedCat)
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }
                    }
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            categories.drop(3).forEach { cat ->
                                val isSelected = cat == selectedCategory
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = {
                                        val localizedCat = when (cat) {
                                            "All" -> LocalizationUtility.get("cat_all", currentLang)
                                            "Famed Supplications" -> LocalizationUtility.get("cat_famed", currentLang)
                                            "Weekly Supplications" -> LocalizationUtility.get("cat_weekly", currentLang)
                                            "Daily Supplications" -> LocalizationUtility.get("cat_daily", currentLang)
                                            "Holy Ziyarats" -> LocalizationUtility.get("cat_ziyarats", currentLang)
                                            "Bookmarked" -> if (currentLang == "ur") "محفوظ شدہ" else "Bookmarked"
                                            else -> cat
                                        }
                                        Text(localizedCat)
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }
                    }
                }

                // Grid/List elements containing available files
                if (filteredDuas.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "No Results",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                "No matched duas found.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        if (selectedCategory == "Holy Ziyarats") {
                            item {
                                Card(
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                    ),
                                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenZiyaratsMap?.invoke() }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(18.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = "Map Button",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (currentLang == "ur") "نقشہ اور جغرافیہ" else if (currentLang == "ar") "عرض الخريطة التفاعلية" else "Interactive Ziyarats Map",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (currentLang == "ur") "مقدس مزارات اور تاریخی مقامات کو دنیا کے نقشے پر دیکھیں" else if (currentLang == "ar") "تصفّح العتبات المقدسة والمواقع التاريخية على الخريطة" else "Explore holy shrines & historical sites on the global map.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = "Go to Map",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        items(filteredDuas) { dua ->
                            val isFavorite = favoriteDuaIds.contains(dua.id)
                            DuaListTile(
                                dua = dua,
                                isFavorite = isFavorite,
                                onFavoriteToggle = { viewModel.toggleDuaFavorite(dua.id) },
                                onSelect = { activeReadingDua = dua }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DuaListTile(
    dua: ShiaDua,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = dua.category,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = dua.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = dua.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = dua.relevance,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                
                Button(
                    onClick = onSelect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Read Now", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Filled.Book,
                        contentDescription = "Go",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuaReadingView(
    viewModel: PrayerViewModel,
    dua: ShiaDua,
    onBack: () -> Unit
) {
    val favoriteDuaIds by viewModel.favoriteDuaIds.collectAsState()
    val currentLang by viewModel.appLanguage.collectAsState()
    val appFont by viewModel.appFont.collectAsState()
    val baseAppFontFamily = com.example.ui.theme.getAppFontFamily(appFont, currentLang)
    val isFavorite = favoriteDuaIds.contains(dua.id)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val duaBookmarks by viewModel.duaBookmarks.collectAsState()
    val savedLineIndex = duaBookmarks[dua.id]
    val listState = androidx.compose.foundation.lazy.rememberLazyListState(initialFirstVisibleItemIndex = savedLineIndex ?: 0)

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                if (index > 0 || (savedLineIndex != null && index == 0)) {
                    viewModel.setDuaBookmark(dua.id, index)
                }
            }
    }

    val fontScale = remember {
        val prefs = context.getSharedPreferences("shia_tracker_prefs", android.content.Context.MODE_PRIVATE)
        prefs.getFloat("arabic_urdu_font_scale", 1.0f)
    }

    // Preferences and sizing state properties
    var arabicFontSize by remember { mutableStateOf(24f * fontScale) }
    var translationFontSize by remember { mutableStateOf(16f * fontScale) }
    
    var showArabic by remember { mutableStateOf(true) }
    var showTranslation by remember { mutableStateOf(true) }
    var showTransliteration by remember { mutableStateOf(true) }

    // Multi-language Translation State Settings
    var selectedTargetLanguage by remember { mutableStateOf("English") }
    val aiTranslations = remember { mutableStateMapOf<String, String>() }
    val aiTranslatingState = remember { mutableStateMapOf<String, Boolean>() }
    var isTranslatingAll by remember { mutableStateOf(false) }

    val languages = listOf(
        "English", 
        "Urdu", 
        "Persian", 
        "Hindi", 
        "Gujarati", 
        "French", 
        "Spanish", 
        "Turkish"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(dua.title, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleDuaFavorite(dua.id) }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Font Sizes control
                    IconButton(onClick = { if (arabicFontSize > 18f) arabicFontSize -= 2f }) {
                        Text("A-", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { if (arabicFontSize < 40f) arabicFontSize += 2f }) {
                        Text("A+", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Display Column configurations layout row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Filters:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ElevatedAssistChip(
                        onClick = { showArabic = !showArabic },
                        label = { Text("Ar") },
                        colors = if (showArabic) AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else AssistChipDefaults.assistChipColors()
                    )
                    ElevatedAssistChip(
                        onClick = { showTransliteration = !showTransliteration },
                        label = { Text("Tr") },
                        colors = if (showTransliteration) AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.secondaryContainer) else AssistChipDefaults.assistChipColors()
                    )
                    ElevatedAssistChip(
                        onClick = { showTranslation = !showTranslation },
                        label = { Text("En") },
                        colors = if (showTranslation) AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer) else AssistChipDefaults.assistChipColors()
                    )
                }
            }

            // Real-time Dynamic Language Selection Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Translation Language:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    languages.forEach { lang ->
                        val isSelected = selectedTargetLanguage == lang
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTargetLanguage = lang },
                            label = { Text(lang) },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            // AI Bulk/Full-Dua translation panel
            if (selectedTargetLanguage != "English") {
                val untranslatedIndices = dua.lines.indices.filter { idx ->
                    val hasOffline = when (selectedTargetLanguage) {
                        "Urdu" -> dua.lines[idx].urduTranslation != null
                        "Persian" -> dua.lines[idx].farsiTranslation != null
                        else -> false
                    }
                    !hasOffline && !aiTranslations.containsKey("${idx}_$selectedTargetLanguage")
                }

                if (untranslatedIndices.isNotEmpty()) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isTranslatingAll = true
                                untranslatedIndices.forEach { idx ->
                                    val line = dua.lines[idx]
                                    aiTranslatingState["${idx}_$selectedTargetLanguage"] = true
                                    val res = translateWithGemini(line.arabic, line.translation, selectedTargetLanguage)
                                    aiTranslations["${idx}_$selectedTargetLanguage"] = res
                                    aiTranslatingState["${idx}_$selectedTargetLanguage"] = false
                                }
                                isTranslatingAll = false
                            }
                        },
                        enabled = !isTranslatingAll,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        if (isTranslatingAll) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.8.dp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("A.I. Translating Prayer Lines...", style = MaterialTheme.typography.labelLarge)
                        } else {
                            Text("✨ AI Live Translate Full Supplication into $selectedTargetLanguage", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            HorizontalDivider()

            // Main line list content scroller
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                itemsIndexed(dua.lines) { index, line ->
                    val isBookmarkedLine = savedLineIndex == index
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isBookmarkedLine) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                
                                IconButton(
                                    onClick = { 
                                        if (isBookmarkedLine) {
                                            viewModel.setDuaBookmark(dua.id, -1) // remove bookmark
                                        } else {
                                            viewModel.setDuaBookmark(dua.id, index) 
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isBookmarkedLine) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Mark as resume point",
                                        tint = if (isBookmarkedLine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            if (showArabic) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = line.arabic,
                                    fontSize = arabicFontSize.sp,
                                    fontFamily = baseAppFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End
                                )
                            }

                            if (showTransliteration) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = line.transliteration,
                                    fontSize = (arabicFontSize * 0.65f).sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (showTranslation) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = line.translation,
                                    fontSize = translationFontSize.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Dynamic Multi-language translation display block
                            if (selectedTargetLanguage != "English") {
                                val offlineTranslation = when (selectedTargetLanguage) {
                                    "Urdu" -> line.urduTranslation
                                    "Persian" -> line.farsiTranslation
                                    else -> null
                                }

                                val aiTranslation = aiTranslations["${index}_$selectedTargetLanguage"]
                                val isTranslating = aiTranslatingState["${index}_$selectedTargetLanguage"] ?: false

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(8.dp))

                                if (offlineTranslation != null) {
                                    // High-quality offline pre-packaged rendering
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    "Offline $selectedTargetLanguage",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = offlineTranslation,
                                            fontSize = (translationFontSize + 2f).sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.End,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                } else if (aiTranslation != null) {
                                    // Dynamically fetched A.I. rendering
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.tertiary,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    "A.I. $selectedTargetLanguage",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onTertiary,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        
                                        val isrtl = selectedTargetLanguage == "Urdu" || selectedTargetLanguage == "Persian"
                                        Text(
                                            text = aiTranslation,
                                            fontSize = (translationFontSize + if (isrtl) 2f else 0f).sp,
                                            fontWeight = if (isrtl) FontWeight.Bold else FontWeight.Medium,
                                            textAlign = if (isrtl) TextAlign.End else TextAlign.Start,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                } else {
                                    // Translation not fetched yet -> Action trigger
                                    if (isTranslating) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.5.dp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                "Fetching A.I. translation...",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    aiTranslatingState["${index}_$selectedTargetLanguage"] = true
                                                    val res = translateWithGemini(line.arabic, line.translation, selectedTargetLanguage)
                                                    aiTranslations["${index}_$selectedTargetLanguage"] = res
                                                    aiTranslatingState["${index}_$selectedTargetLanguage"] = false
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                                contentColor = MaterialTheme.colorScheme.secondary
                                            ),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Translate line",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "✨ Translate Line to $selectedTargetLanguage with A.I.",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Source Credit Reference",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Supplication & Ziyarat text are credit attributed to duas.org.",
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