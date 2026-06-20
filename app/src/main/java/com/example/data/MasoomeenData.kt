package com.example.data

data class MasoomEvent(
    val title: String,
    val dateStringHijri: String,
    val eventType: EventType,
    val description: String
)

enum class EventType {
    WILADAT,
    SHAHADAT
}

data class MasoomDetails(
    val index: Int,
    val name: String,
    val title: String,
    val birthDate: String,
    val martyrdomDate: String,
    val burialPlace: String,
    val bioSummary: String,
    val events: List<MasoomEvent>
)

object MasoomeenData {
    val list = listOf(
        MasoomDetails(
            index = 1,
            name = "Prophet Muhammad (saws)",
            title = "Al-Mustafa (The Chosen)",
            birthDate = "17 Rabi' al-Awwal",
            martyrdomDate = "28 Safar",
            burialPlace = "Al-Masjid an-Nabawi, Medina",
            bioSummary = "The final Prophet and Messenger of Allah, who established Islam, received the Holy Qur'an, and set the path of guidance for humanity.",
            events = listOf(
                MasoomEvent("Prophet Muhammad's Birth", "17 Rabi' al-Awwal", EventType.WILADAT, "The birth of the Noble Messenger of mercy to all of creation."),
                MasoomEvent("Prophet Muhammad's Martyrdom", "28 Safar", EventType.SHAHADAT, "The departure of the Prophet, marking the end of the prophetic era.")
            )
        ),
        MasoomDetails(
            index = 2,
            name = "Imam Ali ibn Abi Talib (as)",
            title = "Amir al-Mu'minin (Commander of the Faithful)",
            birthDate = "13 Rajab",
            martyrdomDate = "21 Ramadan",
            burialPlace = "Imam Ali Shrine, Najaf, Iraq",
            bioSummary = "The first infallible Imam, born inside the Holy Kaabah, the gate of prophetic knowledge, irreplaceable champion of justice, and standard-bearer of Islam.",
            events = listOf(
                MasoomEvent("Imam Ali's Birth", "13 Rajab", EventType.WILADAT, "Born inside the Holy Kaaba, Najaf celebrates the birth of the Champion of Islam."),
                MasoomEvent("Imam Ali's Martyrdom", "21 Ramadan", EventType.SHAHADAT, "Struck with a poisoned sword while in Sajdah of Fajr prayer by Ibn Muljam at Kufa.")
            )
        ),
        MasoomDetails(
            index = 3,
            name = "Lady Fatima al-Zahra (sa)",
            title = "Sayyidat Nisa' al-Alamin (Leader of the Women of the Worlds)",
            birthDate = "20 Jumada al-Thani",
            martyrdomDate = "3 Jumada al-Thani",
            burialPlace = "Jannat al-Baqi / Hidden, Medina",
            bioSummary = "The beloved noble daughter of the Prophet, wife of Imam Ali, mother of Imam Hasan and Imam Hussain, representing the perfect purity of Islam.",
            events = listOf(
                MasoomEvent("Lady Fatima's Birth", "20 Jumada al-Thani", EventType.WILADAT, "The birth of the pure, immaculate lady Zahra, bringing immense joy to the Prophet."),
                MasoomEvent("Lady Fatima's Martyrdom", "3 Jumada al-Thani", EventType.SHAHADAT, "Died of injuries shortly after the departure of her father (saws).")
            )
        ),
        MasoomDetails(
            index = 4,
            name = "Imam Hasan al-Mujtaba (as)",
            title = "Al-Mujtaba (The Chosen)",
            birthDate = "15 Ramadan",
            martyrdomDate = "28 Safar",
            burialPlace = "Jannat al-Baqi, Medina",
            bioSummary = "The second infallible Imam, renowned for his infinite generosity, wisdom, patience, and the historic peace treaty which saved the Muslim community.",
            events = listOf(
                MasoomEvent("Imam Hasan's Birth", "15 Ramadan", EventType.WILADAT, "The first grandchild of the Holy Prophet was born in the blessed month of Ramadan."),
                MasoomEvent("Imam Hasan's Martyrdom", "28 Safar", EventType.SHAHADAT, "Poisoned on the order of Mu'awiyah, buried in Jannat al-Baqi.")
            )
        ),
        MasoomDetails(
            index = 5,
            name = "Imam Hussain ibn Ali (as)",
            title = "Sayyid al-Shuhada (Lord of the Martyrs)",
            birthDate = "3 Sha'ban",
            martyrdomDate = "10 Muharram (Ashura)",
            burialPlace = "Imam Hussain Shrine, Karbala, Iraq",
            bioSummary = "The third infallible Imam, who made the supreme sacrifice in Karbala alongside his family and loyal companions to save truth and justice for eternity.",
            events = listOf(
                MasoomEvent("Imam Hussain's Birth", "3 Sha'ban", EventType.WILADAT, "The birth of the Savior of Faith. The Prophet rejoiced and predicted his epic sacrifice."),
                MasoomEvent("Imam Hussain's Martyrdom", "10 Muharram", EventType.SHAHADAT, "Ashura. Martyrdom in Karbala defending justice, honor, and purity of faith.")
            )
        ),
        MasoomDetails(
            index = 6,
            name = "Imam Ali ibn al-Hussain (as)",
            title = "Zayn al-Abidin / Al-Sajjad (Prostrating Decorator of Worshippers)",
            birthDate = "5 Sha'ban",
            martyrdomDate = "25 Muharram",
            burialPlace = "Jannat al-Baqi, Medina",
            bioSummary = "The fourth infallible Imam, standard-bearer of Karbala's message through sermons, and author of Al-Sahifa al-Sajjadiyya (the Psalms of Islam).",
            events = listOf(
                MasoomEvent("Imam Sajjad's Birth", "5 Sha'ban", EventType.WILADAT, "Born in Medina, bringing spiritual beauty to the household of Imam Hussain."),
                MasoomEvent("Imam Sajjad's Martyrdom", "25 Muharram", EventType.SHAHADAT, "Poisoned by Hisham ibn Abdal Malik after years of moving spiritual teachings.")
            )
        ),
        MasoomDetails(
            index = 7,
            name = "Imam Muhammad ibn Ali (as)",
            title = "Al-Baqir (Splitter / Revealer of Knowledge)",
            birthDate = "1 Rajab",
            martyrdomDate = "7 Dhu al-Hijjah",
            burialPlace = "Jannat al-Baqi, Medina",
            bioSummary = "The fifth infallible Imam, who opened the floodgates of Ahlul Bayt scientific, jurisprudential, and philosophical teachings in Islamic academia.",
            events = listOf(
                MasoomEvent("Imam Baqir's Birth", "1 Rajab", EventType.WILADAT, "A joyful birth establishing the intellectual path of Ahlul Bayt."),
                MasoomEvent("Imam Baqir's Martyrdom", "7 Dhu al-Hijjah", EventType.SHAHADAT, "Poisoned by Hisham's administration, joining his ancestors in Baqi.")
            )
        ),
        MasoomDetails(
            index = 8,
            name = "Imam Ja'far ibn Muhammad (as)",
            title = "Al-Sadiq (The Truthful)",
            birthDate = "17 Rabi' al-Awwal",
            martyrdomDate = "25 Shawwal",
            burialPlace = "Jannat al-Baqi, Medina",
            bioSummary = "The sixth infallible Imam, founder of the Ja'fari school of jurisprudence, who trained over 4,000 scholars including Jabir ibn Hayyan and Abu Hanifa.",
            events = listOf(
                MasoomEvent("Imam Sadiq's Birth", "17 Rabi' al-Awwal", EventType.WILADAT, "Born on the exact birthday anniversary of the Holy Prophet (saws)."),
                MasoomEvent("Imam Sadiq's Martyrdom", "25 Shawwal", EventType.SHAHADAT, "Martyred through poison by Al-Mansur al-Dawanaqi.")
            )
        ),
        MasoomDetails(
            index = 9,
            name = "Imam Musa ibn Ja'far (as)",
            title = "Al-Kadhim (The Restrainer of Anger)",
            birthDate = "7 Safar",
            martyrdomDate = "25 Rajab",
            burialPlace = "Al-Kadhimiya Mosque, Baghdad",
            bioSummary = "The seventh infallible Imam, famous for his extreme patience, enduring long imprisonments in Abbassid dungeons but guiding thousands to truth.",
            events = listOf(
                MasoomEvent("Imam Kadhim's Birth", "7 Safar", EventType.WILADAT, "The birth of the noble restrainer of anger in the town of Abwa."),
                MasoomEvent("Imam Kadhim's Martyrdom", "25 Rajab", EventType.SHAHADAT, "Martyred in Harun al-Rashid's prison in Baghdad, crossing the bridge of Karkh.")
            )
        ),
        MasoomDetails(
            index = 10,
            name = "Imam Ali ibn Musa (as)",
            title = "Al-Ridha (The Pleasing / Appointed One)",
            birthDate = "11 Dhu al-Qa'dah",
            martyrdomDate = "30 Safar",
            burialPlace = "Imam Reza Shrine, Mashhad, Iran",
            bioSummary = "The eighth infallible Imam, famous for his debates with scholars of other religions, showcasing the supremacy of Islamic intellect.",
            events = listOf(
                MasoomEvent("Imam Ridha's Birth", "11 Dhu al-Qa'dah", EventType.WILADAT, "Born in Medina, bringing honor and academic revival."),
                MasoomEvent("Imam Ridha's Martyrdom", "30 Safar", EventType.SHAHADAT, "Poisoned by Ma'mun in Tus, establishing Mashhad as a pivot of Shia love.")
            )
        ),
        MasoomDetails(
            index = 11,
            name = "Imam Muhammad ibn Ali (as)",
            title = "Al-Jawad / Al-Taqi (The Generous / God-Conscious)",
            birthDate = "10 Rajab",
            martyrdomDate = "30 Dhu al-Qa'dah",
            burialPlace = "Al-Kadhimiya Mosque, Baghdad",
            bioSummary = "The ninth infallible Imam, the youngest of all Imams (assumed Imamate at age 7), whose debates astounded veteran contemporary scholars.",
            events = listOf(
                MasoomEvent("Imam Jawad's Birth", "10 Rajab", EventType.WILADAT, "The birth of the young genius of Ahlul Bayt, giving clarity to Shia faith."),
                MasoomEvent("Imam Jawad's Martyrdom", "30 Dhu al-Qa'dah", EventType.SHAHADAT, "Poisoned in Baghdad at the young age of 25.")
            )
        ),
        MasoomDetails(
            index = 12,
            name = "Imam Ali ibn Muhammad (as)",
            title = "Al-Hadi / Al-Naqi (The Guide / The Pure)",
            birthDate = "15 Dhu al-Hijjah",
            martyrdomDate = "3 Rajab",
            burialPlace = "Al-Askari Shrine, Samarra, Iraq",
            bioSummary = "The tenth infallible Imam, who taught Ziyarat al-Jamiah al-Kabirah and guided the Shia through a network of deputies (Wikalah) under fortress confinement.",
            events = listOf(
                MasoomEvent("Imam Hadi's Birth", "15 Dhu al-Hijjah", EventType.WILADAT, "Born in Medina, adding strength to the expanding Wikalah system."),
                MasoomEvent("Imam Hadi's Martyrdom", "3 Rajab", EventType.SHAHADAT, "Martyred under confinement by Al-Mu'tazz in Samarra.")
            )
        ),
        MasoomDetails(
            index = 13,
            name = "Imam Hasan ibn Ali (as)",
            title = "Al-Askari (The Soldier / Garrison Conceded)",
            birthDate = "8 Rabi' al-Thani",
            martyrdomDate = "8 Rabi' al-Awwal",
            burialPlace = "Al-Askari Shrine, Samarra, Iraq",
            bioSummary = "The eleventh infallible Imam, father of the Savior, spent his entire life in military surveillance but maintained connections with the global Shia community.",
            events = listOf(
                MasoomEvent("Imam Askari's Birth", "8 Rabi' al-Thani", EventType.WILADAT, "Born in Medina under Abbasid surveillance."),
                MasoomEvent("Imam Askari's Martyrdom", "8 Rabi' al-Awwal", EventType.SHAHADAT, "Poisoned by Al-Mu'tamid in Samarra, initiating the Minor Occultation.")
            )
        ),
        MasoomDetails(
            index = 14,
            name = "Imam Muhammad ibn al-Hasan (ajtf)",
            title = "Al-Mahdi / Al-Qa'im (The Guided / The Risen)",
            birthDate = "15 Sha'ban",
            martyrdomDate = "Alive (In Occultation)",
            burialPlace = "N/A (Active Guidance)",
            bioSummary = "The twelfth and current Infallible Imam, living under divine occultation, who will reappear with Jesus Christ to fill the earth with equity, peace, and absolute justice.",
            events = listOf(
                MasoomEvent("Imam Mahdi's Birth", "15 Sha'ban", EventType.WILADAT, "Birth of the ultimate Savior of humanity, marked worldwide with lights and celebrations.")
            )
        )
    )

    // Complete index of general notable dates in Shia Hijri Calendar
    val generalNotableDays = listOf(
        MasoomEvent("Eid al-Ghadir", "18 Dhu al-Hijjah", EventType.WILADAT, "The momentous coronation of Imam Ali (as) as Commander of the Faithful by Prophet Muhammad (saws) at Ghadir Khumm."),
        MasoomEvent("Eid al-Mubahalah", "24 Dhu al-Hijjah", EventType.WILADAT, "The triumph of the Prophet's Ahlul Bayt in the spiritual debate against the Christians of Najran."),
        MasoomEvent("Arba'een of Imam Hussain", "20 Safar", EventType.SHAHADAT, "The 40th-day commemoration of the tragedy of Karbala, where millions make the historic pilgrimage walk to Karbala."),
        MasoomEvent("Eid al-Fitr", "1 Shawwal", EventType.WILADAT, "The celebration of completion of the holy month of fasting (Ramadan)."),
        MasoomEvent("Eid al-Adha", "10 Dhu al-Hijjah", EventType.WILADAT, "The celebration of sacrifice, marking the culmination of Hajj.")
    )
}
