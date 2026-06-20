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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

data class NameOfAllah(
    val id: Int,
    val arabic: String,
    val transliteration: String,
    val english: String,
    val urdu: String,
    val benefit: String,
    val benefitUrdu: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NamesOfAllahScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    languageCode: String = "en"
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedName by remember { mutableStateOf<NameOfAllah?>(null) }
    var activeCounter by remember { mutableStateOf(0) }

    val names = remember {
        listOf(
            NameOfAllah(1, "الرَّحْمَنُ", "Ar-Rahman", "The Beneficent", "نہایت مہربان", "Reciting 100 times after Fajr prayer keeps heart focused.", "ہر نماز کے بعد 100 بار پڑھنے سے دل غفلت سے پاک ہوتا ہے۔"),
            NameOfAllah(2, "الرَّحِيمُ", "Ar-Rahim", "The Merciful", "نہایت رحم کرنے والا", "Reciting 100 times after every prayer keeps one safe from all calamities.", "روزی میں برکت اور ہر آفت سے حفاظت کے لیے مغرب کے بعد کثرت سے پڑھیں۔"),
            NameOfAllah(3, "الْمَلِكُ", "Al-Malik", "The King / Sovereign", "حقیقی بادشاہ", "A person who recites Al-Malik excessively will gain self-respect.", "زوال کے وقت کثرت سے تلاوت کرنے والے کو غنا اور عزت عطا کی جاتی ہے۔"),
            NameOfAllah(4, "الْقُدُّوسُ", "Al-Quddus", "The Most Sacred", "انتہائی پاکیزہ", "Reciting daily cleanses the heart from spiritual diseases.", "دل کو وسوسوں اور روحانی امراض سے صاف کرنے کے لیے کثرت سے پڑھیں۔"),
            NameOfAllah(5, "السَّلَامُ", "As-Salam", "The Giver of Peace", "سراسر سلامتی والا", "Reciting 160 times over a sick person helps them recover.", "بیمار پر 160 بار پڑھ کر دم کرنے سے شفا حاصل ہوتی ہے۔"),
            NameOfAllah(6, "الْمُؤْمِنُ", "Al-Mu'min", "The Infuser of Faith", "امن و امان دینے والا", "Reciting Al-Mu'min protects from the evil of enemies.", "ہر خوف اور دشمن کے شر سے پناہ کے لیے کثرت سے پڑھیں۔"),
            NameOfAllah(7, "الْمُهَيْمِنُ", "Al-Muhaymin", "The Preserver / Guardian", "نگہبان و پاسبان", "Reciting offers inner and outer purification.", "غسل کے بعد اکیلے میں 100 بار پڑھنے سے باطن روشن ہو جاتا ہے۔"),
            NameOfAllah(8, "الْعَزِيزُ", "Al-Aziz", "The Mighty One", "سب پر غالب", "Reciting 40 times after Fajr makes the reciter self-reliant.", "فجر کے بعد 40 بار مسلسل پڑھنے سے کسی کا محتاج نہیں رہتا۔"),
            NameOfAllah(9, "الْجَبَّارُ", "Al-Jabbar", "The Omnipotent", "زبردست عمال رکھنے والا", "Reciting protects from violence and tyranny.", "صبح و شام 226 بار پڑھنے سے ظالموں کے ظلم سے امان ملتی ہے۔"),
            NameOfAllah(10, "الْمُتَكَبِّرُ", "Al-Mutakabbir", "The Supreme / Majestic", "بڑائی والا", "Reciting before starting a task ensures its success.", "شروع کام سے پہلے پڑھنے سے برکت اور کامیابی حاصل ہوتی ہے۔"),
            NameOfAllah(11, "الْخَالِقُ", "Al-Khaliq", "The Creator", "پیدا کرنے والا", "Allah creates an angel to pray for the one who recites at night.", "آدھی رات کو کثرت سے پڑھنے سے اللہ پاک دلوں کو منور فرماتا ہے۔"),
            NameOfAllah(12, "الْبَارِئُ", "Al-Bari'", "The Maker / Evolver", "درست بنانے والا", "Reciting helps ease severe diseases.", "بیماریوں اور مصائب سے نجات کے لیے کثرت سے تلاوت کریں۔"),
            NameOfAllah(13, "الْمُصَوِّرُ", "Al-Musawwir", "The Fashioner", "خوبصورت بنانے والا", "Fast for 7 days and recite 21 times for offspring blessings.", "7 دن روزے رکھ کر افطار پر 21 بار پڑھنے سے اولاد صالحہ ملتی ہے۔"),
            NameOfAllah(14, "الْغَفَّارُ", "Al-Ghaffar", "The All-Forgiving", "بہت زیادہ بخشنے والا", "Reciting after Jummah prayer eases financial debts.", "جمعہ کی نماز کے بعد کثرت سے پڑھنے سے گناہ معاف اور پریشانیاں دور ہوتی ہیں۔"),
            NameOfAllah(15, "الْقَهَّارُ", "Al-Qahhar", "The Subduer", "سب کو بس میں رکھنے والا", "Conquers inner ego and purges carnal desires.", "نفسِ لوامہ پر قابو پانے کے لیے کثرت سے ورد کریں۔"),
            NameOfAllah(16, "الْوَهَّابُ", "Al-Wahhab", "The Giver of All", "سب کچھ عطا کرنے والا", "Reciting 7 times during Sajda grants prayers.", "سجدے میں 7 بار گرداگر پڑھنے سے دعائیں قبول ہوتی ہیں اور غنا ملتا ہے۔"),
            NameOfAllah(17, "الرَّزَّاقُ", "Ar-Razzaq", "The Provider", "بہتر رزق دینے والا", "Reciting 10 times in corner of house increases sustenance.", "گھر کے چاروں کونوں میں فجر سے پہلے 10 بار پڑھنے سے رزق کھلتا ہے۔"),
            NameOfAllah(18, "الْفَتَّاحُ", "Al-Fattah", "The Opener", "مشکل کشا / کھولنے والا", "Reciting 70 times after Fajr with hands on chest opens the heart.", "فجر کے بعد دونوں ہاتھ چھاتی پر رکھ کر 70 بار پڑھنے سے دل منور ہوتا ہے۔"),
            NameOfAllah(19, "الْعَلِيمُ", "Al-Alim", "The All-Knowing", "بہت علم والا", "Reciting Al-Alim illuminates the intellect and soul.", "کثرت سے پڑھنے سے علم و حکمت کی راہیں کھل جاتی ہیں۔"),
            NameOfAllah(20, "الْقَابِضُ", "Al-Qabid", "The Withholder / Constrictor", "تنگ کرنے والا", "Reciting writes off spiritual distress and anxiety.", "مشکلات میں صبر اور تنگی دور کرنے کے لئے 40 دن تک روٹی پر لکھ کر کھائیں۔"),
            NameOfAllah(21, "الْبَاسِطُ", "Al-Basit", "The Expander", "کشادہ کرنے والا", "Reciting 10 times after Duha with raised hands brings wealth.", "نمازِ چاشت کے بعد 10 بار ہاتھ اٹھا کر تلاوت کر کے چہرے پر پھیریں۔"),
            NameOfAllah(22, "الْخَافِضُ", "Al-Khafid", "The Abaser", "پست کرنے والا", "Allah lowers the status of enemies of truth who recite this.", "تین دن روزے رکھ کر چوتھے دن مجلس میں 100 بار پڑھیں، فتح نصیب ہوگی۔"),
            NameOfAllah(23, "الرَّافِعُ", "Ar-Rafi", "The Exalter", "بلند کرنے والا", "Increases honor and prestige in the community.", "آدھی رات کو 100 بار پڑھنے سے عزت و رفعت حاصل ہوتی ہے۔"),
            NameOfAllah(24, "الْمُعِزُّ", "Al-Mu'izz", "The Bestower of Honor", "عزت دینے والا", "Reciting after Maghrib on Mondays/Fridays grants respect.", "پیر اور جمعہ کی رات عشا کے بعد 140 بار پڑھنے سے ہیبت اور عزت نصیب ہوگی۔"),
            NameOfAllah(25, "الْمُذِلُّ", "Al-Mudhill", "The Dishonorer", "ذلت دینے والا", "Reciting 75 times keeps one safe from jealousy.", "75 بار پڑھ کر سجدے میں دعا مانگنے سے حاسدوں سے امان ملتی ہے۔"),
            NameOfAllah(26, "السَّمِيعُ", "As-Sami'", "The All-Hearing", "سب کچھ سننے والا", "Reciting Al-Sami' on Thursday after breakfast ensures prayers.", "جمعرات کو چاشت کے بعد 100 بار پڑھنے سے دعا قبول کی جاتی ہے۔"),
            NameOfAllah(27, "الْبَصِيرُ", "Al-Basir", "The All-Seeing", "سب کچھ دیکھنے والا", "Reciting 100 times after Jummah prayer improves eyesight.", "جمعہ کے بعد 100 بار پڑھنے سے نظر اور بصیرت تیز ہوتی ہے۔"),
            NameOfAllah(28, "الْحَكَمُ", "Al-Hakam", "The Judge", "بہتر فیصلہ کرنے والا", "Allah reveals secrets to those who recite Al-Hakam at night.", "رات کے آخری حصے میں 99 بار پڑھنے سے باطنی اسرار عیاں ہوتے ہیں۔"),
            NameOfAllah(29, "الْعَدْلُ", "Al-Adl", "The Utterly Just", "سراسر عدل کرنے والا", "Reciting Al-Adl brings obedience and discipline nearby.", "جمعہ کی رات روٹی کے نوالے پر لکھ کر کھانے سے دل میں اطاعت پیدا ہوتی ہے۔"),
            NameOfAllah(30, "اللَّطِيفُ", "Al-Latif", "The Subtle / Gentle", "نہایت مہربان و باریک بین", "Reciting 133 times brings immense peace and eases hardship.", "133 بار پڑھنے سے رزق میں برکت اور کاموں میں آسانی ہوتی ہے۔"),
            NameOfAllah(31, "الْخَبِيرُ", "Al-Khabir", "The All-Aware", "ہر چیز سے واقف", "Reveals secret information in dreams when recited for 7 days.", "سات دن مسلسل کثرت سے پڑھنے سے غیبی باتیں معلوم ہوتی ہیں۔"),
            NameOfAllah(32, "الْحَلِيمُ", "Al-Halim", "The Forbearing", "بردبار اور نرم خو", "Safeguards crops and business properties.", "کاغذ پر لکھ کر دکان یا کھیت میں رکھنے سے آفات سے حفاظت رہتی ہے۔"),
            NameOfAllah(33, "الْعَظِيمُ", "Al-Azim", "The Magnificent", "بہت عظمت والا", "Reciter is respected by dignitaries and elites.", "کثرت سے پڑھنے والے کو اللہ پاک عزت و وقار عطا فرماتا ہے۔"),
            NameOfAllah(34, "الْغَفُورُ", "Al-Ghafur", "The Most Forgiving", "بہت بخشنے والا", "Eases physical headache and spiritual grief.", "دکھ درد اور گناہوں کی معافی کے لیے بکثرت پڑھیں۔"),
            NameOfAllah(35, "الشَّكُورُ", "Ash-Shakur", "The Grateful", "قدردان", "Brings relief from chest heaviness and sorrow.", "پانی پر 41 بار دم کر کے پینے سے دل کی اداسی دور ہوتی ہے۔"),
            NameOfAllah(36, "الْعَلِيُّ", "Al-Aliyy", "The Most High", "بہت بلند مرتبہ", "Brings success, elevation, and prosperity globally.", "ہمیشہ اپنے پاس لکھ کر رکھنے سے بلندی مراتب حاصل ہوتی ہے۔"),
            NameOfAllah(37, "الْكَبِيرُ", "Al-Kabir", "The Greatest", "سب سے بڑا کیبریا والا", "Those who fast and recite Al-Kabir gain high status.", "برطرفی یا تنزلی سے بچنے کے لیے کثرت سے ورد کریں۔"),
            NameOfAllah(38, "الْحَفِيظُ", "Al-Hafiz", "The Preserver / Protector", "حفاظت کرنے والا", "Protects from sudden death and travel accidents.", "سفر پر روانہ ہوتے وقت پڑھنے سے ہر قسم کا حادثہ ٹل جاتا ہے۔"),
            NameOfAllah(39, "الْمُقِيتُ", "Al-Muqit", "The Nourisher", "توانائی فراہم کرنے والا", "Reciting over water and drinking it quenches extreme thirst.", "خالی برتن پر پڑھ کر دم کرنے سے رزق کے اسباب پیدا ہوتے ہیں۔"),
            NameOfAllah(40, "الْحَسِيبُ", "Al-Hasib", "The Accounter", "حساب لینے والا", "Recite 70 times starting Thursday to defend against fear.", "جمعرات سے شروع کر کے روزانہ 70 بار پڑھنے سے ہر نقصان سے بچاؤ ہوتا ہے۔"),
            NameOfAllah(41, "الْجَلِيلُ", "Al-Jalil", "The Mighty / Glorious", "عظمت و جلال والا", "Brings awe, majesty, and love to the reciter's presence.", "مشک و زعفران سے لکھ کر پاس رکھنے سے خلقِ خدا محبت کرتی ہے۔"),
            NameOfAllah(42, "الْكَرِيمُ", "Al-Karim", "The Most Generous", "بہت مہربان و سخی", "Gives prestige in both worlds.", "بستر پر سوتے وقت پڑھنے سے فرشتے عزت کی دعا کرتے ہیں۔"),
            NameOfAllah(43, "الرَّقِيبُ", "Ar-Raqib", "The Watchful", "نگہبان", "Protects family, children, and properties from safe distance.", "اہل و عیال پر دم کرنے سے وہ اللہ کی خاص امان میں آ جاتے ہیں۔"),
            NameOfAllah(44, "الْمُجِيبُ", "Al-Mujib", "The Responsive", "امیدیں پوری کرنے والا", "Acceptance of all legal prayers and needs.", "کثرت سے تلاوت کرنے سے حاجتیں اور دعائیں قبول ہوتی ہیں۔"),
            NameOfAllah(45, "الْوَاسِعُ", "Al-Wasi'", "The Vast / All-Encompassing", "کشادہ اور وسیع", "Removes severe dependency and financial constraints.", "تنگی رزق کے وقت کثرت سے پڑھنے سے روزی کشادہ ہو جاتی ہے۔"),
            NameOfAllah(46, "الْحَكِيمُ", "Al-Hakim", "The Wise", "نہایت دانا اور حکمت والا", "Allah opens gates of spiritual wisdom.", "جس کام میں رکاوٹ ہو، کثرت سے پڑھنے سے کامیابی ملتی ہے۔"),
            NameOfAllah(47, "الْوَدُودُ", "Al-Wadud", "The Loving One", "محبت کرنے والا", "Solves domestic fights and family issues.", "ہزار بار پڑھ کر کھانے پر دم کر کے میاں بیوی کھائیں تو محبت بڑھتی ہے۔"),
            NameOfAllah(48, "الْمَجِيدُ", "Al-Majid", "The Majestic / Glorious", "بزرگی والا", "Healing from physical skin ailments.", "ایامِ بیض (13، 14، 15 ہجری) کے روزے رکھ کر کثرت سے تلاوت کریں تو شفا ملتی ہے۔"),
            NameOfAllah(49, "الْبَاعِثُ", "Al-Ba'ith", "The Resurrector", "مردوں کو اٹھانے والا", "Creates fear of Allah and piety in heart.", "سوتے وقت سینے پر ہاتھ رکھ کر 101 بار پڑھنے سے علم و حکمت ملتی ہے۔"),
            NameOfAllah(50, "الشَّهِيدُ", "Ash-Shahid", "The Witness", "گواہ اور حاضر", "Finds lost valuables and disobedient children obey.", "باغی اولاد یا ملازم کے سر کے بال پکڑ کر صبح 21 بار پڑھ کر دم کریں۔"),
            NameOfAllah(51, "الْحَقُّ", "Al-Haqq", "The Truth", "سچا اور ثابت", "Reciting brings back missing family members.", "گمشدہ چیز یا قیدی کی رہائی کے لیے چکور کاغذ پر لکھ کر دعا مانگیں۔"),
            NameOfAllah(52, "الْوَكِيلُ", "Al-Wakil", "The Trustee", "کارساز اور مددگار", "Allah becomes the ultimate protector.", "طوفان یا شدید خوف کے وقت پڑھنے سے فوراً مدد ملتی ہے۔"),
            NameOfAllah(53, "الْقَوِيُّ", "Al-Qawiyy", "The All-Strong", "بے پناہ طاقتور", "Protects from victimizing oppressors.", "مغلوب اور کمزور شخص دشمن کے شر سے بچنے کے لیے ورد کرے۔"),
            NameOfAllah(54, "الْمَتِينُ", "Al-Matin", "The Firm", "مستحکم قوت والا", "Cures milk deficiency in nursing mothers.", "پانی پر دم کر کے پلانے سے جسمانی اور باطنی طاقت ملتی ہے۔"),
            NameOfAllah(55, "الْوَلِيُّ", "Al-Waliyy", "The Protector / Supporter", "حمایتی اور سرپرست", "Reciter becomes a close friend of Allah.", "بیوی کی بدزبانی یا نافرمانی دور کرنے کے لیے اس کے سامنے پڑھیں۔"),
            NameOfAllah(56, "الْحَمِيدُ", "Al-Hamid", "The Praiseworthy", "لائقِ تعریف", "Speech becomes soft and loved by everyone.", "تنہائی میں 45 بار پڑھنے سے عاداتِ جمیلہ پیدا ہوتی ہیں۔"),
            NameOfAllah(57, "الْمُحْصِي", "Al-Muhsi", "The Appraiser / Counter", "شمار کرنے والا", "Eases accountability on the Day of Judgment.", "روزانہ 20 بار روٹی پر دم کر کے کھانے سے ذہن رسا ہوتا ہے۔"),
            NameOfAllah(58, "الْمُبْدِئُ", "Al-Mubdi'", "The Originator", "پہلی بار پیدا کرنے والا", "Prevents miscarriage in pregnant mothers.", "حاملہ عورت کے پیٹ پر انگلی سے گول دائرہ بنا کر 99 بار پڑھیں۔"),
            NameOfAllah(59, "الْمُعِيدُ", "Al-Mu'id", "The Restorer", "دوبارہ پیدا کرنے والا", "Brings back lost assets safely.", "گمشدہ شخص کی واپسی کے لیے آدھی رات کو چاروں کونوں میں پڑھیں۔"),
            NameOfAllah(60, "الْمُحْيِي", "Al-Muhyi", "The Giver of Life", "زندگی بخشنے والا", "Eases physical body and chest pain.", "اپنے اوپر دم کرنے سے قید اور سخت بیماری سے خلاصی ملتی ہے۔"),
            NameOfAllah(61, "الْمُمِيتُ", "Al-Mumit", "The Bringer of Death", "موت دینے والا", "Destroys evil sins and bad desires.", "سوتے وقت سینے پر ہاتھ رکھ کر پڑھنے سے نفس مغلوب ہوتا ہے۔"),
            NameOfAllah(62, "الْحَيُّ", "Al-Hayy", "The Ever-Living", "ہمیشہ زندہ رہنے والا", "Reciter gains longevity and good health.", "روزانہ فجر کے بعد 3000 بار پڑھنے سے کبھی سخت بیمار نہیں ہوگا۔"),
            NameOfAllah(63, "الْقَيُّومُ", "Al-Qayyum", "The Self-Subsisting", "سب کا تھامنے والا", "Allah prevents laziness and slumber.", "تھکن اور سستی دور کرنے کے لیے فجر کے وقت بکثرت پڑھیں۔"),
            NameOfAllah(64, "الْوَاجِدُ", "Al-Wajid", "The Finder", "حاصل کرنے والا", "Generates abundance of generosity in heart.", "کھانا کھاتے وقت ہر لقمے پر پڑھنے سے باطن نورانی ہوتا ہے۔"),
            NameOfAllah(65, "الْمَاجِدُ", "Al-Majid", "The Noble", "بزرگ و برتر", "Illuminates heart with divine Shia guidance.", "تنہائی میں کثرت سے اخلاص کے ساتھ پڑھیں تو معرفت ملتی ہے۔"),
            NameOfAllah(66, "الْوَاحِدُ", "Al-Wahid", "The One", "اکیلا اور تنہا", "Removes fear of people and brings courage.", "اکیلا رہ جانے کے خوف کو دور کرنے کے لئے 1000 بار پڑھیں۔"),
            NameOfAllah(67, "الأَحَدُ", "Al-Ahad", "The Unique", "واحد و یکتا", "Reveals real divine truths to the soul.", "تنہائی میں روزانہ 1000 بار پڑھنے سے فرشتے دعا کرتے ہیں۔"),
            NameOfAllah(68, "الصَّمَدُ", "As-Samad", "The Eternal / Self-Sufficient", "بے نیاز اور صمد", "Never feels hunger or extreme poverty.", "باوضو سجدے میں کثرت سے پڑھنے سے مخلوق کی محتاجی دور ہوتی ہے۔"),
            NameOfAllah(69, "الْقَادِرُ", "Al-Qadir", "The Capable", "قادر مطلق", "Fulfills requests and defeats difficulties.", "دو رکعت نفل کے بعد 100 بار پڑھنے سے بند کام کھل جاتے ہیں۔"),
            NameOfAllah(70, "الْمُقْتَدِرُ", "Al-Muqtadir", "The Omnipotent", "کامل اقتدار والا", "Waking up and reciting makes things easy.", "سو کر اٹھتے ہی پڑھنے سے اس دن کے تمام حوائج آسان ہوتے ہیں۔"),
            NameOfAllah(71, "الْمُقَدِّمُ", "Al-Muqaddim", "The Expediter", "آگے کرنے والا", "Allah grants courage on battlefields and exams.", "امتحان یا میدان جنگ میں کثرت سے پڑھنے سے کامیابی ملتی ہے۔"),
            NameOfAllah(72, "الْمُؤَخِّرُ", "Al-Mu'akhkhir", "The Delayer", "پیچھے کرنے والا", "Brings absolute focus to the creator.", "روزانہ 100 بار پڑھنے سے اللہ کی سچی محبت نصیب ہوتی ہے۔"),
            NameOfAllah(73, "الأَوَّلُ", "Al-Awwal", "The First", "سب سے پہلے رہنے والا", "Recite 100 times for safe travels and childbirth.", "مسافر بٹھا کر 100 بار پڑھے تو بخیریت واپس لوٹتا ہے۔"),
            NameOfAllah(74, "الأَخِرُ", "Al-Akhir", "The Last", "ہمیشہ باقی رہنے والا", "Reciting ensures a peaceful death.", "روزانہ عشا کے بعد 100 بار تلاوت کرنے سے خاتمہ بالخیر ہوتا ہے۔"),
            NameOfAllah(75, "الظَّاهِرُ", "Az-Zahir", "The Manifest", "سب پر عیاں", "Allah adds light to the reciter's vision.", "اشراق کے بعد 500 بار پڑھنے سے آنکھوں کی روشنی بڑھتی ہے۔"),
            NameOfAllah(76, "الْبَاطِنُ", "Al-Batin", "The Hidden", "پوشیدہ اور باطن", "Allah reveals spiritual secrets.", "روزانہ 33 بار پڑھنے سے باطنی انوار عیاں ہوتے ہیں۔"),
            NameOfAllah(77, "الْوَالِي", "Al-Wali", "The Patron / Governor", "مالک و مختار", "Secures home and property from celestial distress.", "مٹی کے برتن پر لکھ کر پانی سے دھو کر گھر میں چھڑکیں مصیبت ٹلے گی۔"),
            NameOfAllah(78, "الْمُتَعَالِي", "Al-Muta'ali", "The Supreme Exalted", "نہایت بلند مرتبہ", "Allah solves difficult court matters.", "مشکلات میں کثرت سے پڑھنے سے آسانی پیدا ہوتی ہے۔"),
            NameOfAllah(79, "الْبَرُّ", "Al-Barr", "The Source of All Goodness", "سب کا محسن", "Safeguards child from all harm.", "بچے کی پیدائش پر پڑھنے سے وہ حادثات سے محفوظ رہے گا۔"),
            NameOfAllah(80, "التَّوَّابُ", "At-Tawwab", "The Acceptor of Repentance", "توبہ قبول کرنے والا", "Allah accepts sincere repentance quickly.", "نمازِ چاشت کے بعد 360 بار پڑھنے سے سچی توبہ نصیب ہوتی ہے۔"),
            NameOfAllah(81, "الْمُنْتَقِمُ", "Al-Muntaqim", "The Avenger", "بدلہ لینے والا", "Allah overcomes dangerous enemies.", "ظالم کے شر سے مغلوب شخص تین جمعہ تک کثرت سے ورد کرے۔"),
            NameOfAllah(82, "الْعَفُوُّ", "Al-Afuww", "The Pardoner", "بہت زیادہ معاف کرنے والا", "Allah forgives severe life faults.", "کثرت سے پڑھنے سے گناہ جھڑ جاتے ہیں اور عفو ملتی ہے۔"),
            NameOfAllah(83, "الرَّؤُوفُ", "Ar-Ra'uf", "The Most Kind", "نہایت شفقت کرنے والا", "Allah adds mercy and love inside people's hearts.", "درود پاک کے ساتھ کثرت سے پڑھنے سے خلق مہربان ہوتی ہے۔"),
            NameOfAllah(84, "مَالِكُ الْمُلْكِ", "Malik-ul-Mulk", "Owner of All Sovereignty", "مالک کائنات", "Removes severe doubts and dependency.", "ہمیشہ کثرت سے پڑھنے سے محتاجی دور اور غنا نصیب ہوتا ہے۔"),
            NameOfAllah(85, "ذُو الْجَلَالِ وَالإِكْرَامِ", "Dhul-Jalal-wal-Ikram", "Lord of Majesty and Bounty", "جلال اور بخشش والا", "Acceptance of requests directly by Allah.", "ہر نماز کے بعد کثرت سے پڑھنے سے رزق فراخ ہوتا ہے۔"),
            NameOfAllah(86, "الْمُقْسِطُ", "Al-Muqsit", "The Equitable One", "انصاف پسند", "Eases severe mental distress and anger.", "وسوسوں اور پریشان خیالی سے نجات کے لیے کثرت سے پڑھیں۔"),
            NameOfAllah(87, "الْجَامِعُ", "Al-Jami'", "The Gatherer", "سب کو اکٹھا کرنے والا", "Brings back separated family members safely.", "گمشدہ چیز یا بچھڑے ہوؤں کو ملانے کے لئے کثرت سے ورد کریں۔"),
            NameOfAllah(88, "الْغَنِيُّ", "Al-Ghaniyy", "The Rich One", "سب سے غنی و بے نیاز", "Guarantees physical security and richness.", "بیماری یا تنگدستی کے وقت اس کا کثرت سے تلاوت کرنا غنی بناتا ہے۔"),
            NameOfAllah(89, "الْمُغْنِي", "Al-Mughni", "The Enricher", "غنی کرنے والا", "Solves domestic tension and makes rich.", "بستر پر لیٹ کر دل میں پڑھنے سے میاں بیوی کی ناراضگی دور ہوتی ہے۔"),
            NameOfAllah(90, "الْمَانِعُ", "Al-Mani'", "The Preventer", "روکنے والا", "Allah makes safety around from negative vibes.", "میاں بیوی میں جھگڑا ختم کرنے کے لئے سوتے وقت 20 بار پڑھیں۔"),
            NameOfAllah(91, "الضَّارُّ", "Ad-Darr", "The Distresser", "نقصان کا مالک", "Allah raises values of the reciter.", "جمعہ کی رات 100 بار پڑھنے سے باطنی اور ظاہری بلائیں ٹلتی ہیں۔"),
            NameOfAllah(92, "النَّافِعُ", "An-Nafi'", "The Propitious", "نفع کا مالک", "Allah grants smooth success in plans.", "ہر کام کی چابی فجر سے پہلے 100 بار پڑھنے میں پوشیدہ ہے۔"),
            NameOfAllah(93, "النُّورُ", "An-Nur", "The Light", "سراسر روشنی و نور", "Allah illuminates internal self.", "جمعہ کی رات سورہ نور کے ساتھ 1001 بار پڑھنے سے باطن منور ہوتا ہے۔"),
            NameOfAllah(94, "الْهَادِي", "Al-Hadi", "The Guide", "راہِ راست دکھانے والا", "Allah grants total wisdom and right way.", "روزانہ صبح دونوں ہاتھ چہرے پر پھیر کر 100 بار پڑھنے سے زہد ملتا ہے۔"),
            NameOfAllah(95, "الْبَدِيعُ", "Al-Badi'", "The Incomparable", "عدیم المثال", "Guarantees fulfillment of tasks.", "مغرب کے بعد عشا تک 1000 بار پڑھنے سے مشکل کام آسان ہو جاتے ہیں۔"),
            NameOfAllah(96, "الْبَاقِي", "Al-Baqi", "The Everlasting", "ہمیشہ باقی رہنے والا", "Allah accepts your good deeds forever.", "جمعہ کی رات 1000 بار پڑھنے سے اعمال ضائع ہونے سے بچتے ہیں۔"),
            NameOfAllah(97, "الْوَارِثُ", "Al-Warith", "The Supreme Inheritor", "سب کا وارث", "Saves from sorrow and grief.", "غروبِ آفتاب کے بعد 100 بار پڑھنے سے خوف و حزن سے نجات ملتی ہے۔"),
            NameOfAllah(98, "الرَّشِيدُ", "Ar-Rashid", "The Guide to Path", "سیدھا راستہ دکھانے والا", "Solves all legal difficulties automatically.", "عشا کے بعد 1000 بار پڑھنے سے تمام بگڑے کام بن جاتے ہیں۔"),
            NameOfAllah(99, "الصَّبُورُ", "As-Sabur", "The Extolled Patient", "نہایت صبر و تحمل والا", "Removes calamities and bestows patience.", "سورج نکلنے سے پہلے 100 بار پڑھنے سے مصیبت اور دشمن کے منہ سے امان ملتی ہے۔")
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
                            text = if (languageCode == "ur") "اللہ کے 99 نام" else "99 Names of Allah",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (languageCode == "ur") "اسماء اللہ الحسنیٰ پڑھیں اور یاد کریں" else "Asma-ul-Husna recitation & benefits",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("allah_names_back")
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
        modifier = modifier.testTag("names_of_allah_root")
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
                            text = if (languageCode == "ur") "ذکر تسبیح اسماء الحسنیٰ" else "Tasbeeh of Sacred Names",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (languageCode == "ur") "کاؤنٹر: $activeCounter" else "Recitation Count: $activeCounter",
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
                        text = if (languageCode == "ur") "تلاش کریں..." else "Search name or meaning...",
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
                            .testTag("allah_name_item_${item.id}")
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
                    .testTag("allah_name_detail_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "NAME OF ALLAH (SWT) #${item.id}",
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
                                text = "English Meaning",
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
                                text = "Urdu Translation (ترجمہ)",
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
                                text = "Spiritual Benefits & Virtues (فضائل و برکات)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.benefit,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.benefitUrdu,
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
