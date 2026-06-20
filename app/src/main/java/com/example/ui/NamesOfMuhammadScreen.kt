package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

data class NameOfMuhammad(
    val id: Int,
    val arabic: String,
    val transliteration: String,
    val english: String,
    val urdu: String,
    val description: String,
    val descriptionUrdu: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamesOfMuhammadScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    languageCode: String = "en"
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedName by remember { mutableStateOf<NameOfMuhammad?>(null) }
    var activeCounter by remember { mutableStateOf(0) }

    val names = remember {
        listOf(
            NameOfMuhammad(1, "مُحَمَّدٌ", "Muhammad", "The Highly Praised", "جس کی بہت تعریف کی گئی ہو", "The primary sacred name of the Holy Prophet, representing perfect attributes.", "حضورِ اکرم صلی اللہ علیہ وآلہ وسلم کا اسمِ مبارک جو تمام تعریفوں کے جامع ہونے کا نشان ہے۔"),
            NameOfMuhammad(2, "أَحْمَدٌ", "Ahmad", "The Most Praiseworthy", "سب سے زیادہ تعریف کرنے والا", "Mentioned in Surah As-Saff by Prophet Isa (as) forecasting his arrival.", "قرآنِ پاک میں حضرت عیسیٰ علیہ السلام کی زبانی بشارت کے طور پر مذکور نام۔"),
            NameOfMuhammad(3, "حَامِدٌ", "Hamid", "The Praiser of Allah", "تعریف کرنے والا", "The one who performs absolute and perfect praise of Allah Almighty.", "اللہ تعالیٰ کی کثرت اور بہترین انداز میں حمد و ثنا کرنے والا۔"),
            NameOfMuhammad(4, "مَحْمُودٌ", "Mahmud", "The Praised One", "پسندیدہ اور لائقِ تعریف", "Reflects the supreme spiritual status of Maqam-e-Mahmud.", "مقامِ محمود پر فائز ہستی، جن کی کائنات کی ہر مخلوق ثنا کرتی ہے۔"),
            NameOfMuhammad(5, "قَاسِمٌ", "Qasim", "The Distributor", "بخشنے والا / تقسیم کرنے والا", "The distributor of divine knowledge, blessings, and celestial truth.", "اللہ کی نعمتوں، علم اور معارف کو کائنات میں بانٹنے والا۔"),
            NameOfMuhammad(6, "عَاقِبٌ", "Aqib", "The Successor / Last", "پیچھے آنے والا", "The one who came after all prophets, leaving no prophet after him.", "تمام انبیاء کے آخر میں تشریف لانے والا، جن کے بعد کوئی نبی نہیں۔"),
            NameOfMuhammad(7, "فَاتِحٌ", "Fatih", "The Opener / Conqueror", "کھولنے والا / فتح کرنے والا", "The opener of the doors of divine mercy, guidance, and spiritual victory.", "رحمت الٰہی کے دروازے اور علم کے بند راستے کھولنے والا۔"),
            NameOfMuhammad(8, "خَاتَمٌ", "Khatam", "The Seal", "مہرِ رسالت", "The Seal of all Messengers and Prophets, completing the chain.", "سلسلہ نبوت کو مکمل کر کے اس پر آخری مہر ثبت فرمانے والا۔"),
            NameOfMuhammad(9, "حَاشِرٌ", "Hashir", "The Gatherer", "جمع کرنے والا", "The one at whose feet and call all of humanity will gather on Judgment Day.", "قیامت کے دن جن کے قدموں میں تمام خلقت کا حشر کیا جائے گا۔"),
            NameOfMuhammad(10, "مَاحِي", "Mahi", "The Eraser of Disbelief", "مٹانے والا", "The chosen one through whom Allah erases polytheism and transgression.", "وہ مقدس ہستی جن کے ذریعے اللہ کفر اور شرک کے اندھیروں کو مٹاتا ہے۔"),
            NameOfMuhammad(11, "دَاعِي", "Da'i", "The Caller to Allah", "اللہ کی طرف بلانے والا", "Inviting humanity to the path of eternal light, peace, and salvation.", "انسانیت کو حق اور نجات کے ابدی راستے کی طرف دعوت دینے والا۔"),
            NameOfMuhammad(12, "سِرَاجٌ مُنِيرٌ", "Siraj-um-Munir", "The Glowing Lamp", "روشن چراغ", "Directly named in the Holy Quran as a source of absolute light and guidance.", "قرآن کریم میں روشن اور منور چراغ کے لقب سے پکارا گیا اسم مبارک۔"),
            NameOfMuhammad(13, "بَشِيرٌ", "Bashir", "The Giver of Glad Tidings", "خوشخبری دینے والا", "Forewarning believers of the beautiful rewards of Paradise and close comfort.", "ایمان والوں کو جنت کی لازوال نعمتوں اور قربِ الٰہی کی خوشخبری دینے والا۔"),
            NameOfMuhammad(14, "نَذِيرٌ", "Nadhir", "The Warner", "ڈرانے والا", "Warning humanity against deviation, loss of soul, and divine anger.", "خالق سے دوری اور گمراہی کے نقصانات سے بروقت خبردار کرنے والا۔"),
            NameOfMuhammad(15, "مُنْذِرٌ", "Mundhir", "The warner to each community", "راہِ مانی دکھانے والا", "The master guide showing warning paths and keeping borders clean.", "دلوں کو گمراہی کے تاریک گڑھوں سے بچانے والا رہنما۔"),
            NameOfMuhammad(16, "أُمِّيٌّ", "Ummi", "The Untaught Prophet", "امی (لوح و قلم کے مالک)", "Taught only by Allah, possessing absolute direct divine knowledge.", "کسی انسان سے علم حاصل نہ کرنے والا، جسے براہ راست اللہ نے سکھایا۔"),
            NameOfMuhammad(17, "صَادِقٌ", "Sadiq", "The Truthful One", "سچا", "Famous throughout Arabia even before the declaration of prophet-hood.", "بچپن اور جوانی سے ہی صادق اور امانت کے اعلیٰ مقام پر فائز۔"),
            NameOfMuhammad(18, "أَمِينٌ", "Amin", "The Trustworthy", "امانت دار", "The protector of trusts, secrets, and spiritual messages perfectly.", "ہر امانت اور راز کی حفاظت کرنے والا سب سے بہترین امین۔"),
            NameOfMuhammad(19, "شَكُورٌ", "Shakur", "The Grateful", "شکر گزار", "Perfect in expressing boundless gratitude through every worship.", "اللہ تعالیٰ کی ہر نعمت پر ہر گھڑی شکر ادا کرنے والے۔"),
            NameOfMuhammad(20, "مُطَهَّرٌ", "Mutahhar", "The Purified", "پاک و پاکیزہ", "Completely purified from all defects, faults, and lower desires.", "ہر قسم کی لغزش اور باطنی و ظاہری عیوب سے مکمل پاک ہستی۔"),
            NameOfMuhammad(21, "طَيِّبٌ", "Tayyib", "The Pure / Fragrant", "پاکیزہ نفس", "Whose presence, breath, and fragrance outclass the best musks.", "نہایت پاکیزہ سیرت، جس کے پسینے کی خوشبو کستوری سے بہتر ہو۔"),
            NameOfMuhammad(22, "مُجْتَبَىٰ", "Mujtaba", "The Chosen / Elected", "چنیدہ / منتخب", "Elected as the chief of all creations before time was initiated.", "کائنات کی تخلیق سے قبل اللہ کی طرف سے منتخب کردہ سب سے محبوب۔"),
            NameOfMuhammad(23, "مُصْطَفَىٰ", "Mustafa", "The Chosen One", "چنا ہوا", "Selected by Allah to carry the final and most complete guidance.", "الٰہی احکام اور پیغامِ ہدایت پہنچانے کے لیے برگزیدہ ہستی۔"),
            NameOfMuhammad(24, "مُرْتَضَىٰ", "Murtada", "The Well-Pleasing", "پسندیدہ", "With whom Allah Almighty is eternally and perfectly pleased.", "جن کی رضا اللہ کی رضا ہے اور جن کی ذات سے رب راضی ہے۔"),
            NameOfMuhammad(25, "رَؤُوفٌ", "Ra'uf", "The Compassionate", "نہایت شفیق", "Filled with boundless love and care for his followers both here and there.", "اپنی امت سے بے پناہ پیار کرنے والا، رحمت کے سمندر۔"),
            NameOfMuhammad(26, "رَحِيمٌ", "Rahim", "The Merciful", "مہربان اور رحیم", "Exhibiting deep forgiveness and support for the struggling souls.", "کمزور گنہگاروں کی شفاعت فرمانے والا شفیق غمخوار۔"),
            NameOfMuhammad(27, "رَحْمَةٌ لِّلْعَالَمِينَ", "Rahmat-ul-lil-Aalameen", "Mercy unto the Creation", "تمام جہانوں کے لیے رحمت", "Directly blessed by Allah as the single source of universal mercy.", "کائنات کے ذرے ذرے کے لیے اللہ کا سب سے وسیع تحفہ اور رحمت۔"),
            NameOfMuhammad(28, "طٰهٰ", "Taha", "Taha (Sacred Title)", "طہٰ (مقدس پکار)", "The mysterious divine title, reflecting cosmic purity and guidance.", "قرآنِ عظیم سورہ طہٰ کا آغاز، جو ایک عظیم راز اور لقب ہے۔"),
            NameOfMuhammad(29, "يٰسٓ", "Yaseen", "Yaseen (Perfect Leader)", "یسین (سردار)", "The heart of Quran, addressed with deep love and status by the Creator.", "قرآن پاک کا دل، سردار انبیاء کو پکارنے والا خوبصورت الٰہی خطاب۔"),
            NameOfMuhammad(30, "مُزَّمِّلٌ", "Muzzammil", "The Enwrapped One", "چادر اوڑھنے والا", "Comforted by Allah during early heavy spiritual revelations.", "ابتدائی نزولِ وحی کے وقت چادر مبارک لپیٹنے والا پیارا لقب۔"),
            NameOfMuhammad(31, "مُدَّثِّرٌ", "Muddaththir", "The Cloaked One", "کملی والا", "Addressed by Allah to rise and deliver the warnings to the world.", "سرگوشیوں اور وحی کے غار کے بعد کائنات کو بیدار کرنے والا۔"),
            NameOfMuhammad(32, "شَافِعٌ", "Shafi'", "The Intercessor", "شفاعت کرنے والا", "Whose intercession is guaranteed on Judgment Day for his believers.", "روزِ محشر گناہ گار امتیوں کی مغفرت کے لیے گواہی دینے والا۔"),
            NameOfMuhammad(33, "شَفِيعٌ", "Shafi'", "The Helper / Mediator", "سفارش کرنے والا", "The supreme advocate representing weak souls at the Divine Court.", "بارگاہ الٰہی میں عاجز بندوں کی شفاعت لانے والی برگزیدہ ہستی۔"),
            NameOfMuhammad(34, "مُشَفَّعٌ", "Mushaffa'", "Whose intercession is accepted", "پذیرفتہ شفاعت", "The Savior whose intercession is never rejected by Allah.", "وہ مقرب بارگاہ جن کی امت کے حق میں کی گئی ہر دعا قبول ہے۔")
        )
    }

    val filteredNames = remember(searchQuery, names) {
        if (searchQuery.isBlank()) {
            names
        } else {
            names.filter {
                it.transliteration.contains(searchQuery, ignoreCase = true) ||
                it.english.contains(searchQuery, ignoreCase = true) ||
                it.arabic.contains(searchQuery) ||
                it.urdu.contains(searchQuery)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (languageCode == "ur") "اسمائے محمدؐ (اسمائے گرامی)" else "Names of Prophet Muhammad (saws)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (languageCode == "ur") "سید الانبیاء کے مبارک نام اور برکات" else "Sacred titles of the Holy Prophet & explanations",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("muhammad_names_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("names_of_muhammad_root")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Mini Interactive Tasbeeh Counter at the top
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (languageCode == "ur") "درود و ذکرِ اسمائے گرامی" else "Salawat & Sacred Name Counter",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (languageCode == "ur") "تعداد: $activeCounter" else "Recitations: $activeCounter",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Count button
                        Button(
                            onClick = { activeCounter++ },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Count")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+1")
                        }

                        // Reset button
                        IconButton(
                            onClick = { activeCounter = 0 },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Counter",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = if (languageCode == "ur") "نام یا ترجمہ تلاش کریں..." else "Search sacred name...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                singleLine = true
            )

            // Names Grid with 2 columns to fit nicely
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredNames) { item ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedName = item }
                            .testTag("muhammad_name_item_${item.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // ID Badge
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                    .align(Alignment.Start),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.id.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Arabic script
                            Text(
                                text = item.arabic,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )

                            // Transliteration
                            Text(
                                text = item.transliteration,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            // Translation based on app language
                            Text(
                                text = if (languageCode == "ur") item.urdu else item.english,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    // Beautiful Name Detail Dialog
    selectedName?.let { item ->
        Dialog(onDismissRequest = { selectedName = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .testTag("muhammad_name_detail_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "NAME OF PROPHET MUHAMMAD (SAWS) #${item.id}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.arabic,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.transliteration,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column {
                            Text(
                                text = "Meaning / Attributes",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = item.english,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column {
                            Text(
                                text = "Urdu Meaning (مفہوم)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = item.urdu,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column {
                            Text(
                                text = "Context & Virtues (سیرت و فضائل)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.descriptionUrdu,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { selectedName = null },
                            modifier = Modifier.align(Alignment.CenterEnd),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (languageCode == "ur") "بند کریں" else "Close")
                        }
                    }
                }
            }
        }
    }
}
