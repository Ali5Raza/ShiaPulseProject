package com.example.ui

import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.view.ViewGroup
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import org.json.JSONObject

data class KarbalaEvent(
    val id: String,
    val dateHijri: String,
    val dateGregorian: String,
    val location: String,
    val locationEn: String,
    val locationUr: String,
    val title: String,
    val titleEn: String,
    val titleUr: String,
    val desc: String,
    val descEn: String,
    val descUr: String,
    val lat: Double,
    val lon: Double,
    val isMajor: Boolean = false
)

val karbalaTimelineEvents = listOf(
    KarbalaEvent(
        id = "medina_dep",
        dateHijri = "28 Rajab 60 AH",
        dateGregorian = "4 May 680 AD",
        location = "Medina",
        locationEn = "Medina",
        locationUr = "مدینہ",
        title = "Departure from Medina",
        titleEn = "Departure from Medina",
        titleUr = "مدینہ سے روانگی",
        desc = "Imam Hussain (as) leaves Medina after receiving news of Muawiyah's death and Yazid's demand for allegiance. He visits the grave of his brother Imam Hasan (as) and his mother Fatima (sa).",
        descEn = "Imam Hussain (as) leaves Medina after receiving news of Muawiyah's death and Yazid's demand for allegiance. He visits the grave of his brother Imam Hasan (as) and his mother Fatima (sa).",
        descUr = "امام حسینؑ نے یزید کی بیعت کے مطالبے پر مدینہ چھوڑنے کا فیصلہ کیا۔ آپؑ نے اپنے نانا رسول اللہؐ، والدہ سیدہ فاطمہؑ اور بھائی امام حسنؑ کی قبور پر الوداعی حاضری دی۔",
        lat = 24.4670, lon = 39.6111,
        isMajor = true
    ),
    KarbalaEvent(
        id = "mecca_arr",
        dateHijri = "8 Sha'ban 60 AH",
        dateGregorian = "14 May 680 AD",
        location = "Mecca",
        locationEn = "Mecca",
        locationUr = "مکہ مکرمہ",
        title = "Arrival in Mecca",
        titleEn = "Arrival in Mecca",
        titleUr = "مکہ میں آمد",
        desc = "Arrival in Mecca. Imam Hussain (as) stays in Mecca for approximately 4 months. He receives letters from Kufa inviting him to come and lead them.",
        descEn = "Arrival in Mecca. Imam Hussain (as) stays in Mecca for approximately 4 months. He receives letters from Kufa inviting him to come and lead them.",
        descUr = "امام حسینؑ مکہ پہنچے اور تقریباً 4 ماہ تک وہاں قیام کیا۔ اس دوران کوفہ سے آپؑ کو خطوط موصول ہوئے جن میں آپ کو کوفہ آنے کی دعوت دی گئی۔",
        lat = 21.3891, lon = 39.8579,
        isMajor = true
    ),
    KarbalaEvent(
        id = "mecca_dep",
        dateHijri = "8 Dhul-Hijjah 60 AH",
        dateGregorian = "9 Sep 680 AD",
        location = "Mecca",
        locationEn = "Mecca",
        locationUr = "مکہ",
        title = "Departure from Mecca",
        titleEn = "Departure from Mecca",
        titleUr = "مکہ سے روانگی",
        desc = "Imam Hussain (as) performs the farewell Tawaf and changes his intention from Hajj to Umrah to avoid bloodshed in the sanctuary. He leaves Mecca.",
        descEn = "Imam Hussain (as) performs the farewell Tawaf space changes his intention from Hajj to Umrah to avoid bloodshed in the sanctuary. He leaves Mecca.",
        descUr = "حرم مکہ کی حرمت بچانے کے لیے آپؑ نے حج کو عمرہ میں تبدیل کیا اور کوفہ کی جانب روانہ ہو گئے۔",
        lat = 21.4225, lon = 39.8262
    ),
    KarbalaEvent(
        id = "zuh_hasam",
        dateHijri = "15 Dhul-Hijjah 60 AH",
        dateGregorian = "16 Sep 680 AD",
        location = "Zuh Hasam",
        locationEn = "Zuh Hasam",
        locationUr = "ذو حسم",
        title = "News of Muslim bin Aqil",
        titleEn = "News of Muslim bin Aqil",
        titleUr = "مسلم بن عقیل کی شہادت کی خبر",
        desc = "Imam Hussain (as) receives the tragic news of Muslim bin Aqil's martyrdom in Kufa.",
        descEn = "Imam Hussain (as) receives the tragic news of Muslim bin Aqil's martyrdom in Kufa.",
        descUr = "امام حسینؑ کو یہاں حضرت مسلم بن عقیلؑ کی شہادت کی اندوہناک خبر ملی۔",
        lat = 25.5, lon = 41.5
    ),
    KarbalaEvent(
        id = "khuzaymiyah",
        dateHijri = "21 Dhul-Hijjah 60 AH",
        dateGregorian = "22 Sep 680 AD",
        location = "Khuzaymiyah",
        locationEn = "Khuzaymiyah",
        locationUr = "خزیمیہ",
        title = "Counsel to Turn Back",
        titleEn = "Counsel to Turn Back",
        titleUr = "واپسی کا مشورہ",
        desc = "Imam Hussain (as) meets a companion who advises him to turn back because the people of Kufa are treacherous.",
        descEn = "Imam Hussain (as) meets a companion who advises him to turn back because the people of Kufa are treacherous.",
        descUr = "یہاں ایک ساتھی نے آپؑ کو مشورہ دیا کہ کوفہ والے بے وفا ہیں، اس لیے آپ واپس لوٹ جائیں۔",
        lat = 28.1, lon = 43.2
    ),
    KarbalaEvent(
        id = "qadisiyyah",
        dateHijri = "27 Dhul-Hijjah 60 AH",
        dateGregorian = "28 Sep 680 AD",
        location = "Qadisiyyah",
        locationEn = "Qadisiyyah",
        locationUr = "قادسیہ",
        title = "Sermon Delivered",
        titleEn = "Sermon Delivered",
        titleUr = "قادسیہ میں خطبہ",
        desc = "Imam Hussain (as) delivers a deeply moving sermon to his companions.",
        descEn = "Imam Hussain (as) delivers a deeply moving sermon to his companions.",
        descUr = "امام حسینؑ نے اپنے ساتھیوں سے ایک ولولہ انگیز اور تاریخی خطبہ ارشاد فرمایا۔",
        lat = 31.579, lon = 44.252
    ),
    KarbalaEvent(
        id = "hurr_meet",
        dateHijri = "3 Muharram 61 AH",
        dateGregorian = "3 Oct 680 AD",
        location = "Near Karbala",
        locationEn = "Near Karbala",
        locationUr = "کربلا کے قریب",
        title = "Intercepted by Hurr",
        titleEn = "Intercepted by Hurr",
        titleUr = "حر کی فوج سے سامنا",
        desc = "Imam Hussain (as) and his companions are surrounded by Hurr ibn Yazid al-Riyahi's forces (1,000 soldiers).",
        descEn = "Imam Hussain (as) and his companions are surrounded by Hurr ibn Yazid al-Riyahi's forces (1,000 soldiers).",
        descUr = "امام حسینؑ اور ان کے قافلے کو حر ابن یزید ریاحی کے ایک ہزار کے لشکر نے روکا۔",
        lat = 32.5, lon = 44.1
    ),
    KarbalaEvent(
        id = "karbala_arr",
        dateHijri = "4 Muharram 61 AH",
        dateGregorian = "4 Oct 680 AD",
        location = "Karbala",
        locationEn = "Karbala",
        locationUr = "کربلا",
        title = "Arrival in Karbala",
        titleEn = "Arrival in Karbala",
        titleUr = "کربلا میں آمد",
        desc = "Imam Hussain (as) is forced to stop at the barren plains of Karbala. The siege begins.",
        descEn = "Imam Hussain (as) is forced to stop at the barren plains of Karbala. The siege begins.",
        descUr = "امام حسینؑ کو مجبور کیا گیا کہ وہ کربلا کے ویران میدان میں پڑاؤ ڈالیں۔ یہاں سے محاصرہ شروع ہوتا ہے۔",
        lat = 32.6160, lon = 44.0249,
        isMajor = true
    ),
    KarbalaEvent(
        id = "karbala_water",
        dateHijri = "7 Muharram 61 AH",
        dateGregorian = "7 Oct 680 AD",
        location = "Karbala",
        locationEn = "Karbala",
        locationUr = "کربلا",
        title = "Water Cut Off",
        titleEn = "Water Cut Off",
        titleUr = "پانی کی بندش",
        desc = "The siege intensifies. Water from the Euphrates is cut off from Imam Hussain (as) and his companions.",
        descEn = "The siege intensifies. Water from the Euphrates is cut off from Imam Hussain (as) and his companions.",
        descUr = "دشمن کا محاصرہ سخت ہو گیا اور امام حسینؑ کے خیموں کا پانی بند کر دیا گیا۔",
        lat = 32.6170, lon = 44.0259
    ),
    KarbalaEvent(
        id = "karbala_ashura",
        dateHijri = "10 Muharram 61 AH",
        dateGregorian = "10 Oct 680 AD",
        location = "Karbala",
        locationEn = "Karbala",
        locationUr = "کربلا",
        title = "The Day of Ashura",
        titleEn = "The Day of Ashura",
        titleUr = "عاشورہ کا دن",
        desc = "The Battle of Karbala (Ashura). Imam Hussain (as) and his 72 companions are martyred defending Imamate and Islam.",
        descEn = "The Battle of Karbala (Ashura). Imam Hussain (as) and his 72 companions are martyred defending Imamate and Islam.",
        descUr = "روزِ عاشورہ۔ امام حسینؑ اور ان کے 72 باوفا ساتھیوں نے دینِ اسلام کی بقا کے لیے عظیم قربانی پیش کی اور شہید ہوئے۔",
        lat = 32.6180, lon = 44.0269,
        isMajor = true
    )
)

class KarbalaWebInterface(
    private val onMarkerClick: (String) -> Unit
) {
    @JavascriptInterface
    fun postMessage(message: String) {
        try {
            val json = JSONObject(message)
            if (json.has("action") && json.getString("action") == "markerClick") {
                onMarkerClick(json.getString("id"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun KarbalaMapScreen(currentLang: String, onBack: () -> Unit) {
    var selectedEventId by remember { mutableStateOf<String?>(null) }
    var detailEvent by remember { mutableStateOf<KarbalaEvent?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    var showEducationalInfo by remember { mutableStateOf(false) }

    val leafHtml = """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
        <style>
            html, body, #map {
                height: 100%;
                width: 100%;
                margin: 0;
                padding: 0;
            }
            .custom-icon {
                background: none;
                border: none;
            }
            .pin-circle {
                width: 20px;
                height: 20px;
                background-color: #E65100;
                border-radius: 50%;
                border: 3px solid white;
                box-shadow: 0 0 5px rgba(0,0,0,0.5);
            }
            .pin-circle.major {
                background-color: #D32F2F;
                width: 24px;
                height: 24px;
            }
            .pin-circle.selected {
                border-color: #FFEA00;
                transform: scale(1.3);
                transition: 0.2s;
            }
        </style>
    </head>
    <body style="margin: 0;">
        <div id="map"></div>
        <script>
            var map = L.map('map', {zoomControl: false}).setView([27.0, 42.0], 5);
            
            L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
                attribution: '&copy; OpenStreetMap contributors'
            }).addTo(map);

            var markers = {};
            var latlngs = [];

            function initMarkers(dataStr) {
                var data = JSON.parse(dataStr);
                data.forEach(function(ev) {
                    var isMajorClass = ev.isMajor ? " major" : "";
                    var customIcon = L.divIcon({
                        className: 'custom-icon',
                        html: '<div id="pin-'+ev.id+'" class="pin-circle'+isMajorClass+'"></div>',
                        iconSize: [20, 20],
                        iconAnchor: [10, 10]
                    });
                    
                    var marker = L.marker([ev.lat, ev.lon], {icon: customIcon, title: ev.title})
                        .addTo(map)
                        .on('click', function(e) {
                            selectMarker(ev.id);
                            if (window.AndroidBridge) {
                                window.AndroidBridge.postMessage(JSON.stringify({action: 'markerClick', id: ev.id}));
                            }
                        });
                    markers[ev.id] = marker;
                    latlngs.push([ev.lat, ev.lon]);
                });

                if (latlngs.length > 0) {
                    var polyline = L.polyline(latlngs, {color: '#8E44AD', weight: 4, dashArray: '5, 10'}).addTo(map);
                    map.fitBounds(polyline.getBounds(), {padding: [30, 30]});
                }
            }

            function selectMarker(id) {
                // reset all
                for (var key in markers) {
                    var el = document.getElementById('pin-'+key);
                    if (el) el.classList.remove('selected');
                }
                var selectedEl = document.getElementById('pin-'+id);
                if (selectedEl) selectedEl.classList.add('selected');
                
                if (markers[id]) {
                    map.flyTo(markers[id].getLatLng(), 8, {duration: 1.5});
                }
            }
        </script>
    </body>
    </html>
    """.trimIndent()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // App Bar
        TopAppBar(
            title = {
                Text(
                    text = if (currentLang == "ur") "کربلا کا سفر" else "Karbala Journey",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            actions = {
                IconButton(onClick = { showEducationalInfo = true }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Educational Info",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        // Map Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.40f)
                .background(Color.Gray)
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        
                        addJavascriptInterface(KarbalaWebInterface(
                            onMarkerClick = { id ->
                                scope.launch {
                                    selectedEventId = id
                                    val index = karbalaTimelineEvents.indexOfFirst { it.id == id }
                                    if (index != -1) {
                                        listState.animateScrollToItem(index)
                                    }
                                }
                            }
                        ), "AndroidBridge")
                        
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                val jsonArray = org.json.JSONArray()
                                karbalaTimelineEvents.forEach { ev ->
                                    val obj = org.json.JSONObject()
                                    obj.put("id", ev.id)
                                    obj.put("lat", ev.lat)
                                    obj.put("lon", ev.lon)
                                    obj.put("title", ev.title)
                                    obj.put("isMajor", ev.isMajor)
                                    jsonArray.put(obj)
                                }
                                view?.evaluateJavascript("initMarkers('${jsonArray.toString()}');", null)
                            }
                        }
                        
                        loadDataWithBaseURL("https://leaflet", leafHtml, "text/html", "UTF-8", null)
                        webViewRef = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Timeline Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.60f)
        ) {
            Text(
                text = if (currentLang == "ur") "سفر نامہ" else "Timeline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.primary
            )
            
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                itemsIndexed(karbalaTimelineEvents) { index, event ->
                    val isSelected = event.id == selectedEventId
                    val titleText = if (currentLang == "ur") event.titleUr else event.titleEn
                    val locText = if (currentLang == "ur") event.locationUr else event.locationEn
                    val descText = if (currentLang == "ur") event.descUr else event.descEn

                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                        // Timeline stem & circle
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(30.dp).fillMaxHeight()
                        ) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(if (event.isMajor) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                            )
                            if (index < karbalaTimelineEvents.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .weight(1f)
                                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clickable {
                                    selectedEventId = event.id
                                    webViewRef?.evaluateJavascript("selectMarker('${event.id}');", null)
                                    detailEvent = event
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = locText,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = titleText,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = event.dateHijri,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = descText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog
    if (detailEvent != null) {
        AlertDialog(
            onDismissRequest = { detailEvent = null },
            title = {
                Text(if (currentLang == "ur") detailEvent!!.titleUr else detailEvent!!.titleEn)
            },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentLang == "ur") detailEvent!!.locationUr else detailEvent!!.locationEn,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${detailEvent!!.dateHijri} / ${detailEvent!!.dateGregorian}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (currentLang == "ur") detailEvent!!.descUr else detailEvent!!.descEn,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { detailEvent = null }) {
                    Text(if (currentLang == "ur") "بند کریں" else "Close")
                }
            }
        )
    }

    if (showEducationalInfo) {
        AlertDialog(
            onDismissRequest = { showEducationalInfo = false },
            title = {
                Text(
                    text = if (currentLang == "ur") "کربلا: تاریخی حقائق" else "Karbala: Historical Context",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = if (currentLang == "ur") "پس منظر" else "Background",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = if (currentLang == "ur") "امام حسینؑ نے مدینہ اس لیے چھوڑا کیونکہ وہ یزید جیسے ظالم کی بیعت نہیں کر سکتے تھے۔ آپؑ کا مقصد امت کی اصلاح اور دین کی بقا تھا۔" 
                        else "Imam Hussain (as) left Medina refusing allegiance to Yazid, an unjust ruler. His mission was to reform the ummah and save Islam.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (currentLang == "ur") "شہدائے کربلا" else "Companions (The 72)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = if (currentLang == "ur") "آپؑ کے ساتھ 72 وفادار ساتھی تھے، جن میں آپ کا خاندان (بنو ہاشم) اور جان نثار اصحاب شامل تھے۔ ان میں حضرت عباسؑ، علی اکبرؑ، قاسمؑ، اور 6 ماہ کے علی اصغرؑ شامل تھے۔" 
                        else "He was accompanied by 72 loyal companions, including his family (Banu Hashim) and devoted followers. Key figures include Hazrat Abbas (as), Ali Akbar (as), Qasim (as), and the 6-month-old Ali Asghar (as).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (currentLang == "ur") "دشمن کی فوج" else "Enemy Forces",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = if (currentLang == "ur") "یزید اور عبیداللہ ابن زیاد کے حکم پر، عمر بن سعد کی قیادت میں 30،000 سے زیادہ کثیر فوج نے کربلا میں امامؑ کا محاصرہ کیا۔" 
                        else "Commanded by Umar ibn Sa'd, over 30,000 soldiers besieged the Imam's camp under the orders of Yazid and Ubaydullah ibn Ziyad.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showEducationalInfo = false }) {
                    Text(if (currentLang == "ur") "سمجھ گیا" else "Understood")
                }
            }
        )
    }
}
