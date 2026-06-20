package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*

@Composable
fun HajjUmrahTab(viewModel: PrayerViewModel, languageCode: String) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = if (languageCode == "ur") listOf("عمرہ", "حج", "نقشہ", "معلومات") else listOf("Umrah", "Hajj", "Map", "Info")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> UmrahGuide(viewModel, languageCode)
                1 -> HajjGuide(viewModel, languageCode)
                2 -> HajjMapVisualization(languageCode)
                3 -> InfoGuide(languageCode)
            }
        }
    }
}

@Composable
fun HajjMapVisualization(languageCode: String) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = if (languageCode == "ur") "حج کے مقامات اور سفر کا نقشہ" else "Hajj Rituals Journey Map",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        BoxWithConstraints(modifier = Modifier.fillMaxSize().weight(1f)) {
            val w = maxWidth
            val h = maxHeight
            
            val wPx = constraints.maxWidth.toFloat()
            val hPx = constraints.maxHeight.toFloat()
            
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary
            val tertiaryColor = MaterialTheme.colorScheme.tertiary
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val meccaPos = androidx.compose.ui.geometry.Offset(wPx * 0.2f, hPx * 0.15f)
                val minaPos = androidx.compose.ui.geometry.Offset(wPx * 0.75f, hPx * 0.35f)
                val muzdalifahPos = androidx.compose.ui.geometry.Offset(wPx * 0.5f, hPx * 0.6f)
                val arafatPos = androidx.compose.ui.geometry.Offset(wPx * 0.8f, hPx * 0.85f)
                
                // Draw line Mecca -> Mina
                drawLine(
                    color = primaryColor.copy(alpha = 0.6f),
                    start = meccaPos,
                    end = minaPos,
                    strokeWidth = 6.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(20f, 20f))
                )

                // Draw line Mina -> Arafat
                drawLine(
                    color = secondaryColor.copy(alpha = 0.6f),
                    start = minaPos,
                    end = arafatPos,
                    strokeWidth = 6.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(20f, 20f))
                )

                // Draw line Arafat -> Muzdalifah
                drawLine(
                    color = tertiaryColor.copy(alpha = 0.6f),
                    start = arafatPos,
                    end = muzdalifahPos,
                    strokeWidth = 6.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(20f, 20f))
                )

                // Draw line Muzdalifah -> Mina
                drawLine(
                    color = primaryColor.copy(alpha = 0.6f),
                    start = muzdalifahPos,
                    end = minaPos,
                    strokeWidth = 6.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(20f, 20f))
                )

                // Mina -> Mecca
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(minaPos.x, minaPos.y)
                    quadraticBezierTo(wPx * 0.3f, hPx * 0.45f, meccaPos.x, meccaPos.y)
                }
                drawPath(
                    path = path,
                    color = secondaryColor.copy(alpha = 0.6f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 6.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(20f, 20f))
                    )
                )
            }
            
            // Add nodes
            NodeContent(
                x = w * 0.2f, y = h * 0.15f,
                title = if (languageCode == "ur") "مکہ" else "Mecca",
                desc = if (languageCode == "ur") "8 و 10 ذوالحجہ" else "8th & 10th",
                color = primaryColor
            )
            // Mina
            NodeContent(
                x = w * 0.75f, y = h * 0.35f,
                title = if (languageCode == "ur") "منیٰ" else "Mina",
                desc = if (languageCode == "ur") "8, 10-12 ذوالحجہ" else "8th, 10-12th",
                color = secondaryColor
            )
            // Muzdalifah
            NodeContent(
                x = w * 0.5f, y = h * 0.6f,
                title = if (languageCode == "ur") "مزدلفہ" else "Muzdalifah",
                desc = if (languageCode == "ur") "9 کی رات" else "9th Night",
                color = primaryColor
            )
            // Arafat
            NodeContent(
                x = w * 0.8f, y = h * 0.85f,
                title = if (languageCode == "ur") "عرفات" else "Arafat",
                desc = if (languageCode == "ur") "9 ذوالحجہ" else "9th",
                color = tertiaryColor
            )
        }

        // Legend
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (languageCode == "ur") "سفر کی ترتیب:" else "Journey Sequence:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                val seq = if (languageCode == "ur") "1️⃣ مکہ سے منیٰ\n2️⃣ منیٰ سے عرفات (9 ذوالحجہ)\n3️⃣ عرفات سے مزدلفہ (مغرب کے بعد)\n4️⃣ مزدلفہ سے منیٰ (10 ذوالحجہ کی صبح)\n5️⃣ منیٰ سے مکہ (طواف کیلئے) اور واپسی" 
                          else "1️⃣ Mecca to Mina (8th)\n2️⃣ Mina to Arafat (9th Daylight)\n3️⃣ Arafat to Muzdalifah (9th Night)\n4️⃣ Muzdalifah to Mina (10th Dawn)\n5️⃣ Mina to Mecca (Tawaf) & Return to Mina"
                Text(seq, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun NodeContent(x: androidx.compose.ui.unit.Dp, y: androidx.compose.ui.unit.Dp, title: String, desc: String, color: Color) {
    Box(
        modifier = Modifier
            .offset(x = x - 45.dp, y = y - 45.dp) // center approx 90dp width
            .size(90.dp)
            .background(color, androidx.compose.foundation.shape.CircleShape)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(desc, color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun UmrahGuide(viewModel: PrayerViewModel, languageCode: String) {
    val hajjUmrahItems by viewModel.allHajjUmrahStates.collectAsState()
    
    val steps = if (languageCode == "ur") {
        listOf(
            GuideStep(1, "میقات اور احرام", "میقات سے احرام باندھنا (غسل، نیت اور تلبیہ)۔ محرم کے لیے حرام کردہ چیزوں سے پرہیز کرنا۔", 
                duas = listOf("نیت: میں عمرہ مفردہ (یا تمتع) قربت الی اللہ انجام دیتا/دیتی ہوں۔", "تلبيہ: لَبَّيْكَ ٱللَّٰهُمَّ لَبَّيْكَ، لَبَّيْكَ لَا شَرِيكَ لَكَ لَبَّيْكَ..."),
                refs = "میقات: ذوالحلیفہ (مدینہ سے)، جحفہ (شام سے)، قرن المنازل (نجد سے)، یلملم (یمن سے)، ذات عرق (عراق سے)۔ مکہ والے گھر سے باندھ سکتے ہیں۔"
            ),
            GuideStep(2, "طواف (خانہ کعبہ کے 7 چکر)", "حجر اسود کی سیدھ سے شروع کر کے 7 چکر لگانا۔ کعبہ کو بائیں جانب رکھنا۔", 
                duas = listOf("طواف کی کوئی مخصوص دعا واجب نہیں، تاہم مستحب ہے: بِسْمِ اللَّهِ وَ اللَّهُ أَكْبَرُ، اللَّهُمَّ إِيمَانًا بِكَ وَ تَصْدِيقًا بِكِتَابِكَ... (حجر اسود کے سامنے)"),
                refs = "حجر اسود، رکن یمانی، ملتزم، مقام ابراہیم اور آب زمزم کی برکات سمیٹیں۔ طواف کے بعد مقام ابراہیم کے پیچھے 2 رکعت نماز طواف پڑھیں۔"
            ),
            GuideStep(3, "سعی (صفا و مروہ)", "صفا سے شروع اور مروہ پر ختم۔ کل 7 چکر۔ راستے میں دعائیں اور ذکر کریں۔",
                duas = listOf("کوئی بھی ذکر، درود، اور دعائے توسل پڑھی جا سکتی ہے۔")
            ),
            GuideStep(4, "تقصیر / حلق", "احرام سے نکلنے کے لیے بال اور ناخن کاٹنا (عمرہ تمتع میں صرف تقصیر، عمرہ مفردہ میں مردوں کے لیے حلق یعنی سر منڈانا بھی جائز ہے)۔",
                duas = emptyList()
            )
        )
    } else {
        listOf(
            GuideStep(1, "Miqat & Ihram", "Enter state of Ihram from Miqat (Ghusl, Niyyah, Talbiyah). Observe all Ihram restrictions.", 
                duas = listOf("Niyyah: I perform Umrah Mufradah (or Tamattu) seeking proximity to Allah.", "Talbiyah: Labbayka Allahumma Labbayk, Labbayka La Sharika Laka Labbayk..."),
                refs = "Miqats: Dhu al-Hulayfah (Medina), Al-Juhfah (Syria), Qarn al-Manazil (Najd), Yalamlam (Yemen), Dhat Irq (Iraq). Meccans from their homes."
            ),
            GuideStep(2, "Tawaf (7 Circuits)", "Start from the line of Hajr-e-Aswad, walk 7 times counter-clockwise (Kaaba on your left).", 
                duas = listOf("Recommended: Bismillahi wallahu akbar, Allahumma imanan bika wa tasdiqan bi-kitabika... (when facing the Black Stone)."),
                refs = "After Tawaf, perform 2 Rak'ats of Tawaf Prayer behind Maqam-e-Ibrahim. Be mindful of Rukn-e-Yamani, Multazam, and Zamzam."
            ),
            GuideStep(3, "Sa'i (Safa & Marwah)", "7 circuits starting from Safa and ending at Marwah.",
                duas = listOf("Recite any Adhkar or Salawat during the walk.")
            ),
            GuideStep(4, "Taqsir / Halq", "Cutting hair/nails to exit Ihram. (For Umrah Tamattu, only Taqsir; for Mufradah, men can also shave their heads).",
                duas = emptyList()
            )
        )
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text(
            text = if (languageCode == "ur") "عمرہ کا قدم بہ قدم طریقہ اور چیک لسٹ" else "Step-by-Step Umrah Checklist",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        steps.forEach { step ->
            val isChecked = hajjUmrahItems.find { it.type == "UMRAH" && it.stepId == step.id }?.isCompleted == true
            StepCard(
                step = step, 
                isChecked = isChecked,
                onCheckedChange = { checked -> viewModel.toggleHajjUmrahItem("UMRAH", step.id, checked) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun HajjGuide(viewModel: PrayerViewModel, languageCode: String) {
    val hajjUmrahItems by viewModel.allHajjUmrahStates.collectAsState()
    
    val steps = if (languageCode == "ur") {
        listOf(
            GuideStep(1, "8 ذوالحجہ (یوم الترویہ)", "مکہ سے حج کا احرام باندھنا اور منیٰ روانگی۔ مستحب ہے کہ ظہر سے پہلے منیٰ پہنچیں اور دعائیں پڑھیں۔",
                duas = listOf("نیت: میں حج تمتع کا احرام باندھتا ہوں قربت الی اللہ۔")
            ),
            GuideStep(2, "9 ذوالحجہ (یوم عرفہ)", "میدان عرفات میں وقوف (قیام) زوالِ آفتاب (ظہر) سے لے کر مغرب تک۔ یہ حج کا سب سے اہم رکن ہے۔",
                duas = listOf("عرفات میں کوئی بھی دعا، استغفار، اور خاص طور پر دعائے عرفہ (امام حسینؑ) پڑھنا بہت مستحب ہے۔")
            ),
            GuideStep(3, "مزدلفہ (مشعر الحرام)", "مغرب کے بعد عرفات سے مزدلفہ روانگی۔ وہاں نماز مغربین اکٹھی پڑھنا اور کنکریاں (70) چننا۔ اذانِ صبح سے طلوعِ آفتاب تک وقوف۔",
                duas = emptyList()
            ),
            GuideStep(4, "10 ذوالحجہ (یوم النحر - منیٰ)", "1. جمرہ عقبہ کی رمی (7 کنکریاں مارنا)\n2. قربانی کرنا\n3. حلق یا تقصیر (سر منڈانا یا بال کاٹنا) تاکہ احرام کھل جائے۔",
                duas = listOf("کنکری مارتے وقت: بِسْمِ اللَّهِ وَ اللَّهُ أَكْبَرُ")
            ),
            GuideStep(5, "طواف ِ زیارت و سعی اور طواف النساء (مکہ)", "مکہ واپس جا کر حج کا طواف (طواف زیارت)، نماز طواف، سعی (صفا و مروہ) کرنا۔ پھر طواف النساء اور اس کی نماز پڑھنا۔ بقیہ ایام (11 اور 12) کو منیٰ میں گزارنا۔",
                duas = emptyList()
            ),
            GuideStep(6, "11 اور 12 ذوالحجہ (منیٰ میں قیام)", "ایام تشریق میں منیٰ میں راتیں گزارنا اور دونوں دن زوال کے بعد تینوں جمرات کو بالترتیب (اولیٰ، وسطیٰ، عقبہ) 7-7 کنکریاں مارنا۔",
                duas = emptyList()
            )
        )
    } else {
        listOf(
            GuideStep(1, "8th Dhul-Hijjah (Tarwiyah)", "Enter Ihram for Hajj from Mecca and travel to Mina. Recommended to reach before Dhuhr.",
                duas = listOf("Niyyah: I wear Ihram for Hajj al-Tamattu seeking proximity to Allah.")
            ),
            GuideStep(2, "9th Dhul-Hijjah (Day of Arafah)", "Travel to Arafat. Stand (Wuquf) in Arafat from Dhuhr until Maghrib. This is the most important pillar.",
                duas = listOf("Recite any Munajat, Istighfar, and highly recommended to recite Dua Arafah of Imam Hussain (as).")
            ),
            GuideStep(3, "Muzdalifah (Mash'ar)", "Leave Arafat after Maghrib for Muzdalifah. Pray Maghrib & Isha combined. Collect pebbles (approx. 70). Wuquf from Fajr to Sunrise.",
                duas = emptyList()
            ),
            GuideStep(4, "10th Dhul-Hijjah (Nahr - Mina)", "1. Rami at Jamaraat al-Aqabah (7 pebbles)\n2. Animal Sacrifice (Qurbani)\n3. Halq/Taqsir (Shave/cut hair) to exit Ihram.",
                duas = listOf("While throwing pebbles: Bismillahi Wallahu Akbar")
            ),
            GuideStep(5, "Tawaf al-Ziyarah, Sa'i & Tawaf al-Nisa (Mecca)", "Return to Mecca to perform Tawaf of Hajj, its Prayer, and Sa'i. Then Tawaf al-Nisa and its Prayer. After these, marital relations become Halal.",
                duas = emptyList()
            ),
            GuideStep(6, "11th & 12th Dhul-Hijjah (Mina)", "Spend the nights of Ayyam al-Tashreeq in Mina. Perform Rami at all three Jamarat (Ula, Wusta, Aqabah) every afternoon.",
                duas = emptyList()
            )
        )
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text(
            text = if (languageCode == "ur") "حج (تمتع) کا طریقہ اور چیک لسٹ" else "Step-by-Step Hajj Checklist",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        steps.forEach { step ->
            val isChecked = hajjUmrahItems.find { it.type == "HAJJ" && it.stepId == step.id }?.isCompleted == true
            StepCard(
                step = step, 
                isChecked = isChecked,
                onCheckedChange = { checked -> viewModel.toggleHajjUmrahItem("HAJJ", step.id, checked) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun InfoGuide(languageCode: String) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (languageCode == "ur") "حج کی اقسام" else "Types of Hajj",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                if (languageCode == "ur") {
                    Text("1. حج تمتع: ان لوگوں کے لیے جو مکہ سے 88 کلومیٹر یا اس سے زیادہ دور رہتے ہیں۔ اس میں پہلے عمرہ تمتع کیا جاتا ہے، پھر احرام کھول دیا جاتا ہے، اور 8 ذوالحجہ کو حج کا احرام باندھا جاتا ہے۔\n\n2. حج قران: اس میں عمرہ اور حج ایک ہی احرام میں کیے جاتے ہیں اور قربانی کا جانور ساتھ لیجایا جاتا ہے۔\n\n3. حج افراد: یہ قران کی طرح ہے لیکن اس میں قربانی لازمی نہیں۔", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("1. Hajj al-Tamattu': For those living >88km from Mecca. You perform Umrah exactly, exit Ihram, and re-enter Ihram for Hajj on 8th Dhul-Hijjah.\n\n2. Hajj al-Qiran: Umrah and Hajj in a single Ihram, and the sacrificial animal is brought along.\n\n3. Hajj al-Ifrad: Similar to Qiran but sacrifice is not obligatory.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (languageCode == "ur") "ارکانِ حج و عمرہ (لازمی فرائض)" else "Arkan (Obligatory Pillars)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (languageCode == "ur") 
                    "عمرہ کے ارکان: احرام، طواف، اور سعی۔ (اگر ان میں خلل آئے تو عمرہ باطل ہو سکتا ہے)\n\nحج کے ارکان: احرام، وقوفِ عرفات، وقوفِ مزدلفہ، طواف حج، اور سعی۔" 
                    else "Pillars of Umrah: Ihram, Tawaf, Sa'i.\nPillars of Hajj: Ihram, Wuquf in Arafat, Wuquf in Muzdalifah, Tawaf al-Hajj, Sa'i.\n(Invalidating any of these nullifies the ritual).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
fun StepCard(step: GuideStep, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) 
                             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!isChecked) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(step.id.toString(), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = step.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Checkbox(checked = isChecked, onCheckedChange = onCheckedChange)
            }

            if (step.duas.isNotEmpty() || step.refs != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                
                step.refs?.let {
                    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(bottom = 8.dp)) {
                        Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
                
                step.duas.forEach { dua ->
                    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(bottom = 4.dp)) {
                        Icon(Icons.Default.Star, contentDescription = "Dua", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(dua, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

data class GuideStep(
    val id: Int,
    val title: String,
    val description: String,
    val duas: List<String> = emptyList(),
    val refs: String? = null
)
