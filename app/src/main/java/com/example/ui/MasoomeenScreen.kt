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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.EventType
import com.example.data.MasoomeenData
import com.example.data.MasoomEvent

data class LocalizedMasoom(
    val index: Int,
    val nameAr: String,
    val nameEn: String,
    val nameUr: String,
    val titleAr: String,
    val titleEn: String,
    val titleUr: String,
    val bornAr: String,
    val bornEn: String,
    val bornUr: String,
    val martyrdomAr: String,
    val martyrdomEn: String,
    val martyrdomUr: String,
    val burialAr: String,
    val burialEn: String,
    val burialUr: String,
    val bioAr: String,
    val bioEn: String,
    val bioUr: String,
    val events: List<MasoomEvent> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasoomeenScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    languageCode: String = "en"
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMasoom by remember { mutableStateOf<LocalizedMasoom?>(null) }

    val rawList = remember {
        listOf(
            LocalizedMasoom(
                index = 1,
                nameAr = "مُحَمَّدٌ (ص)",
                nameEn = "Prophet Muhammad (saws)",
                nameUr = "حضرت محمد مصطفیٰ صلی اللہ علیہ وآلہ وسلم",
                titleAr = "خَاتَمُ النَّبِيِّينَ / الْمُصْطَفَى",
                titleEn = "Al-Mustafa (The Chosen)",
                titleUr = "خاتم الانبیاء والمرسلین، رحمۃ للعالمین",
                bornAr = "١٧ ربيع الأول",
                bornEn = "17 Rabi' al-Awwal",
                bornUr = "۱۷ ربیع الاول",
                martyrdomAr = "٢٨ صفر",
                martyrdomEn = "28 Safar",
                martyrdomUr = "۲۸ صفر",
                burialAr = "المسجد النبوي، المدينة المنورة",
                burialEn = "Al-Masjid an-Nabawi, Medina",
                burialUr = "مسجد نبوی، مدینہ منورہ",
                bioAr = "خاتم الأنبياء والمرسلين والرحمة المهداة للعالمين، الذي أرسله الله بالهدى ودين الحق ليظهره على الدين كله.",
                bioEn = "The final Prophet and Messenger of Allah, who established Islam, received the Holy Qur'an, and set the path of guidance for humanity.",
                bioUr = "اللہ کے آخری رسول اور نبی، جنہوں نے خدا کی توحید کا پیغام عام کیا، قرآن مجید وصول کیا اور پوری انسانیت کے لیے عالمگیر ضابطہ حیات متعارف کرایا۔"
            ),
            LocalizedMasoom(
                index = 2,
                nameAr = "عَلِيٌّ (ع)",
                nameEn = "Imam Ali ibn Abi Talib (as)",
                nameUr = "امام علی ابن ابی طالب علیہ السلام",
                titleAr = "أَمِيرُ الْمُؤْمِنِينَ / الْمُرْتَضَى",
                titleEn = "Amir al-Mu'minin (Comm. of Faithful)",
                titleUr = "امیر المومنین، اسد اللہ الغالب، باب علمِ رسول",
                bornAr = "١٣ رجب",
                bornEn = "13 Rajab",
                bornUr = "۱۳ رجب",
                martyrdomAr = "٢١ رمضان",
                martyrdomEn = "21 Ramadan",
                martyrdomUr = "۲۱ رمضان",
                burialAr = "النجف الأشرف، العراق",
                burialEn = "Imam Ali Shrine, Najaf, Iraq",
                burialUr = "روضہ مبارک امام علیؑ، نجف اشرف، عراق",
                bioAr = "أمير المؤمنين وسيد الوصيين، ولد في جوف الكعبة المشرفة، وباب مدينة علم رسول الله، مظهر العدالة الإلهية والبطولة الإسلامية.",
                bioEn = "The first infallible Imam, born inside the Holy Kaabah, the gate of prophetic knowledge, irreplaceable champion of justice, and standard-bearer of Islam.",
                bioUr = "دوسرے معصوم اور پہلے امام جو خانہ کعبہ کے اندر پیدا ہوئے۔ آپ علمِ نبوت کا دروازہ، عدل و انصاف کے سب سے بڑے علمبردار اور اسلام کے جری کمانڈر تھے۔"
            ),
            LocalizedMasoom(
                index = 3,
                nameAr = "فَاطِمَةُ (ع)",
                nameEn = "Lady Fatima al-Zahra (sa)",
                nameUr = "حضرت فاطمہ زہرا سلام اللہ علیہا",
                titleAr = "سَيِّدَةُ نِسَاءِ الْعَالَمِينَ / الزَّهْرَاءُ",
                titleEn = "Sayyidat Nisa' al-Alamin",
                titleUr = "سیدہ نساء العالمین، خاتونِ جنت، بضعة الرسول",
                bornAr = "٢٠ جمادى الثانية",
                bornEn = "20 Jumada al-Thani",
                bornUr = "۲۰ جمادی الثانی",
                martyrdomAr = "٣ جمادى الثانية",
                martyrdomEn = "3 Jumada al-Thani",
                martyrdomUr = "۳ جمادی الثانی",
                burialAr = "البقيع / قبر مخفي، المدينة المنورة",
                burialEn = "Jannat al-Baqi / Hidden, Medina",
                burialUr = "جنت البقیع یا پوشیدہ قبر، مدینہ منورہ",
                bioAr = "بنت رسول الله وخير نساء العالمين، زوجة الإمام علي وأم السبطين الحسن والحسين، تمثل الطهارة الإلهية والقدوة لكل مؤمنة.",
                bioEn = "The beloved noble daughter of the Prophet, wife of Imam Ali, mother of Imam Hasan and Imam Hussain, representing the perfect purity of Islam.",
                bioUr = "رسول خدا کی لختِ جگر، مولا علیؑ کی زوجہ، اور سیدا شبابِ اہل الجنۃ کی والدہ گرامی۔ آپ کائنات کی سب سے پاکیزہ اور عظمت والی خاتون ہیں۔"
            ),
            LocalizedMasoom(
                index = 4,
                nameAr = "الْحَسَنُ (ع)",
                nameEn = "Imam Hasan al-Mujtaba (as)",
                nameUr = "امام حسن مجتبیٰ علیہ السلام",
                titleAr = "الْمُجْتَبَى / سِبْطُ الرَّسُولِ",
                titleEn = "Al-Mujtaba (The Chosen)",
                titleUr = "کریمِ اہل بیت، سردارِ شباب المومنین",
                bornAr = "١٥ رمضان",
                bornEn = "15 Ramadan",
                bornUr = "۱۵ رمضان",
                martyrdomAr = "٢٨ صفر",
                martyrdomEn = "28 Safar",
                martyrdomUr = "۲۸ صفر",
                burialAr = "جنة البقيع، المدينة المنورة",
                burialEn = "Jannat al-Baqi, Medina",
                burialUr = "جنت البقیع، مدینہ منورہ",
                bioAr = "سبط رسول الله الأكبر وثاني أئمة أهل البيت، تميز بالحلم والفضائل الفائقة، وصاحب الصلح التاريخي الذي حقن دماء المسلمين وصان الأمة.",
                bioEn = "The second infallible Imam, renowned for his infinite generosity, wisdom, patience, and the historic peace treaty which saved the Muslim community.",
                bioUr = "چوتھے معصوم اور دوسرے امام، جو اپنی بے پناہ سخاوت، حلم، صلح اور دانشمندی کے لیے مشہور ہیں جس کے ذریعے آپ نے امن کی ضامن صلح کر کے اسلام کو بچایا۔"
            ),
            LocalizedMasoom(
                index = 5,
                nameAr = "الْحُسَيْنُ (ع)",
                nameEn = "Imam Hussain ibn Ali (as)",
                nameUr = "امام حسین ابن علی علیہ السلام",
                titleAr = "سَيِّدُ الشُّهَدَاءِ / الرَّشِيدُ",
                titleEn = "Sayyid al-Shuhada (Lord of Martyrs)",
                titleUr = "سید الشہداء، وارثِ انبیاء، سفینہ نجات",
                bornAr = "٣ شعبان",
                bornEn = "3 Sha'ban",
                bornUr = "۳ شعبان",
                martyrdomAr = "١٠ محرم (عاشوراء)",
                martyrdomEn = "10 Muharram (Ashura)",
                martyrdomUr = "۱۰ محرم (عاشورہ)",
                burialAr = "كربلاء المقدسة، العراق",
                burialEn = "Imam Hussain Shrine, Karbala, Iraq",
                burialUr = "روضہ امام حسینؑ، کربلائے معلیٰ، عراق",
                bioAr = "سبط رسول الله ومصباح الهدى وسفينة النجاة، الذي قدم التضحية العظمى في كربلاء مع أهل بيته وأصحابه نصرةً للحق والعدل ودفاعاً عن دين جده.",
                bioEn = "The third infallible Imam, who made the supreme sacrifice in Karbala alongside his family and loyal companions to save truth and justice for eternity.",
                bioUr = "پانچویں معصوم اور تیسرے امام، جنہوں نے کربلا کی تپتی ریت پر دینِ حق کو بچانے کے لیے اپنی اور اپنے پیاروں کی لازوال قربانی پیش کر کے اسلام کی بنیادوں کو مستحکم کیا۔"
            ),
            LocalizedMasoom(
                index = 6,
                nameAr = "عَلِيُّ السَّجَّادُ (ع)",
                nameEn = "Imam Ali ibn al-Hussain (as)",
                nameUr = "امام علی ابن الحسین زین العابدین علیہ السلام",
                titleAr = "زَيْنُ الْعَابِدِينَ / السَّجَّادُ",
                titleEn = "Zayn al-Abidin (Decorator of Worshippers)",
                titleUr = "زین العابدین، بیمارِ کربلا، سید الساجدین",
                bornAr = "٥ شعبان",
                bornEn = "5 Sha'ban",
                bornUr = "۵ شعبان",
                martyrdomAr = "٢٥ محرم",
                martyrdomEn = "25 Muharram",
                martyrdomUr = "۲۵ محرم",
                burialAr = "جنة البقيع، المدينة المنورة",
                burialEn = "Jannat al-Baqi, Medina",
                burialUr = "جنت البقیع، مدینہ منورہ",
                bioAr = "الإمام الرابع، الشاهد على مأساة كربلاء وناشر نداء النهضة الحسينية بالخطب والدموع، وصاحب الصحيفة السجادية (زبور آل محمد) في غاية العرفان.",
                bioEn = "The fourth infallible Imam, standard-bearer of Karbala's message through sermons, and author of Al-Sahifa al-Sajjadiyya (the Psalms of Islam).",
                bioUr = "چوتھے امام، جنہوں نے معرکہ کربلا کی اسیری کے بعد کوفہ و شام کے درباروں میں خطباتِ عالیہ کے ذریعے سچائی کو بلند کیا اور صحیفہ سجادیہ کی صورت میں دعا کا خزانہ دیا۔"
            ),
            LocalizedMasoom(
                index = 7,
                nameAr = "مُحَمَّدُ الْبَاقِرُ (ع)",
                nameEn = "Imam Muhammad ibn Ali (as)",
                nameUr = "امام محمد الباقر علیہ السلام",
                titleAr = "بَاقِرُ الْعُلُومِ / الشَّاكِرُ",
                titleEn = "Al-Baqir (Revealer of Knowledge)",
                titleUr = "باقر العلوم، شکافندہ علم، امام علم اور معارف",
                bornAr = "١ رجب",
                bornEn = "1  Rajab",
                bornUr = "۱ رجب",
                martyrdomAr = "٧ ذو الحجة",
                martyrdomEn = "7 Dhu al-Hijjah",
                martyrdomUr = "۷ ذوالحجہ",
                burialAr = "جنة البقيع، المدينة المنورة",
                burialEn = "Jannat al-Baqi, Medina",
                burialUr = "جنت البقیع، مدینہ منورہ",
                bioAr = "الإمام الخامس، باقر علم النبيين وشاقّ مجامع الحديث والفقه، الممهد المخلص للمدرسة الأكاديمية الكبرى لعلم وفكر عترة المصطفى.",
                bioEn = "The fifth infallible Imam, who opened the floodgates of Ahlul Bayt scientific, jurisprudential, and philosophical teachings in Islamic academia.",
                bioUr = "پانچویں امام، جنہوں نے اسلامی دنیا میں علوم و معارف کو وسعت دی اور علومِ آلِ محمدؑ کا ایک بہت بڑا اسلامی مکتب قائم کیا جس کی وجہ سے آپ باقر العلوم کہلائے۔"
            ),
            LocalizedMasoom(
                index = 8,
                nameAr = "جَعْفَرُ الصَّادِقُ (ع)",
                nameEn = "Imam Ja'far ibn Muhammad (as)",
                nameUr = "امام جعفر الصادق علیہ السلام",
                titleAr = "الصَّادِقُ / الْفَاضِلُ",
                titleEn = "Al-Sadiq (The Truthful)",
                titleUr = "صادق آلِ محمد، بانیِ مذہبِ جعفریہ، شیخ الائمہ",
                bornAr = "١٧ ربيع الأول",
                bornEn = "17 Rabi' al-Awwal",
                bornUr = "۱۷ ربیع الاول",
                martyrdomAr = "٢٥ شوال",
                martyrdomEn = "25 Shawwal",
                martyrdomUr = "۲۵ شوال",
                burialAr = "جنة البقيع، المدينة المنورة",
                burialEn = "Jannat al-Baqi, Medina",
                burialUr = "جنت البقیع، مدینہ منورہ",
                bioAr = "الإمام السادس، رئيس المذهب ومؤسس الصرح العلمي الشيعي، درس آلاف العلماء والرواد ورفع منارة الفقه والأخلاق ونشر مناهج البحث العلمي.",
                bioEn = "The sixth infallible Imam, founder of the Ja'fari school of jurisprudence, who trained over 4,000 scholars including Jabir ibn Hayyan and Abu Hanifa.",
                bioUr = "چھٹے امام، جن کی علمی کرسی سے جابر بن حیان اور ابوحنیفہ جیسے ہزاروں نامور دنیا کے محققین نکلے، آپ مذہبِ جعفریہ کے فکری و فقہی بانی ہیں۔"
            ),
            LocalizedMasoom(
                index = 9,
                nameAr = "مُوسَى الْكَاظِمُ (ع)",
                nameEn = "Imam Musa ibn Ja'far (as)",
                nameUr = "امام موسیٰ الکاظم علیہ السلام",
                titleAr = "الْكَاظِمُ / بَابُ الْحَوَائِجِ",
                titleEn = "Al-Kadhim (The Restrainer of Anger)",
                titleUr = "موسیٰ کاظم، باب الحوائج، صابر و شاکر",
                bornAr = "٢٠ ذو الحجة / ٧ صفر",
                bornEn = "7 Safar / 20 Dhu al-Hijjah",
                bornUr = "۷ صفر / ۲۰ ذوالحجہ",
                martyrdomAr = "٢٥ رجب",
                martyrdomEn = "25 Rajab",
                martyrdomUr = "۲۵ رجب",
                burialAr = "الكاظمية المقدسة، بغداد",
                burialEn = "Al-Kadhimiya Mosque, Baghdad",
                burialUr = "روضہ کاظمین، بغداد، عراق",
                bioAr = "الإمام السابع، أسير السجون الرهيبة ورمز الصبر الجميل وضبط الغضب، الملقب بكاظم الغيظ لشدة ترفعه وصاحب كرامة باب الحوائج للناس.",
                bioEn = "The seventh infallible Imam, famous for his extreme patience, enduring long imprisonments in Abbassid dungeons but guiding thousands to truth.",
                bioUr = "ساتویں امام، جنہیں بوجہ حلم و تقویٰ 'الکاظم' کہا جاتا ہے۔ آپ نے عباسی حاکم ہارون الرشید کی قید و بند کی اذیتیں ہنس کر جھیلیں مگر وقارِ دین پر سمجھوتہ نہیں کیا۔"
            ),
            LocalizedMasoom(
                index = 10,
                nameAr = "عَلِيُّ الرِّضَا (ع)",
                nameEn = "Imam Ali ibn Musa (as)",
                nameUr = "امام علی الرضا علیہ السلام",
                titleAr = "الرِّضَا / غَرِيبُ الطُّوسِ",
                titleEn = "Al-Ridha (The Pleasing)",
                titleUr = "ضامنِ علیؑ الرضا، غریب الغربا، عالم آل محمد",
                bornAr = "١١ ذو القعدة",
                bornEn = "11 Dhu al-Qa'dah",
                bornUr = "۱۱ ذوالقعدہ",
                martyrdomAr = "٣٠ صفر",
                martyrdomEn = "30 Safar",
                martyrdomUr = "۳۰ صفر",
                burialAr = "مشهد المقدسة، إيران",
                burialEn = "Imam Reza Shrine, Mashhad, Iran",
                burialUr = "حرم امام علی رضاؑ، مشہد مقدس، ایران",
                bioAr = "الإمام الثامن، ضامن الجنان وعالم آل محمد، الذي بهر العلماء والبطاركة في مناظراته العلمية في خراسان، ونشر راية التشيع بسلام وحكمة.",
                bioEn = "The eighth infallible Imam, famous for his debates with scholars of other religions, showcasing the supremacy of Islamic intellect.",
                bioUr = "آٹھویں امام، جن کی شان کلام خداوندی اور علم و حکمت میں اس قدر بلند تھی کہ آپ کی مجلس ہائے مناظرہ میں مختلف مذاہب کے تمام معتبر علماء مبہوت ہوجاتے تھے۔"
            ),
            LocalizedMasoom(
                index = 11,
                nameAr = "مُحَمَّدُ الْجَوَادُ (ع)",
                nameEn = "Imam Muhammad ibn Ali (as)",
                nameUr = "امام محمد تقی الجواد علیہ السلام",
                titleAr = "الْجَوَادُ / التَّقِيُّ / قَانِعٌ",
                titleEn = "Al-Jawad / Al-Taqi (The Generous)",
                titleUr = "تقی الجواد، معجزہ امامت، باب المراد",
                bornAr = "١٠ رجب",
                bornEn = "10 Rajab",
                bornUr = "۱۰ رجب",
                martyrdomAr = "٣٠ ذو القعدة",
                martyrdomEn = "30 Dhu al-Qa'dah",
                martyrdomUr = "۳۰ ذوالقعدہ",
                burialAr = "الكاظمية المقدسة، بغداد",
                burialEn = "Al-Kadhimiya Mosque, Baghdad",
                burialUr = "روضہ کاظمین، بغداد، عراق",
                bioAr = "الإمام التاسع، الملقب بالجواد لبحر جوده، وأصغر الأئمة عمراً لكنه أفحم أكابر فقهاء العصر بمجالس العلم والفتيا منذ صباه الباكر.",
                bioEn = "The ninth infallible Imam, the youngest of all Imams (assumed Imamate at age 7), whose debates astounded veteran contemporary scholars.",
                bioUr = "نویں امام، جنہوں نے نہایت کم عمری (۷ سال) میں منصبِ امامت سنبھالا اور اپنے بے مثال باریک بین فقہی و فکری جوابات سے زمانے کے بڑے بڑے مدعیانِ پختگی کو انگشت بدنداں کر دیا۔"
            ),
            LocalizedMasoom(
                index = 12,
                nameAr = "عَلِيُّ الْهَادِي (ع)",
                nameEn = "Imam Ali ibn Muhammad (as)",
                nameUr = "امام علی نقی الہادی علیہ السلام",
                titleAr = "الْهَادِي / النَّقِيُّ / الْعَسْكَرِيُّ",
                titleEn = "Al-Hadi / Al-Naqi (The Guide)",
                titleUr = "ہادی النقی، معصوم دہم، بانیِ نظام وکالت",
                bornAr = "١٥ ذو الحجة",
                bornEn = "15 Dhu al-Hijjah",
                bornUr = "۱۵ ذوالحجہ",
                martyrdomAr = "٣ رجب",
                martyrdomEn = "3 Rajab",
                martyrdomUr = "۳ رجب",
                burialAr = "سامراء المقدسة، العراق",
                burialEn = "Al-Askari Shrine, Samarra, Iraq",
                burialUr = "روضہ عسکریین، سامرا، عراق",
                bioAr = "الإمام العاشر، صاحب الزيارة الجامعة الكبيرة التي تمثل ميثاق الولاء، أدار شئون الأمة بحكمة من خلال نظام الوكلاء الحذر في سامراء.",
                bioEn = "The tenth infallible Imam, who taught Ziyarat al-Jamiah al-Kabirah and guided the Shia through a network of deputies (Wikalah) under fortress confinement.",
                bioUr = "دسویں امام، جنہوں نے عسکری حصار کی کڑی نگرانی میں بھی وکلاء کے نیٹ ورک کی قیادت کی اور عظیم الشان 'زیارت جامعہ کبیرہ' جیسی پرمعارف ترین زیارت کی تعلیم فرمائی۔"
            ),
            LocalizedMasoom(
                index = 13,
                nameAr = "الْحَسَنُ الْعَسْكَرِيُّ (ع)",
                nameEn = "Imam Hasan ibn Ali (as)",
                nameUr = "امام حسن العسکری علیہ السلام",
                titleAr = "الْعَسْكَرِيُّ / الزَّكِيُّ",
                titleEn = "Al-Askari (The Soldier)",
                titleUr = "حسنؑ العسکری، والدِ منجیِ کائنات",
                bornAr = "٨ ربيع الثاني",
                bornEn = "8 Rabi' al-Thani",
                bornUr = "۸ ربیع الثانی",
                martyrdomAr = "٨ ربيع الأول",
                martyrdomEn = "8 Rabi' al-Awwal",
                martyrdomUr = "۸ ربیع الاول",
                burialAr = "سامراء المقدسة، العراق",
                burialEn = "Al-Askari Shrine, Samarra, Iraq",
                burialUr = "روضہ عسکریین، سامرا، عراق",
                bioAr = "الإمام الحادي عشر، والد الإمام المنتظر، قضى حياته في الإقامة الجبرية العسكرية تحت عيون السلاطين، ومهد للشريعة في مرحلة الغيبة الكبرى.",
                bioEn = "The eleventh infallible Imam, father of the Savior, spent his entire life in military surveillance but maintained connections with the global Shia community.",
                bioUr = "گیارہویں امام، جو امام زمانہ عجل اللہ فرجہ کے والد بزرگوار ہیں۔ آپ کا سارا دور فوجی محاصرے میں کٹ گیا لیکن آپ نے امتیوں کو غیبت کی زبردست علمی تیاری کروائی۔"
            ),
            LocalizedMasoom(
                index = 14,
                nameAr = "الْقَائِمُ الْمَهْدِيُّ (عج)",
                nameEn = "Imam Muhammad ibn al-Hasan (ajtf)",
                nameUr = "امام زمانہ مہدی القائم عجل اللہ فرجہ الشریف",
                titleAr = "الْمَهْدِيُّ الْمُنْتَظَرُ / صَاحِبُ الزَّمَانِ",
                titleEn = "Al-Mahdi / Al-Qa'im (The Guided)",
                titleUr = "بقیۃ اللہ، صاحب العصر والزمان، منجیِ کائنات",
                bornAr = "١٥ شعبان",
                bornEn = "15 Sha'ban",
                bornUr = "۱۵ شعبان",
                martyrdomAr = "حيٌّ (في الغيبة الإلهية)",
                martyrdomEn = "Alive (In Occultation)",
                martyrdomUr = "بقیدِ حیات (پردہ غیبت میں قائم)",
                burialAr = "عجل الله فرجه (الظهور المرتقب)",
                burialEn = "N/A (Active Guidance under Occultation)",
                burialUr = "عجل اللہ فرجہ الشریف (منتظرِ حکمِ الٰہی)",
                bioAr = "الإمام الثاني عشر، حجة الله الغائب والمنقذ المنتظر للبشرية، الموعود أن يملأ الأرض قسطاً وعدلاً كما ملئت ظلماً وجوراً عند ظهوره المبارك.",
                bioEn = "The twelfth and current Infallible Imam, living under divine occultation, who will reappear with Jesus Christ to fill the earth with equity, peace, and absolute justice.",
                bioUr = "بارہویں معصوم اور آخری امامِ برحق، جو بحکمِ الٰہی پردہ غیبت میں تشریف فرما ہیں۔ آپ ہی ظہور فرما کر دنیا کو کفر سے مٹائیں گے اور فسطائیت مٹا کر عدل قائم کریں گے۔"
            )
        )
    }

    // Attach events from original data object to match calendar
    val localizedMasoomeen = remember(rawList) {
        rawList.map { local ->
            val orig = MasoomeenData.list.find { it.index == local.index }
            local.copy(events = orig?.events ?: emptyList())
        }
    }

    val filteredMasoomeen = remember(searchQuery, localizedMasoomeen) {
        if (searchQuery.isBlank()) {
            localizedMasoomeen
        } else {
            localizedMasoomeen.filter {
                it.nameEn.contains(searchQuery, ignoreCase = true) ||
                it.nameUr.contains(searchQuery, ignoreCase = true) ||
                it.nameAr.contains(searchQuery) ||
                it.titleEn.contains(searchQuery, ignoreCase = true) ||
                it.titleUr.contains(searchQuery, ignoreCase = true) ||
                it.titleAr.contains(searchQuery)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (languageCode == "ur") "14 معصومین علیہم السلام" else if (languageCode == "ar") "١٤ المعصومين (ع)" else "The 14 Masoomeen",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (languageCode == "ur") "اہلبیت علیہم السلام کی پاکیزہ زندگی اور سوانح" else if (languageCode == "ar") "سيرة وتاريخ المعصومين الأربعة عشر من آل محمد" else "Biography and history of the Holy Ahlul Bayt (as)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("masoomeen_back")
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
        modifier = modifier.testTag("masoomeen_root")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = if (languageCode == "ur") "نام یا لقب تلاش کریں..." else if (languageCode == "ar") "ابحث عن الاسم أو اللقب..." else "Search by name or title...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                singleLine = true
            )

            // Masoomeen Custom Grid Display matching Allah / Muhammad interfaces
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredMasoomeen) { item ->
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
                            .clickable { selectedMasoom = item }
                            .testTag("masoom_item_${item.index}")
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
                                    text = item.index.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Arabic script
                            Text(
                                text = item.nameAr,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )

                            // Localized Name Translation
                            Text(
                                text = if (languageCode == "ur") item.nameUr else if (languageCode == "ar") item.nameAr else item.nameEn,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Title details based on language settings
                            Text(
                                text = if (languageCode == "ur") item.titleUr else if (languageCode == "ar") item.titleAr else item.titleEn,
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

    // Dialog displaying complete historical bio and metadata
    selectedMasoom?.let { item ->
        Dialog(onDismissRequest = { selectedMasoom = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .testTag("masoomeen_detail_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (languageCode == "ur") "معصوم نمبر #${item.index}" else if (languageCode == "ar") "المعصوم رقم #${item.index}" else "INFALLIBLE MASOOM #${item.index}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.nameAr,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (languageCode == "ur") item.nameUr else if (languageCode == "ar") item.nameAr else item.nameEn,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (languageCode == "ur") item.titleUr else if (languageCode == "ar") item.titleAr else item.titleEn,
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary,
                            textAlign = TextAlign.Center
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Born details mapping
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (languageCode == "ur") "ولادت باسعادت" else if (languageCode == "ar") "الولادة المباركة" else "SACRED BIRTH",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (languageCode == "ur") item.bornUr else if (languageCode == "ar") item.bornAr else item.bornEn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (languageCode == "ur") "شہادت / گراں سفر" else if (languageCode == "ar") "الشهادة / الغيبة" else "MARTYDOM / STATUS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (item.index == 14) Color(0xFF00BFA5) else MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = if (languageCode == "ur") item.martyrdomUr else if (languageCode == "ar") item.martyrdomAr else item.martyrdomEn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.End
                                )
                            }
                        }

                        // Shrine/Place detail row
                        Column {
                            Text(
                                text = if (languageCode == "ur") "روضہ اطہر / زیارت گاہ" else if (languageCode == "ar") "المرقد والمقام" else "ZIYARAH SHRINE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Shrine Location",
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (languageCode == "ur") item.burialUr else if (languageCode == "ar") item.burialAr else item.burialEn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Detailed Bio mapping
                        Column {
                            Text(
                                text = if (languageCode == "ur") "سوانح حیات اور فضل" else if (languageCode == "ar") "السيرة الروحية العطرة" else "SACRED BIOGRAPHY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (languageCode == "ur") item.bioUr else if (languageCode == "ar") item.bioAr else item.bioEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }

                        // Sub Events listing
                        if (item.events.isNotEmpty()) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = if (languageCode == "ur") "اہم تواریخ" else if (languageCode == "ar") "الأحداث الهامة" else "KEY LUNAR CALENDAR EVENTS",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    item.events.forEach { ev ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = ev.title,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        if (ev.eventType == EventType.WILADAT) Color(0xFF00BFA5).copy(alpha = 0.15f)
                                                        else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = ev.dateStringHijri,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (ev.eventType == EventType.WILADAT) Color(0xFF00BFA5) else MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { selectedMasoom = null },
                            modifier = Modifier.align(Alignment.CenterEnd),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (languageCode == "ur") "بند کریں" else if (languageCode == "ar") "إغلاق" else "Close")
                        }
                    }
                }
            }
        }
    }
}
