package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import com.example.data.MasoomeenData
import com.example.data.ZiyaratItem

data class ZiyaratLocation(
    val id: String,
    val nameEn: String,
    val nameAr: String,
    val nameUr: String,
    val lat: Double,
    val lon: Double,
    val descEn: String,
    val descAr: String,
    val descUr: String,
    val cityEn: String,
    val cityAr: String,
    val cityUr: String,
    val tipsEn: String,
    val tipsAr: String,
    val tipsUr: String,
    val masoomeenIndices: List<Int> = emptyList()
)

val ziyaratLocations = listOf(
    ZiyaratLocation(
        id = "medina_prophet",
        nameEn = "Al-Masjid an-Nabawi (Holy Prophet s.a.w.w.)",
        nameAr = "المسجد النبوي الشريف (الرسول الأكرم ص)",
        nameUr = "مسجد نبوی شریف (خاتم المرسلین حضرت محمد مصطفیٰ صلی اللہ علیہ وآلہ وسلم)",
        lat = 24.4672,
        lon = 39.6111,
        cityEn = "Medina, Saudi Arabia",
        cityAr = "المدينة المنورة، السعودية",
        cityUr = "مدینہ منورہ، سعودی عرب",
        descEn = "The magnificent Prophet's Mosque containing the holy shrine of the Savior of Humanity, Prophet Muhammad (s.a.w.w.), the 1st of the 14 Infallibles (Masoomeen). It is the second holiest site in Islam.",
        descAr = "المسجد النبوي الشريف الذي يضم المرقد الطاهر لخاتم الأنبياء والمرسلين نبي الرحمة محمد (ص)، أول المعصومين الأربعة عشر.",
        descUr = "مسجدِ نبوی شریف جہاں تمام انسانیت کے نجات دہندہ، خاتم النبیین حضرت محمد مصطفیٰ صلی اللہ علیہ وآلہ وسلم کا روضہ اطہر موجود ہے، جو کہ چودہ معصومینؑ میں سب سے پہلے ہیں۔",
        tipsEn = "Recommended attire: Plain, formal conservative clothing. Women must cover their heads completely. Best time to visit: Late evening (after Isha) or third portion of the night. Local etiquette: Enter with extreme humility (Khushu), keep your voice lowered, recite Ziyarat with peace, and avoid taking large cameras.",
        tipsAr = "الزي الموصى به: الملابس البسيطة والساترة جداً مع تغطية الرأس بالكامل للنساء. أفضل وقت للزيارة: في أواخر الليل أو بعد صلاة العشاء. الآداب المحلية: الدخول بخشوع تام، خفض الصوت عند قبر النبي الكريم، وتجنب التصوير الصاخب.",
        tipsUr = "تجویز کردہ لباس: نہایت سادہ، صاف اور باوقار شرعی لباس پہنیں۔ زیارت کا بہترین وقت: نمازِ عشا کے بعد یا رات کا آخری پہر۔ مقامی آداب: انتہائی عاجزی اور خشوع کے ساتھ داخل ہوں، آواز کو دھیما رکھیں، اور بغیر کسی شور کے صلوٰۃ و سلام پیش کریں۔",
        masoomeenIndices = listOf(1)
    ),
    ZiyaratLocation(
        id = "medina_zahra",
        nameEn = "Sanctuary of Lady Fatima al-Zahra (s.a.)",
        nameAr = "مقام سيدة نساء العالمين فاطمة الزهراء (ع)",
        nameUr = "مقامِ سیدہ نساء العالمین حضرت فاطمہ زہرا سلام اللہ علیہا",
        lat = 24.4676,
        lon = 39.6115,
        cityEn = "Medina, Saudi Arabia",
        cityAr = "المدينة المنورة، السعودية",
        cityUr = "مدینہ منورہ، سعودی عرب",
        descEn = "The spiritual presence and home of the beloved daughter of Prophet Muhammad (s.a.w.w.), wife of Imam Ali (a.s.), and mother of the Infallible Imams - the 2nd of the 14 Infallibles. Her precise burial place remains hidden or associated with Riyadhul Jannah / Jannat al-Baqi.",
        descAr = "المكان الروحي والمقام الطاهر لبضعة الرسول الأكرم، وسيدة نساء العالمين فاطمة الزهراء (ع)، زوجة أمير المؤمنين وأم الأئمة الأطهار - المعصومة الثانية.",
        descUr = "سرورِ کائناتؐ کی لخت جگر، امیر المومنینؑ کی شریک حیات، اور معصوم ائمہ کی مادرِ گرامی حضرت فاطمہ زہرا سلام اللہ علیہا کا مقامِ روحانی؛ جو کہ دوسری معصومہ ہیں۔ روایات کے مطابق آپ کا مزار اقدس پوشیدہ ہے یا جنت البقیع / ریاض الجنہ میں ہے۔",
        tipsEn = "Recommended attire: Strictly modest conservative attire. Best time to visit: During times of prayer when Masjid an-Nabawi is open. Local etiquette: Focus on deep internal reflection, pray inside Riyadhul Jannah (between the Prophet's house and his pulpit), and recite the unique Ziyarat of Lady Zahra (s.a.) quietly.",
        tipsAr = "الزي الموصى به: الملابس المحتشمة والساترة جداً. أفضل وقت للزيارة: أوقات الصلوات المكتوبة. الآداب المحلية: استحضار عظمة الصديقة الطاهرة، والدعاء بخشوع في الروضة الشريفة (ما بين بيت النبي ومنبره) وقراءة زيارتها المأثورة بهدوء.",
        tipsUr = "تجویز کردہ لباس: مکمل باوقار اور شرعی پردہ لازمی ہے۔ زیارت کا بہترین وقت: نمازوں کے اوقات کے دوران۔ مقامی آداب: خاتونِ جنت کی عظمت کا دل میں احترام قائم کریں، ریاض الجنہ (مبارک منبر اور کاشانہ رسولؐ کے درمیان) میں نماز ادا کریں اور نہایت راز داری و سکون سے زیارت پڑھیں۔",
        masoomeenIndices = listOf(3)
    ),
    ZiyaratLocation(
        id = "najaf",
        nameEn = "Shrine of Imam Ali (a.s.)",
        nameAr = "مرقد الإمام علي بن أبي طالب (ع)",
        nameUr = "روضہ مبارک حضرت امام علی علیہ السلام",
        lat = 31.9961,
        lon = 44.3524,
        cityEn = "Najaf, Iraq",
        cityAr = "النجف الأشرف، العراق",
        cityUr = "نجف اشرف، عراق",
        descEn = "The holy sanctuary of the Commander of the Faithful, Imam Ali ibn Abi Talib (a.s.), the cousin and son-in-law of Prophet Muhammad (s.a.w.w.) and the 3rd Masoom (1st Imam). It is a major center of Shia scholarship and spirituality.",
        descAr = "المرقد المقدس لأمير المؤمنين الإمام علي بن أبي طالب (ع)، وهو مركز رئيسي للعلوم الدينية والروحانية وجهاد الأئمة المعصومين.",
        descUr = "امیر المومنین حضرت علی ابن ابی طالب علیہ السلام کا روضہ مبارک، جو علم و معرفت اور روحانیت کا عظیم مرکز ہے۔",
        tipsEn = "Recommended attire: Modest traditional clothing (Abaya for women). Best time to visit: Early morning (after Fajr) or late at night (after 10 PM) to avoid extreme heat and crowds. Local etiquette: Perform wudhu (ablution) before entering, enter with your right foot first, maintain silence, and follow directions from local guides.",
        tipsAr = "الزي الموصى به: الملابس التقليدية المحتشمة (العباءة والچادر للنساء). أفضل وقت للزيارة: الصباح الباكر (بعد الفجر) أو في وقت متأخر من الليل (بعد الساعة 10 مساءً) لتجنب الحرارة والزحام الشديد. الآداب المحلية: الوضوء قبل الدخول، الدخول بالقدم اليمنى، التزام الهدوء، واتباع إرشادات اللجان المنظمة.",
        tipsUr = "تجویز کردہ لباس: باوقار اور روایتی لباس (خواتین کے لیے عبایا یا چادر لازمی ہے)۔ زیارت کا بہترین وقت: صبح سویرے (نمازِ فجر کے بعد) یا دیر رات (رات 10 بجے کے بعد) تاور گرمی اور ہجوم سے بچا جا سکے۔ مقامی آداب: داخلے سے پہلے وضو کریں، دایاں پاؤں پہلے رکھیں، خاموشی برقرار رکھیں، اور خدام کی ہدایات پر عمل کریں۔",
        masoomeenIndices = listOf(2)
    ),
    ZiyaratLocation(
        id = "medina_baqi",
        nameEn = "Jannat al-Baqi (4 Holy Imams)",
        nameAr = "جنة البقيع (الأئمة الأربعة ع)",
        nameUr = "جنت البقیع (ائمہ اربعہ علیہم السلام)",
        lat = 24.4674,
        lon = 39.6158,
        cityEn = "Medina, Saudi Arabia",
        cityAr = "المدينة المنورة، السعودية",
        cityUr = "مدینہ منورہ، سعودی عرب",
        descEn = "The oldest and most sacred Islamic cemetery containing the vanished/demolished shrines of four holy Imams of Ahlul Bayt: Imam Hasan al-Mujtaba (4th Masoom, 2nd Imam), Imam Sajjad (6th Masoom, 4th Imam), Imam Baqir (7th Masoom, 5th Imam), and Imam Sadiq (8th Masoom, 6th Imam). It also holds Lady Umm ul-Banin (s.a.) and the Prophet's close family.",
        descAr = "مقبرة البقيع المقدسة وتضم المقامات للأئمة الأربعة المعصومين: الأسباط الحسن بن علي، علي السجاد، محمد الباقر، وجعفر الصادق (ع)، ومقام السيدة أم البنين (ع) والهاشميين.",
        descUr = "مدینہ منورہ کا تاریخی قبرستان جہاں چودہ معصومینؑ میں سے چار معصوم ائمہ: امام حسن مجتبیٰؑ (چوتھے معصوم)، امام زین العابدینؑ (چھٹے معصوم)، امام محمد باقرؑ (ساتویں معصوم)، اور امام جعفر صادقؑ (آٹھویں معصوم) آرام فرما ہیں۔ یہاں جناب ام البنینؑ اور دیگر اہلبیتؑ کی قبریں بھی ہیں۔",
        tipsEn = "Recommended attire: Plain, conservative attire. Best time to visit: Right after Fajr prayer or after Asr prayer when the main gates are opened for a short duration. Local etiquette: Strictly no photography or videography is allowed (including mobile phones). Do not raise your voice, and pray in silent contemplation from the boundary fence.",
        tipsAr = "الزي الموصى به: الملابس البسيطة والساترة للجميع. أفضل وقت للزيارة: بعد صلاة الفجر مباشرة أو الأسابيع العادية بعد صلاة العصر. الآداب المحلية: يمنع منعاً باتاً التصوير بالهواتف النقالة، والابتعاد عن رفع الصوت والدعاء بتوقير ووقار وهدوء خلف السياج.",
        tipsUr = "تجویز کردہ لباس: انتہائی سادہ اور باوقار شرعی لباس پہنیں۔ زیارت کا بہترین وقت: نمازِ فجر یا نمازِ عصر کے فوراً بعد جب مرکزی دروازہ مختصر وقت کے لیے کھولا جاتا ہے۔ مقامی آداب: کیمرے یا موبائل فون سے تصویر کھینچنے کی سخت ممانعت ہے۔ اونچی آواز میں کچھ نہ پڑھیں، بلکہ جنگلے کے باہر نہایت خاموشی اور احترام سے سلام پیش کریں۔",
        masoomeenIndices = listOf(4, 6, 7, 8)
    ),
    ZiyaratLocation(
        id = "karbala_hussain",
        nameEn = "Shrine of Imam Hussain (a.s.)",
        nameAr = "مرقد الإمام الحسين بن علي (ع)",
        nameUr = "روضہ مبارک حضرت امام حسین علیہ السلام",
        lat = 32.6164,
        lon = 44.0324,
        cityEn = "Karbala, Iraq",
        cityAr = "كربلاء المقدسة، العراق",
        cityUr = "کربلائے معلیٰ، عراق",
        descEn = "The sacred resting place of Imam Hussain (a.s.), the grandson of Prophet Muhammad (s.a.w.w.) and the 5th Masoom (3rd Imam). He was martyred in the tragic Event of Karbala in 61 AH alongside his loyal companions, defending truth and justice. His shrine also holds Hazrat Ali Akbar (a.s.) and Ali Asghar (a.s.).",
        descAr = "مرقد سيد الشهداء الإمام الحسين بن علي (ع) - المعصوم الخامس وثالث الأئمة، الذي استشهد مع أهل بيته وأصحابه الأوفياء في واقعة الطف الأليمة عام ٦١ هـ دفاعاً عن شريعة جده المصطفى.",
        descUr = "نواسے رسولؐ، سید الشہداء حضرت امام حسین علیہ السلام کا روضہ مبارک، جو پانچویں معصوم اور تیسرے امام ہیں۔ آپ نے ۶۱ ہجری میں حق و صداقت کی بالادستی کے لیے معصوم اہل بیتؑ اور جانثار اصحابؑ کے همراہ لازوال قربانی پیش کی۔",
        tipsEn = "Recommended attire: Conservative black or dark-colored clothing as a symbol of mourning. Best time to visit: Between midnight and Fajr for a peaceful experience. Local etiquette: Cooperate fully during several security checkpoints, avoid carrying large bags or professional cameras, and recite the prescribed Ziyarat quietly.",
        tipsAr = "الزي الموصى به: الملابس الداكنة أو السوداء الموقرة تعبيراً عن الحزن والعزاء. أفضل وقت للزيارة: ما بين منتصف الليل وصلاة الفجر لأجواء أكثر هدوءاً وروحانية. الآداب المحلية: التعاون التام مع الحرس عند نقاط التفتيش، وعدم حمل الحقائب الكبيرة، وقراءة الزيارة المأثورة بصوت منخفض.",
        tipsUr = "تجویز کردہ لباس: احتراماً سیاہ یا گہرے رنگ کا سادہ شرعی لباس زیب تن کریں۔ زیارت کا بہترین وقت: آدھی رات سے فجر کے درمیان کا وقت پرسکون زیارت کے لیے موزوں ترین ہے۔ مقامی آداب: متعدد سیکیورٹی چیک پوائنٹس پر مکمل تعاون کریں، بڑا سامان یا کیمرے اپنے ساتھ نہ لے جائیں، اور نہایت خشوع کے ساتھ زیارت پڑھ ہیں۔",
        masoomeenIndices = listOf(5)
    ),
    ZiyaratLocation(
        id = "karbala_abbas",
        nameEn = "Shrine of Hazrat Abbas (a.s.)",
        nameAr = "مرقد أبي الفضل العباس (ع)",
        nameUr = "روضہ مبارک حضرت عباس علمدار علیہ السلام",
        lat = 32.6171,
        lon = 44.0375,
        cityEn = "Karbala, Iraq",
        cityAr = "كربلاء المقدسة، العراق",
        cityUr = "کربلائے معلیٰ، عراق",
        descEn = "The shrine of Al-Abbas ibn Ali (a.s.), the loyal brother and standard-bearer of Imam Hussain (a.s.), renowned globally for his peerless loyalty, bravery, and chivalry. He is a primary figure of Ahlul Bayt.",
        descAr = "مرقد قمر بني هاشم أبي الفضل العباس بن علي (ع) - حامل لواء الإمام الحسين، المعروف بوفائه وشجاعته الفائقة وجهاده الخالد في كربلاء.",
        descUr = "علمدارِ کربلا حضرت ابو الفضل العباس علیہ السلام کا روضہ مبارک، جو وفاداری، ایثار اور شجاعت کا بے مثال استعارہ ہیں۔",
        tipsEn = "Recommended attire: Modest dark attire. Best time to visit: Mid-morning on weekdays or late hours. Local etiquette: Enter with deep respect recognizing the supreme loyalty of Al-Abbas (a.s.), wait patiently in queues, and follow the entry/exit flows.",
        tipsAr = "الزي الموصى به: ملابس ساترة داكنة اللون. أفضل وقت للزيارة: الضحى في أيام الأسبوع العادية لتقليل الانتظار. الآداب المحلية: استحضار الوفاء العظيم لأبي الفضل العباس (ع)، الالتزام بالانتظار المنظم في الممرات، واتباع اتجاهات السير المحددة.",
        tipsUr = "تجویز کردہ لباس: باوقار اور گہرے رنگ کے کپڑے۔ زیارت کا بہترین وقت: منگل اور بدھ کو دن کا درمیانی حصہ یا رات کا آخری پہر۔ مقامی آداب: سیدنا عباس علمدارؑ کی بے مثال وفاداری کا تصور ذہن میں رکھ کر انتہائی احترام سے داخل ہوں، قطار میں صبر سے کام لیں اور آمد و رفت کے راستوں کا خیال رکھیں۔"
    ),
    ZiyaratLocation(
        id = "baghdad_kadhim",
        nameEn = "Al-Kadhimiya Shrine (2 Holy Imams)",
        nameAr = "العتبة الكاظمية الشريفة (الإمام الكاظم والإمام الجواد ع)",
        nameUr = "روضہ مبارک کاظمین شریفین (حضرت امام موسیٰ کاظمؑ اور حضرت امام محمد تقیؑ)",
        lat = 33.3797,
        lon = 44.3411,
        cityEn = "Baghdad, Iraq",
        cityAr = "الكاظمية، بغداد، العراق",
        cityUr = "کاظمین، بغداد، عراق",
        descEn = "The holy sanctuary of Imam Musa al-Kadhim (9th Masoom, 7th Imam) and Imam Muhammad al-Jawad (11th Masoom, 9th Imam) of Ahlul Bayt. It is an architectural and spiritual marvel located in Baghdad.",
        descAr = "العتبة الكاظمية المقدسة، وتضم مرقدي الإمام موسى الكاظم (ع) - المعصوم التاسع وسابع الأئمة، والإمام محمد الجواد (ع) - المعصوم الحادي عشر وتاسع الأئمة.",
        descUr = "کاظمین شریفین، جہاں دو معصوم ائمہ: حضرت امام موسیٰ کاظم علیہ السلام (نویں معصوم اور ساتویں امام ہوئے) اور حضرت امام محمد تقی علیہ السلام (گیارہویں معصوم اور نویں امام) آرام فرما ہیں۔",
        tipsEn = "Recommended attire: Clean conservative dress. Best time to visit: Early mornings or after Isha prayers. Local etiquette: Maintain a highly prayerful state, keep track of designated entry gates for men and women, and cooperate with security personnel doing thorough checks.",
        tipsAr = "الزي الموصى به: الملابس الطاهرة والساترة والمحتشمة. أفضل وقت للزيارة: ساعات الصباح الأولى أو بعد صلاة العشاء. الآداب المحلية: الاستغفار المستمر واستحضار الخشوع، الانتباه والمحافظة على المداخل المنفصلة للرجال والنساء، والتعاون مع رجال الأمن للحفاظ على سلامة الجميع.",
        tipsUr = "تجویز کردہ لباس: پاک و صاف اور سادہ لباس پہنیں۔ زیارت کا بہترین وقت: صبح سویرے کے وقت یا نمازِ عشا کے بعد۔ مقامی آداب: ذکر و استغفار کرتے ہوئے داخل ہوں، مردوں اور خواتین کے لیے الگ داخلی راستوں کا دھیان رکھیں، اور سیکیورٹی اہلکاروں کی تفصیلی چیکنگ کے دوران مکمل تعاون۔",
        masoomeenIndices = listOf(9, 11)
    ),
    ZiyaratLocation(
        id = "mashhad_reza",
        nameEn = "Shrine of Imam Reza (a.s.)",
        nameAr = "مرقد الإمام علي الرضا (ع)",
        nameUr = "روضہ مبارک حضرت امام علی رضا علیہ السلام",
        lat = 36.2878,
        lon = 59.6151,
        cityEn = "Mashhad, Iran",
        cityAr = "مشهد، إيران",
        cityUr = "مشہد مقدس، ایران",
        descEn = "The grand mausoleum of Imam Ali al-Reza (a.s.), the 10th Masoom (8th Imam). It is the largest mosque in the world by area and hosts millions of pilgrims annually.",
        descAr = "المرقد الشريف للإمام الثامن علي بن موسى الرضا (ع) - المعصوم العاشر، ويعد من أكبر المساجد والمزارات في العالم حيث يحتضن الملايين من الزوار سنوياً.",
        descUr = "آٹھویں تاجدارِ امامت حضرت امام علی رضا علیہ السلام کا شاندار روضہ، جو دسویں معصوم اور آٹھویں امام ہیں۔ یہ رقبے کے لحاظ سے دنیا کی سب سے بڑی زیارت گاہ ہے۔",
        tipsEn = "Recommended attire: Chador is mandatory for females and is provided for free at the main shrine entrances. Best time to visit: Afternoon hours or late night. Local etiquette: Keep your shoes at the designated 'Kafshdari' counters, obtain a free guide book, and silence your mobile phones.",
        tipsAr = "الزي الموصى به: ارتداء 'الچادر' ضروري جداً للنساء ومتوفر مجاناً عند مداخل الحرم الشريف. أفضل وقت للزيارة: ساعات العصر أو أواخر الليل. الآداب المحلية: إيداع الأحذية في الأماكن المخصصة (الكفشداري)، الحصول على كتيبات الزيارة المجانية، وإغلاق الهواتف النقالة.",
        tipsUr = "تجویز کردہ لباس: خواتین کے لیے چادر اوڑھنا لازمی ہے جو تمام داخلی راستوں پر مفت ملتی ہے۔ زیارت کا بہترین وقت: دوپہر کے اوقات یا دیر رات کا وقت۔ مقامی آداب: اپنے جوتے مخصوص 'کفش داری' کاؤنٹرز پر جمع کروائیں، وہاں موجود رہنمائی کے مراکز سے زائرین کا گائیڈ کارڈ حاصل کریں اور موبائل فون بند یا سائلنٹ رکھیں۔",
        masoomeenIndices = listOf(10)
    ),
    ZiyaratLocation(
        id = "samarra_askari",
        nameEn = "Al-Askariyain Shrine (2 Holy Imams)",
        nameAr = "العتبة العسكرية المقدسة (الإمام الهادي والإمام العسكري ع)",
        nameUr = "روضہ مبارک عسکریین شریفین (حضرت امام علی نقیؑ اور حضرت امام حسن عسکریؑ)",
        lat = 34.1988,
        lon = 43.8732,
        cityEn = "Samarra, Iraq",
        cityAr = "سامراء، العراق",
        cityUr = "سامرا، عراق",
        descEn = "The holy resting place of Imam Ali al-Hadi (12th Masoom, 10th Imam) and Imam Hasan al-Askari (13th Masoom, 11th Imam) of Ahlul Bayt, located in Samarra, north of Baghdad.",
        descAr = "العتبة العسكرية المقدسة في سامراء، مرقد الإمامين علي الهادي (المعصوم ١٢) والحسن العسكري (المعصوم ١٣) عليهما السلام.",
        descUr = "سامرا میں عسکریین شریفین کا روضہ، جہاں دسویں اور گیارہویں امام حضرت امام علی نقیؑ (بارہویں معصوم) اور حضرت امام حسن عسکریؑ (تیرہویں معصوم) آرام فرما ہیں۔",
        tipsEn = "Recommended attire: Standard modest pilgrim clothing. Best time to visit: It is safest and highly recommended to travel in organized daytime groups/pilgrim buses leaving from Baghdad. Local etiquette: Be patient at numerous highway checkpoints, pray in the holy cellar (Sardab), and follow the guidance of tour leaders.",
        tipsAr = "الزي الموصى به: ملابس الزيارة المعتادة والساترة. أفضل وقت للزيارة: نهاراً عبر القوافل والحافلات المنسقة التي تسير من بغداد وتعود قبل الغروب. الآداب المحلية: التحلي بالصبر عند نقاط التفتيش المنتشرة على الطرق السريعة، الصلاة والزيارة في سرداب الغيبة المقدس، واتباع توجيهات مسؤولي الحملة.",
        tipsUr = "تجویز کردہ لباس: عام باوقار سفری و شرعی لباس۔ زیارت کا بہترین وقت: سیکیورٹی وجوہات کی بنا پر بغداد سے دن کے وقت روانہ ہونے والے منظم قافلوں یا سرکاری بسوں کے ذریعے سفر کریں۔ مقامی آداب: شاہراہ پر موجود متعدد چیک پوسٹوں پر تحمل کا مظاہرہ کریں، سردابِ مقدس میں نماز ادا کریں، اور اپنے گروپ لیڈر کی ہدایات پر عمل کریں۔",
        masoomeenIndices = listOf(12, 13)
    ),
    ZiyaratLocation(
        id = "samarra_mahdi",
        nameEn = "Sardab al-Ghaybah (Imam al-Mahdi a.t.f.s.)",
        nameAr = "سرداب الغيبة المقدسة (الإمام المهدي عج)",
        nameUr = "سردابِ غیبت (حضرت امام مہدی عجل اللہ تعالیٰ فرجہ الشریف)",
        lat = 34.1993,
        lon = 43.8724,
        cityEn = "Samarra, Iraq",
        cityAr = "سامراء، العراق",
        cityUr = "سامرا، عراق",
        descEn = "The sacred cellar where the 14th Masoom, the Savior of Humanity, Imam al-Mahdi (a.t.f.s.) lived and began his Minor Occultation. It is a highly respected place of prayer and direct connection with the Imam of Our Time.",
        descAr = "سرداب الغيبة في العتبة العسكرية المقدسة، حيث كان منزلاً ومصلى للإمام المهدي المنتظر (عج) - المعصوم الرابع عشر والأخير، ومركز الدعاء بتعجيل فرجه الشريف.",
        descUr = "عسکریین شریفین کے احاطے میں موجود وہ مبارک تہہ خانہ (سرداب) جہاں آخری معصوم (چودھویں معصوم)، منجیِ بشریت حضرت صاحب الزماں امامِ مہدی (عج) نے زندگی گزاری اور جہاں سے غیبتِ صغریٰ کا آغاز ہوا۔",
        tipsEn = "Recommended attire: Devotional modest attire. Best time to visit: Midday during secure group visits. Local etiquette: Recite Dua Nudba or Ziyarat Al-Yasin inside the Sardab with immense humility, present your written letters of supplication (Ariza) with pure intent, and stay calm.",
        tipsAr = "الزي الموصى به: ملابس التقديس والساترة. أفضل وقت للزيارة: نهاراً مع الوفود. الآداب المحلية: قراءة زيارة آل ياسين ودعاء الندبة بإنكسار وخشوع، والدعاء بتعجيل فرج الإمام المهدي (عج) تحت قبة السرداب الشريف.",
        tipsUr = "تجویز کردہ لباس: باوضو اور پاکیزہ لباس۔ زیارت کا بہترین وقت: دن کے پرسکون اوقات میں۔ مقامی آداب: سرداب کے اندر نہایت خشوع سے زیارتِ آلِ یاسین یا دعائے ندبہ پڑھیں، امامِ عصرؑ کے حضور اپنی حاجات اور سلام پیش کریں اور سکوت قائم رکھیں۔",
        masoomeenIndices = listOf(14)
    ),
    ZiyaratLocation(
        id = "damascus_zainab",
        nameEn = "Shrine of Sayyida Zainab (s.a.)",
        nameAr = "مرقد العقيلة السيدة زينب (ع)",
        nameUr = "روضہ مبارک حضرت سیدہ زینب سلام اللہ علیہا",
        lat = 33.4439,
        lon = 36.3400,
        cityEn = "Damascus, Syria",
        cityAr = "دمشق، سوريا",
        cityUr = "دمشق، شام",
        descEn = "The holy tomb of Sayyida Zainab bint Ali (s.a.), the heroic daughter of Imam Ali (a.s.) and Lady Fatima (s.a.), who kept the message of Karbala alive through her majestic sermons.",
        descAr = "المرقد المقدس للعقيلة السيدة زينب بنت علي (ع)، بطلة كربلاء وابنة أمير المؤمنين والزهراء (ع)، التي خلدت النهضة الحسينية.",
        descUr = "ثانیِ زہرا حضرت سیدہ زینب سلام اللہ علیہا کا روضہ مبارک، جنہوں نے کوفہ و شام کے درباروں میں اپنے بے مثل خطبات سے کربلا کے پیغام کو زندہ رکھا.",
        tipsEn = "Recommended attire: Traditional modest black abaya for women. Best time to visit: Cooler afternoon or evening hours. Local etiquette: Carry your passport or legal ID cards as they are routinely checked before entry, show extreme deference, and support the local service initiatives.",
        tipsAr = "الزي الموصى به: العباءة السوداء التقليدية الساترة كاملة للنساء. أفضل وقت للزيارة: ساعات العصر الأقل حرارة أو في المساء. الآداب المحلية: الاحتفاظ بجواز السفر أو بطاقة الهوية الرسمية لفحصها بشكل روتيني عند المداخل، التحلي بأعلى درجات الوقار والأدب، ومساعدة الفقراء المحيطين بالمنطقة.",
        tipsUr = "تجویز کردہ لباس: خواتین کے لیے روایتی اور باوقار سیاہ عبایا لازمی ہے۔ زیارت کا بہترین وقت: سہ پہر یا شام کے اوقات جب موسم معتدل ہوتا ہے۔ مقامی آداب: داخلے کے وقت شناختی چیکنگ کی جاتی ہے اس لیے اپنا پاسپورٹ اپنے ساتھ رکھیں، بے پناہ ادب کا مظاہرہ کریں، اور مقامی خدمت گاروں سے تعاون کریں۔"
    ),
    ZiyaratLocation(
        id = "damascus_ruqayya",
        nameEn = "Shrine of Sayyida Ruqayya (s.a.)",
        nameAr = "مرقد السيدة رقية بنت الحسين (ع)",
        nameUr = "روضہ مبارک حضرت سیدہ رقیہ سلام اللہ علیہا",
        lat = 33.5134,
        lon = 36.3052,
        cityEn = "Damascus, Syria",
        cityAr = "دمشق، سوريا",
        cityUr = "دمشق، شام",
        descEn = "The highly spiritual shrine of the young, beloved daughter of Imam Hussain (a.s.), Sayyida Ruqayya (also known as Fatima al-Sughra), who passed away in the dark ruins of Damascus due to grief after the battle of Karbala.",
        descAr = "مرقد اليتيمة المظلومة السيدة رقية بنت الإمام الحسين (ع)، التي فارقت الحياة في خربة الشام حزناً على أبيها الشهيد بعد واقعة كربلاء الأليمة.",
        descUr = "امام حسین علیہ السلام کی کمسن اور مظلوم بیٹی حضرت سیدہ رقیہ سلام اللہ علیہا کا مزارِ اقدس، جنہوں نے بازار اور قید خانے کی صعوبتوں کے بعد دمشق کے قید خانے میں کمالِ غربت و غم میں جان دی۔",
        tipsEn = "Recommended attire: Dark conservative clothing. Best time to visit: Morning hours before the heat rises. Local etiquette: Enter with total empathy for the orphans of Hussein, recite the specific Ziyarat with a soft low voice, and cooperate at the security doors.",
        tipsAr = "الزي الموصى به: العباءة الشرعية للنساء والملابس المحتشمة للرجال. أفضل وقت للزيارة: الساعات الصباحية الهادئة. الآداب المحلية: استحضار مأساة يتامى أهل البيت (ع) والبكاء بوقار وصمت، والتعاون التام مع خدمة المقام الشريف عند الأبواب.",
        tipsUr = "تجویز کردہ لباس: نہایت باوقار اور گہرے رنگ کے کپڑے۔ زیارت کا بہترین وقت: سورج نکلنے کے بعد صبح کے پرسکون اوقات۔ مقامی آداب: مظلوم کربلا کے کمسن بچوں اور یتیموں کی صعوبتوں کو یاد رکھ کر گریہ و زاری کریں، شور شرابے سے دور رہیں، اور سیکیورٹی اہلکاروں کی ہدایات کی پابندی کریں۔"
    ),
    ZiyaratLocation(
        id = "qom_masuma",
        nameEn = "Shrine of Sayyida Fatima Masuma (s.a.)",
        nameAr = "مرقد السيدة فاطمة المعصومة (ع)",
        nameUr = "روضہ مبارک حضرت سیدہ فاطمہ معصومہ سلام اللہ علیہا",
        lat = 34.6416,
        lon = 50.8795,
        cityEn = "Qom, Iran",
        cityAr = "قم، إيران",
        cityUr = "قم مقدس، ایران",
        descEn = "The holy sanctuary of Lady Fatima Masuma (s.a.), the noble sister of Imam al-Reza (a.s.). Qom has grown into a premier global center for Islamic theology around her shrine.",
        descAr = "المرقد المقدس لكريمة أهل البيت السيدة فاطمة المعصومة (ع)، أخت الإمام الرضا (ع)، وهي منارة كبرى للحوزة العلمية في قم المقدسة.",
        descUr = "قم میں کریمہ اہلبیت حضرت فاطمہ معصومہ سلام اللہ علیہا کا روضہ مبارک، جن کے وجود مسعود کی برکت سے قم علم و فقہ کا عظیم عالمی مرکز بنا۔",
        tipsEn = "Recommended attire: A Chador is mandatory for women visitors and can be obtained at the main entrances. Best time to visit: Weekdays between 8 AM and 11 AM when it is highly peaceful. Local etiquette: Show respect towards the seminary students, avoid loud discussions, and utilize informational guides.",
        tipsAr = "الزي الموصى به: ارتداء 'الچادر' كامل للمرأة وهو إلزامي لدخول المزار (متوفر عند الأبواب). أفضل وقت للزيارة: الصباح بين الساعة 8 و 11 لضمان الخلو والهدوء. الآداب المحلية: توقير طلاب الحوزة والعلماء، تجنب الأحاديث الجانبية والصاخبة، والاستفادة من النشرات الإرشادية.",
        tipsUr = "تجویز کردہ لباس: خواتین زائرین کے لیے چادر اوڑھنا لازمی ہے (داخلی دروازوں پر فراہم کی جاتی ہے)۔ زیارت کا بہترین وقت: ہفتے کے عام دنوں میں صبح 8 سے 11 بجے کا وقت، جب وہاں خاموشی اور سکون ہوتا ہے۔ مقامی آداب: حوزہ علمیہ کے طلبہ اور اساتذہ کا احترام کریں، بلند آواز سے گفتگو سے پرہیز کریں، اور مفت معلوماتی لٹریچر سے استفادہ کریں۔"
    ),
    ZiyaratLocation(
        id = "mecca_mualla",
        nameEn = "Jannat al-Mu'alla (Lady Khadija s.a.)",
        nameAr = "مقبرة جنة المعلاة (السيدة خديجة الكبرى ع)",
        nameUr = "قبرستان جنت المعلیٰ (حضرت سیدہ خدیجہ کبریٰ سلام اللہ علیہا)",
        lat = 21.4312,
        lon = 39.8294,
        cityEn = "Mecca, Saudi Arabia",
        cityAr = "مكة المكرمة، السعودية",
        cityUr = "مکہ مکرمہ، سعودی عرب",
        descEn = "The sacred graveyard of Mecca containing the holy graves of Lady Khadija (s.a.), the first wife of the Prophet and mother of Lady Fatima (s.a.), alongside Hazrat Abu Talib (a.s.) and the grandfather of the Prophet, Abdul Muttalib (a.s.).",
        descAr = "مقبرة الحجون الشريفة في مكة المكرمة وتضم الضريح المهدم لأم المؤمنين السيدة خديجة الكبرى (ع) - المعصومة الروحية وسند الإسلام - وجد النبي عبد المطلب وأبي طالب (ع).",
        descUr = "مکہ مکرمہ کا تاریخی اور مقدس ترین قبرستان جہاں ام المومنین حضرت خدیجہ کبریٰ سلام اللہ علیہا (سرتاجِ اولِ رسولؐ اور مادرِ سیدہ زہراؑ)، محسنِ اسلام حضرت ابوطالب علیہ السلام، اور دادائے رسولؐ حضرت عبدالمطلب علیہ السلام آسودہ خاک ہیں۔",
        tipsEn = "Recommended attire: Plain conservative Hajj / Umrah clothing. Best time to visit: Mid-morning or after Sunset on regular days. Local etiquette: Approach with absolute respect, keep phones secure, do not shout or perform loud ritual practices, and pray from the designated sidewalks quietly.",
        tipsAr = "الزي الموصى به: ملابس الإحرام البسيطة أو الثوب والعباءة الساترة. أفضل وقت للزيارة: بعد صلاة العصر أو أثناء النهار. الآداب المحلية: السلام على أم المؤمنين خديجة الكبرى (ع) بخشوع وحزن، عدم رفع الأصوات لضمان احترام القوانين المحلية.",
        tipsUr = "تجویز کردہ لباس: سادہ شرعی لباس یا سفید شلوار قمیض۔ زیارت کا بہترین وقت: دوپہر کے علاوہ صبح و شام کے اوقات۔ مقامی آداب: محسنۂ اسلام جنابِ خدیجۃ الکبریٰؑ کے درجات پر سلام پیش کریں، بلند آواز سے گریہ و زاری سے بچیں، اور خاموشی سے سورہ فاتحہ اور صلوٰۃ پیش کریں۔"
    ),
    ZiyaratLocation(
        id = "medina_uhud",
        nameEn = "Uhud Cemetery (Hazrat Hamza a.s.)",
        nameAr = "مقبرة شهداء أحد (جبل أحد وسيد الشهداء حمزة ع)",
        nameUr = "قبرستان شہدائے احد (سید الشہداء حضرت حمزہ علیہ السلام)",
        lat = 24.5024,
        lon = 39.6146,
        cityEn = "Medina, Saudi Arabia",
        cityAr = "المدينة المنورة، السعودية",
        cityUr = "مدینہ منورہ، سعودی عرب",
        descEn = "The battleground of Uhud and cemetery containing the holy resting place of the uncle of the Prophet, Hazrat Hamza ibn Abdul Muttalib (a.s.), 'Sayyid al-Shuhada' of his time, who was martyred protecting Islam.",
        descAr = "مقبرة شهداء معركة أحد المقدسة في المدينة المنورة، وتضم مرقد عم النبي الأكرم وسيد الشهداء حمزة بن عبد المطلب (ع) والصحابة المخلصين.",
        descUr = "مدینہ منورہ میں غزوۂ احد کا تاریخی میدان اور وہ مقدس قبرستان جہاں حضورؐ کے باوفا چچا، شیرِ خدا و شیرِ رسولؐ حضرت حمزہ بن عبدالمطلب علیہ السلام اور دیگر ستر شہدائے احد آرام فرما ہیں۔",
        tipsEn = "Recommended attire: Comfortable walking modest clothing. Best time to visit: Early morning when temperatures are cool. Local etiquette: Recite the unique Ziyarat of the Uhud martyrs, stand quietly opposite the graves, and climb the Archers' hill with care.",
        tipsAr = "الزي الموصى به: ملابس مريحة ومحتشمة للمشي الطويل. أفضل وقت للزيارة: ساعات الصباح الباكر لبرودة الجو. الآداب المحلية: الزيارة والدعاء لعم الرسول حمزة (ع) والشهداء الأبرار، والمحافظة على النظافة العامة.",
        tipsUr = "تجویز کردہ لباس: باوقار اور آرام دہ سفری لباس۔ زیارت کا بہترین وقت: صبح سویرے جب دھوپ تیز نہیں ہوتی۔ مقامی آداب: جنابِ حمزہؑ اور شہدائے احد کے لیے صلوٰۃ و سلام کا ہدیہ پیش کریں، نہایت احترام سے الوداعی سلام پڑھیں، اور پہاڑی پر چڑھتے وقت احتیاط بڑھائیں۔"
    ),
    ZiyaratLocation(
        id = "balad_muhammad",
        nameEn = "Shrine of Sayyid Muhammad (a.s.)",
        nameAr = "مرقد السيد محمد بن الإمام الهادي (ع)",
        nameUr = "روضہ مبارک حضرت سید محمد بن امام علی نقی علیہ السلام",
        lat = 34.0135,
        lon = 44.1481,
        cityEn = "Balad, Iraq",
        cityAr = "بلد، العراق",
        cityUr = "بلد، عراق",
        descEn = "The grand shrine of Sayyid Muhammad (a.s.), the eldest son of Imam Ali al-Hadi (a.s.) and brother of Imam Hasan al-Askari (a.s.). He is universally revered for his intense purity, deep knowledge, and powerful spiritual presence.",
        descAr = "مرقد سبع الدجيل، السيد محمد بن الإمام علي الهادي (ع)، وهو شقيق الإمام الحسن العسكري، المعروف بعلو مقامه، وكراماته العظيمة المشهورة وجلالة روحه الطاهرة.",
        descUr = "حضرت سید محمد علیہ السلام (سبع الدجیل) کا پروقار مزارِ اقدس، جو دسویں امام حضرت علی نقیؑ کے سب سے بڑے فرزند اور گیارہویں امام حضرت حسن عسکریؑ کے سگے بھائی ہیں۔ آپ اپنی جلالتِ علمی، بے پناہ طہارت اور کراماتِ الٰہی کے لیے زائرین میں بے حد مشہور ہیں۔",
        tipsEn = "Recommended attire: Plain traditional pilgrim dress. Best time to visit: On the way heading towards or coming back from Samarra. Local etiquette: Be fully prepared for extensive security screens, wait patiently at the gates, and read the Ziyarat of the noble son of Imamate.",
        tipsAr = "الزي الموصى به: الملابس الشرعية والتقليدية المحتشمة. أفضل وقت للزيارة: أثناء التوجه إلى سامراء أو العودة منها. الآداب المحلية: التعاون عند البوابة الأمنية، وطلب كرامات السيد بن الإمام الهادي، وقراءة زيارته بنية صادقة وخالص لله.",
        tipsUr = "تجویز کردہ لباس: پاک و صاف روایتی یا سلیس شلوار قمیض۔ زیارت کا بہترین وقت: سامرا جاتے ہوئے یا وہاں سے بغداد واپس لوٹتے ہوئے راستے کا قیام۔ مقامی آداب: بیرونی گیٹ پر تفصیلی تلاشی کے دوران سیکیورٹی پر مامور خدام سے تعاون کریں، نہایت مودب ہو کر دربار پہ سلام پیش کریں اور دلی خلوص سے دعا مانگیں۔"
    ),
    ZiyaratLocation(
        id = "hillah_sharifa",
        nameEn = "Shrine of Lady Sharifa (s.a.)",
        nameAr = "مرقد العلوية الشريفة بنت الإمام الحسن (ع)",
        nameUr = "روضہ مبارک جناب سیدہ شریفہ بنت امام حسن مجتبیٰ علیہا السلام",
        lat = 32.4411,
        lon = 44.4152,
        cityEn = "Hillah, Iraq",
        cityAr = "الحلة، العراق",
        cityUr = "ہلہ، عراق",
        descEn = "The holy sanctuary of Lady Sharifa (s.a.), the noble daughter of Imam Hasan al-Mujtaba (a.s.). Celebrated globally for her miraculous healing intercessions (Bab al-Hawaij) and comforting presence.",
        descAr = "مرقد طبيبة معاصي أهل البيت العلوية الشريفة بنت الإمام الحسن السبط (ع)- في منطقة الحلة، مقصد المرضى وطالبي الحوائج ببركتها الطاهرة.",
        descUr = "حضرت امام حسن مجتبیٰ علیہ السلام کی صاحبزادی حضرت سیدہ شریفہ سلام اللہ علیہا کا پربرکت روضہ مبارک، جو عراق کے تاریخی شہر ہلہ میں واقع ہے۔ آپ بیماروں کی شفا یابی اور باب الحوائج ہونے کی وجہ سے دنیا بھر کے زائرین کی امیدوں کا مرکز ہیں۔",
        tipsEn = "Recommended attire: Conservative traditional attire (chador/abaya to show respect). Best time to visit: Afternoon on weekdays. Local etiquette: Present your prayers for health with deep faith, respect the massive influx of patients, and assist in distributing charity items if possible.",
        tipsAr = "الزي الموصى به: العباءة الچادر الساتر للنساء. أفضل وقت للزيارة: في منتصف الأسبوع نهاراً. الآداب المحلية: التوسل إلى الله بشفائها المبارك للمرضى، إظهار التعاطف مع كثرة الزوار المرضى، والمساهمة في الموائد الخيرية.",
        tipsUr = "تجویز کردہ لباس: خواتین کے لیے عبایا اور پردے کا مکمل انتظام۔ زیارت کا بہترین وقت: ہفتے کے درمیانی ایام میں دن کا پچھلا حصہ۔ مقامی آداب: شفا یابی اور حاجات کی برآوری کے لیے پختہ یقین کے ساتھ الٰہی بارگاہ میں گڑگڑائیں، وہاں موجود علیل زائرین کا احترام و خیال رکھیں اور لنگر و خیرات میں حصہ لیں۔"
    ),
    ZiyaratLocation(
        id = "kufa_muslim",
        nameEn = "Shrine of Muslim ibn Aqeel (a.s.)",
        nameAr = "مرقد مسلم بن عقيل (ع)",
        nameUr = "روضہ مبارک حضرت مسلم بن عقیل علیہ السلام",
        lat = 32.0294,
        lon = 44.4011,
        cityEn = "Kufa, Iraq",
        cityAr = "الكوفة، العراق",
        cityUr = "کوفہ، عراق",
        descEn = "The sacred shrine inside Majid al-Kufa holding the holy graves of Hazrat Muslim ibn Aqeel (a.s.), the cousin and ambassador of Imam Hussain (a.s.), together with his loyal host Hani ibn Urwah (a.s.).",
        descAr = "المرقد المقدس لمسلم بن عقيل (ع) سفير أبي عبد الله الحسين في الكوفة، ومعه هانئ بن عروة والمختار الثقفي بطل الإسلام.",
        descUr = "کوفہ میں حضرت مسلم بن عقیل علیہ السلام اور حضرت ہانی بن عروہ کا مزار اقدس جو کہ مسجد کوفہ کے احاطے میں ہے اور بہت ہی عظیم الشان زیارت ہے۔",
        tipsEn = "Recommended attire: Modest traditional clothing. Best time to visit: Afternoon, combined with performing Masjid al-Kufa holy A'maal prayers. Local etiquette: Chant greetings with extreme reverence, do not block the paths, and recite the specific Ziyarat of Hazrat Muslim.",
        tipsAr = "الزي الموصى به: العباءة للنساء والثوب للرجال. أفضل وقت للزيارة: فترة الضحى أو العصر مع أداء أعمال مسجد الكوفة التاريخية. الآداب المحلية: قراءة زيارة مسلم بن عقيل بخشوع ودموع طاهرة.",
        tipsUr = "تجویز کردہ لباس: باوقار اور سادہ کپڑے۔ زیارت کا بہترین وقت: دن کا پچھلا حصہ جب مسجد کوفہ کے اعمال کرنے میں آسانی ہو۔ مقامی آداب: دربار میں داخل ہو کر گریہ و زاری کریں، وہاں کی مخصوص زیارت پڑھیں اور زائرین کے حقوق کا خیال رکھیں۔"
    ),
    ZiyaratLocation(
        id = "musayyib_tiflan",
        nameEn = "Shrine of Tiflan-e-Muslim (a.s.)",
        nameAr = "مرقد طفلي مسلم بن عقيل (ع)",
        nameUr = "مزار طفلان مسلم علیہ السلام",
        lat = 32.7983,
        lon = 44.2900,
        cityEn = "Musayyib, Iraq",
        cityAr = "المسيب، العراق",
        cityUr = "مسیب، عراق",
        descEn = "The holy resting place of Ibrahim and Muhammad, the two young, pure sons of Hazrat Muslim ibn Aqeel (a.s.), brutally martyred while in captivity of the oppressors near the Euphrates river.",
        descAr = "المزار الشريف لولدي مسلم بن عقيل إبراهيم ومحمد عليهما السلام، اللذين قتلا مظلومين في ريعان طفولتهما بعد واقعة كربلاء.",
        descUr = "حضرت مسلم بن عقیلؑ کے دو چاند جیسے معصوم فرزندوں، محمد اور ابراہیم کا روضہ، جنہیں کربلا کے بعد قید خانے سے نکل کر فرات کے کنارے انتہائی بیدردی سے شہید کیا گیا تھا۔",
        tipsEn = "Recommended attire: Plain pilgrim attire. Best time to visit: Early afternoons. Local etiquette: Direct your thoughts towards children's hardships, pray for the orphan children of the world, and maintain a quiet mourning posture.",
        tipsAr = "الزي الموصى به: ملابس طاهرة وساترة. أفضل وقت للزيارة: بعد الظهر نهاراً. الآداب المحلية: تذكر ظمأ وعطش الأطفال الأبرياء في كربلاء، وقراءة التعازي بهدوء تام.",
        tipsUr = "تجویز کردہ لباس: دھلی ہوئی پاک و صاف شلوار قمیض۔ زیارت کا بہترین وقت: دوپہر کے وقت۔ مقامی آداب: مصیبت زدہ بچوں اور یتیموں کی پیاس اور بھوک کو یاد کر کے سلام پیش کریں اور دربار پر ادب و احترام کا مکمل التزام کریں۔"
    )
)

val leafletHtml = """
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
            margin: 0;
            padding: 0;
            background-color: #0F172A;
        }
        .leaflet-popup-content-wrapper {
            background-color: #1E293B !important;
            color: #FFFFFF !important;
            border-radius: 12px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.5);
            font-family: system-ui, -apple-system, sans-serif;
            border: 1px solid rgba(255,255,255,0.1);
        }
        .leaflet-popup-tip {
            background-color: #1E293B !important;
        }
        .popup-title {
            font-weight: bold;
            font-size: 15px;
            color: #38BDF8;
            margin-bottom: 4px;
        }
        .popup-city {
            font-size: 11px;
            font-weight: 600;
            color: #34D399;
            text-transform: uppercase;
            margin-bottom: 8px;
        }
        .popup-desc {
            font-size: 12px;
            line-height: 1.5;
            color: #E2E8F0;
        }
    </style>
</head>
<body>
    <div id="map"></div>
    <script>
        var map = L.map('map', {
            zoomControl: false
        }).setView([32.5, 45.0], 4);
        
        L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> &copy; <a href="https://carto.com/attributions">CARTO</a>',
            subdomains: 'abcd',
            maxZoom: 20
        }).addTo(map);

        L.control.zoom({
            position: 'topright'
        }).addTo(map);

        var markers = {};

        function addMarker(id, lat, lon, title, city, desc) {
            var popupContent = '<div class="popup-title">' + title + '</div>' +
                               '<div class="popup-city">📍 ' + city + '</div>' +
                               '<div class="popup-desc">' + desc + '</div>';
            
            var marker = L.marker([lat, lon]).addTo(map)
                .bindPopup(popupContent);
            
            markers[id] = marker;
        }

        function focusLocation(id, lat, lon) {
            map.flyTo([lat, lon], 12, {
                animate: true,
                duration: 1.5
            });
            setTimeout(function() {
                if (markers[id]) {
                    markers[id].openPopup();
                }
            }, 1600);
        }

        // Dynamically injected markers:
        %MARKERS%
    </script>
</body>
</html>
""".trimIndent()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZiyaratsScreen(
    viewModel: PrayerViewModel,
    onBack: () -> Unit,
    languageCode: String = "en",
    modifier: Modifier = Modifier,
    initialSelectedZiyaratId: String? = null
) {
    val allZiyarats by viewModel.allZiyarats.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") } // "All", "Prophet", "Imams", "Lady Fatima", "Favorites"
    var activeReadingZiyarat by remember { mutableStateOf<ZiyaratItem?>(null) }
    var readingTabState by remember { mutableIntStateOf(0) } // 0 = Bilingual, 1 = Arabic Only, 2 = Translation Only

    // Auto-select starting Ziyarat if passed as initial argument
    LaunchedEffect(initialSelectedZiyaratId, allZiyarats) {
        if (initialSelectedZiyaratId != null && allZiyarats.isNotEmpty()) {
            val matched = allZiyarats.find { it.id == initialSelectedZiyaratId }
            if (matched != null) {
                activeReadingZiyarat = matched
            }
        }
    }

    // Filtering system
    val filteredZiyarats = remember(allZiyarats, searchQuery, selectedCategory) {
        allZiyarats.filter { item ->
            // Search criteria
            val matchesSearch = item.titleEn.contains(searchQuery, ignoreCase = true) ||
                    item.titleAr.contains(searchQuery) ||
                    item.titleUr.contains(searchQuery, ignoreCase = true) ||
                    item.arText.contains(searchQuery) ||
                    item.enText.contains(searchQuery, ignoreCase = true) ||
                    item.urText.contains(searchQuery, ignoreCase = true)

            // Category classification
            val matchesCategory = when (selectedCategory) {
                "Prophet" -> item.masoomIndex == 1
                "Lady Fatima" -> item.masoomIndex == 3
                "Imams" -> item.masoomIndex in listOf(2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)
                "Favorites" -> item.isFavorite
                else -> true
            }

            matchesSearch && matchesCategory
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (languageCode == "ur") "مقدس زیارات" else if (languageCode == "ar") "الزيارات الشريفة" else "Sacred Ziyarats",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (languageCode == "ur") "۱۴ معصومین کے لئے منسوب دعائیں اور تلاوتیں" else if (languageCode == "ar") "أدعية التوسل والزيارات للأئمة المعصومين" else "Searchable readings for the 14 Infallible Masoomeen",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("ziyarat_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.testTag("ziyarats_root")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Input with interactive leading icon
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = if (languageCode == "ur") "زیارت تلاش کریں..." else if (languageCode == "ar") "ابحث عن الزيارة..." else "Search Ziyarats & Arabic text...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                singleLine = true
            )

            // Category filter chips
            val categories = listOf("All", "Prophet", "Lady Fatima", "Imams", "Favorites")
            val categoriesUr = mapOf("All" to "سب", "Prophet" to "رسول اکرمؐ", "Lady Fatima" to "سیدہ زہراؑ", "Imams" to "ائمہ معصومینؑ", "Favorites" to "پسندیدہ")
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val selected = cat == selectedCategory
                    val label = if (languageCode == "ur") categoriesUr[cat] ?: cat else cat
                    FilterChip(
                        selected = selected,
                        onClick = { selectedCategory = cat },
                        label = { Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = filteredZiyarats.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Search, contentDescription = "Empty", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                        Text(
                            text = if (languageCode == "ur") "کوئی نتائج دستیاب نہیں ہیں" else "No matching Ziyarats found.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Scrollable list of core Ziyarat items
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredZiyarats, key = { it.id }) { ziyarat ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeReadingZiyarat = ziyarat }
                            .testTag("ziyarat_card_${ziyarat.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Header: Masoom Index badge, Favorite, and Title
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = ziyarat.masoomIndex.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    
                                    val masoomName = remember(ziyarat.masoomIndex) {
                                        MasoomeenData.list.find { it.index == ziyarat.masoomIndex }?.name ?: "Holy Ahlul Bayt (as)"
                                    }
                                    Text(
                                        text = masoomName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }

                                Row {
                                    IconButton(onClick = { viewModel.toggleZiyaratFavorite(ziyarat) }) {
                                        Icon(
                                            imageVector = if (ziyarat.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Favorite",
                                            tint = if (ziyarat.isFavorite) Color(0xFFE94057) else MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }

                            // Dynamic Ziyarat Title displaying correct translations
                            Text(
                                text = if (languageCode == "ur") ziyarat.titleUr else if (languageCode == "ar") ziyarat.titleAr else ziyarat.titleEn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Arabic Preview snippet
                            Text(
                                text = ziyarat.arText.take(120) + "...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Connected Lunar Calendar Date Label
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = "Calendar Link", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = if (languageCode == "ur") "منسلک قمری تاریخیں: ${ziyarat.dateStringHijri}" else "Linked Hijri Events: ${ziyarat.dateStringHijri}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            // Quick action row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { activeReadingZiyarat = ziyarat },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Book, contentDescription = "Read", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = if (languageCode == "ur") "زیارت پڑھیں" else "Read Arabic & Trans.", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog containing translation split screen
    activeReadingZiyarat?.let { ziyarat ->
        ZiyaratDetailDialog(
            ziyarat = ziyarat,
            languageCode = languageCode,
            viewModel = viewModel,
            onDismiss = { activeReadingZiyarat = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZiyaratDetailDialog(
    ziyarat: ZiyaratItem,
    languageCode: String,
    viewModel: PrayerViewModel,
    onDismiss: () -> Unit
) {
    var readingTabState by remember { mutableIntStateOf(0) } // 0 = Bilingual, 1 = Arabic Only, 2 = Translation Only
    val ziyaratBookmarks by viewModel.ziyaratBookmarks.collectAsState()
    
    val savedOffset = ziyaratBookmarks[ziyarat.id] ?: 0
    val listState = androidx.compose.foundation.lazy.rememberLazyListState(
        initialFirstVisibleItemIndex = 0,
        initialFirstVisibleItemScrollOffset = savedOffset
    )

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemScrollOffset }
            .collect { offset ->
                if (offset > 0 || (savedOffset != 0 && offset == 0)) {
                    viewModel.setZiyaratBookmark(ziyarat.id, offset)
                }
            }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = if (languageCode == "ur") ziyarat.titleUr else ziyarat.titleEn,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val masoomName = remember(ziyarat.masoomIndex) {
                                    MasoomeenData.list.find { it.index == ziyarat.masoomIndex }?.name ?: ""
                                }
                                Text(
                                    text = masoomName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss, modifier = Modifier.testTag("ziyarat_detail_back")) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close View")
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.toggleZiyaratFavorite(ziyarat) }) {
                                Icon(
                                    imageVector = if (ziyarat.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (ziyarat.isFavorite) Color(0xFFE94057) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                }
            ) { overlayPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(overlayPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Translation layout selection tabs
                    TabRow(
                        selectedTabIndex = readingTabState,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Tab(
                            selected = readingTabState == 0,
                            onClick = { readingTabState = 0 },
                            text = { Text("Bilingual", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = readingTabState == 1,
                            onClick = { readingTabState = 1 },
                            text = { Text("Arabic Only", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = readingTabState == 2,
                            onClick = { readingTabState = 2 },
                            text = { Text("Translations", fontWeight = FontWeight.Bold) }
                        )
                    }

                    // Core scrollable text layout
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Immersive Text Card according to Tab States
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    if (readingTabState == 0 || readingTabState == 1) {
                                        Text(
                                            text = ziyarat.arText,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            lineHeight = 42.sp,
                                            textAlign = TextAlign.Right,
                                            modifier = Modifier.fillMaxWidth().testTag("ziyarat_arabic_text")
                                        )
                                    }

                                    if (readingTabState == 0) {
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                    }

                                    if (readingTabState == 0 || readingTabState == 2) {
                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            // English section
                                            Column {
                                                Text(
                                                    text = "ENGLISH TRANSLATION",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = ziyarat.enText,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    lineHeight = 22.sp
                                                )
                                            }

                                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))

                                            // Urdu section
                                            Column {
                                                Text(
                                                    text = "اردو ترجمہ",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.tertiary,
                                                    textAlign = TextAlign.Right,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = ziyarat.urText,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Normal,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    lineHeight = 28.sp,
                                                    textAlign = TextAlign.Right,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Dynamic Timeline Card representing Connected Islamic lunar calendar Events
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "🕌 HISTORICAL CONTEXT & HOLY DATES",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    val matchesEvents = remember(ziyarat.masoomIndex) {
                                        MasoomeenData.list.find { it.index == ziyarat.masoomIndex }?.events ?: emptyList()
                                    }

                                    if (matchesEvents.isEmpty()) {
                                        Text(
                                            text = "Recited during key Hijri months particularly on birth and martyrdom anniversaries of the Holy Prophet.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        matchesEvents.forEach { ev ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                                    .padding(8.dp),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (ev.eventType == com.example.data.EventType.WILADAT) Icons.Default.Star else Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = if (ev.eventType == com.example.data.EventType.WILADAT) Color(0xFF00BFA5) else Color(0xFFE94057),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = ev.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                    Text(text = "Anniversary Lunar Recurrence: ${ev.dateStringHijri}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
