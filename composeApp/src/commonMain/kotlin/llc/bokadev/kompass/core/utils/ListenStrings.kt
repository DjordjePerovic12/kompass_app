package llc.bokadev.kompass.core.utils

/** Strings for the Lamplight audio library ("Listen"). "{title}", "{duration}", "{elapsed}", "{n}" are replaced at the call site. */
data class ListenStrings(
    val freeAudio: String,
    val title: String,
    val subtitle: String,
    val allAudio: String,
    val downloaded: String,
    val streamsOnline: String,
    val needsInternet: String,
    val downloading: String,
    val englishAudio: String,
    val loading: String,
    val nowPlaying: String,
    val paused: String,
    val couldntPlay: String,
    val retry: String,
    val stop: String,
    val back15: String,
    val forward15: String,
    val nowPlayingRegion: String,
    val play: String,
    val pause: String,
    val resume: String,
    val loadingTapToCancel: String,
    val downloadFor: String,
    val downloadingTapToCancel: String,
    val downloadedTapToRemove: String,
    val removeTitle: String,
    val removeBody: String,
    val removeBodyLanguage: String,
    val remove: String,
    val cancel: String,
    val emptyDownloads: String,
    val emptyAll: String,
    val connectToPlay: String,
    val couldntPlaySnack: String,
    val downloadFailed: String,
    val progressValue: String
)

internal fun listenStringsFor(language: String): ListenStrings = when (language) {
    "fr" -> ListenStrings(
        freeAudio = "AUDIO GRATUIT", title = "Écouter", subtitle = "De courtes histoires sur les lieux de la baie.",
        allAudio = "Tout l'audio", downloaded = "Téléchargé", streamsOnline = "En ligne", needsInternet = "Internet requis",
        downloading = "Téléchargement {n} %", englishAudio = "Audio en anglais", loading = "CHARGEMENT…", nowPlaying = "EN LECTURE",
        paused = "EN PAUSE", couldntPlay = "LECTURE IMPOSSIBLE", retry = "Réessayer", stop = "Arrêter", back15 = "Reculer de 15 secondes",
        forward15 = "Avancer de 15 secondes", nowPlayingRegion = "En lecture", play = "Écouter {title}, {duration}", pause = "Mettre en pause {title}",
        resume = "Reprendre {title}, en pause à {elapsed}", loadingTapToCancel = "Chargement de {title}, touchez pour annuler",
        downloadFor = "Télécharger {title} hors ligne", downloadingTapToCancel = "Téléchargement {n} pour cent, touchez pour annuler",
        downloadedTapToRemove = "Téléchargé, touchez pour supprimer", removeTitle = "Supprimer le téléchargement ?",
        removeBody = "Il sera lu en ligne la prochaine fois.", removeBodyLanguage = "Seule la version en {lang} est supprimée. Elle sera lue en ligne la prochaine fois.",
        remove = "Supprimer", cancel = "Annuler",
        emptyDownloads = "Rien de téléchargé pour l'instant. Touchez ↓ sur une piste pour l'écouter hors ligne.",
        emptyAll = "Aucun audio pour l'instant.", connectToPlay = "Connectez-vous à Internet pour écouter ceci, ou téléchargez-le la prochaine fois que vous serez en ligne.",
        couldntPlaySnack = "Lecture impossible. Touchez pour réessayer.", downloadFailed = "Le téléchargement a échoué. Réessayez en ligne.",
        progressValue = "{elapsed} sur {duration}"
    )
    "de" -> ListenStrings(
        freeAudio = "KOSTENLOSES AUDIO", title = "Hören", subtitle = "Kurze Geschichten über Orte in der Bucht.",
        allAudio = "Alle Audios", downloaded = "Heruntergeladen", streamsOnline = "Streamt online", needsInternet = "Braucht Internet",
        downloading = "Lädt {n} %", englishAudio = "Englisches Audio", loading = "LÄDT…", nowPlaying = "LÄUFT",
        paused = "PAUSIERT", couldntPlay = "WIEDERGABE FEHLGESCHLAGEN", retry = "Erneut", stop = "Stopp", back15 = "15 Sekunden zurück",
        forward15 = "15 Sekunden vor", nowPlayingRegion = "Läuft gerade", play = "{title} abspielen, {duration}", pause = "{title} pausieren",
        resume = "{title} fortsetzen, pausiert bei {elapsed}", loadingTapToCancel = "{title} lädt, tippen zum Abbrechen",
        downloadFor = "{title} offline speichern", downloadingTapToCancel = "Lädt {n} Prozent, tippen zum Abbrechen",
        downloadedTapToRemove = "Heruntergeladen, tippen zum Entfernen", removeTitle = "Download entfernen?",
        removeBody = "Beim nächsten Mal wird gestreamt.", removeBodyLanguage = "Nur die Version auf {lang} wird entfernt. Beim nächsten Mal wird gestreamt.",
        remove = "Entfernen", cancel = "Abbrechen",
        emptyDownloads = "Noch nichts heruntergeladen. Tippe bei einem Titel auf ↓, um ihn offline zu hören.",
        emptyAll = "Noch keine Audios.", connectToPlay = "Verbinde dich mit dem Internet, um das abzuspielen, oder lade es herunter, wenn du online bist.",
        couldntPlaySnack = "Audio konnte nicht abgespielt werden. Tippe, um es erneut zu versuchen.", downloadFailed = "Download fehlgeschlagen. Versuche es online erneut.",
        progressValue = "{elapsed} von {duration}"
    )
    "es" -> ListenStrings(
        freeAudio = "AUDIO GRATIS", title = "Escuchar", subtitle = "Historias breves sobre lugares de la bahía.",
        allAudio = "Todo el audio", downloaded = "Descargado", streamsOnline = "En línea", needsInternet = "Necesita internet",
        downloading = "Descargando {n} %", englishAudio = "Audio en inglés", loading = "CARGANDO…", nowPlaying = "REPRODUCIENDO",
        paused = "EN PAUSA", couldntPlay = "NO SE PUDO REPRODUCIR", retry = "Reintentar", stop = "Detener", back15 = "Retroceder 15 segundos",
        forward15 = "Avanzar 15 segundos", nowPlayingRegion = "Reproduciendo", play = "Reproducir {title}, {duration}", pause = "Pausar {title}",
        resume = "Reanudar {title}, en pausa en {elapsed}", loadingTapToCancel = "Cargando {title}, toca para cancelar",
        downloadFor = "Descargar {title} sin conexión", downloadingTapToCancel = "Descargando {n} por ciento, toca para cancelar",
        downloadedTapToRemove = "Descargado, toca para quitar", removeTitle = "¿Quitar la descarga?",
        removeBody = "La próxima vez se reproducirá en línea.", removeBodyLanguage = "Solo se quita la versión en {lang}. La próxima vez se reproducirá en línea.",
        remove = "Quitar", cancel = "Cancelar",
        emptyDownloads = "Aún no hay descargas. Toca ↓ en una pista para escucharla sin conexión.",
        emptyAll = "Todavía no hay audio.", connectToPlay = "Conéctate a internet para reproducir esto, o descárgalo la próxima vez que estés en línea.",
        couldntPlaySnack = "No se pudo reproducir el audio. Toca para reintentar.", downloadFailed = "La descarga falló. Inténtalo de nuevo con conexión.",
        progressValue = "{elapsed} de {duration}"
    )
    "tr" -> ListenStrings(
        freeAudio = "ÜCRETSİZ SES", title = "Dinle", subtitle = "Körfezdeki yerler hakkında kısa hikâyeler.",
        allAudio = "Tüm sesler", downloaded = "İndirildi", streamsOnline = "Çevrimiçi çalar", needsInternet = "İnternet gerekli",
        downloading = "İndiriliyor %{n}", englishAudio = "İngilizce ses", loading = "YÜKLENİYOR…", nowPlaying = "ÇALIYOR",
        paused = "DURAKLATILDI", couldntPlay = "ÇALINAMADI", retry = "Tekrar dene", stop = "Durdur", back15 = "15 saniye geri",
        forward15 = "15 saniye ileri", nowPlayingRegion = "Şimdi çalıyor", play = "{title} çal, {duration}", pause = "{title} duraklat",
        resume = "{title} devam et, {elapsed} konumunda duraklatıldı", loadingTapToCancel = "{title} yükleniyor, iptal için dokunun",
        downloadFor = "{title} çevrimdışı için indir", downloadingTapToCancel = "Yüzde {n} indiriliyor, iptal için dokunun",
        downloadedTapToRemove = "İndirildi, kaldırmak için dokunun", removeTitle = "İndirme kaldırılsın mı?",
        removeBody = "Bir dahaki sefere çevrimiçi çalınır.", removeBodyLanguage = "Yalnızca {lang} sürümü kaldırılır. Bir dahaki sefere çevrimiçi çalınır.",
        remove = "Kaldır", cancel = "Vazgeç",
        emptyDownloads = "Henüz indirilen yok. Çevrimdışı dinlemek için bir parçada ↓ simgesine dokunun.",
        emptyAll = "Henüz ses yok.", connectToPlay = "Bunu çalmak için internete bağlanın veya bir dahaki çevrimiçi olduğunuzda indirin.",
        couldntPlaySnack = "Ses çalınamadı. Tekrar denemek için dokunun.", downloadFailed = "İndirme başarısız. Çevrimiçiyken tekrar deneyin.",
        progressValue = "{elapsed} / {duration}"
    )
    else -> ListenStrings(
        freeAudio = "FREE AUDIO", title = "Listen", subtitle = "Short stories about places in the bay.",
        allAudio = "All audio", downloaded = "Downloaded", streamsOnline = "Streams online", needsInternet = "Needs internet",
        downloading = "Downloading {n}%", englishAudio = "English audio", loading = "LOADING…", nowPlaying = "NOW PLAYING",
        paused = "PAUSED", couldntPlay = "COULDN'T PLAY", retry = "Retry", stop = "Stop", back15 = "Back 15 seconds",
        forward15 = "Forward 15 seconds", nowPlayingRegion = "Now playing", play = "Play {title}, {duration}", pause = "Pause {title}",
        resume = "Resume {title}, paused at {elapsed}", loadingTapToCancel = "Loading {title}, tap to cancel",
        downloadFor = "Download {title} for offline", downloadingTapToCancel = "Downloading {n} percent, tap to cancel",
        downloadedTapToRemove = "Downloaded, tap to remove", removeTitle = "Remove download?",
        removeBody = "It will stream next time.", removeBodyLanguage = "Only the {lang} version is removed. It will stream next time.",
        remove = "Remove", cancel = "Cancel",
        emptyDownloads = "Nothing downloaded yet. Tap ↓ on a track to keep it for offline listening.",
        emptyAll = "No audio yet.", connectToPlay = "Connect to the internet to play this, or download it next time you're online.",
        couldntPlaySnack = "Couldn't play audio. Tap to try again.", downloadFailed = "Download failed. Try again when you're online.",
        progressValue = "{elapsed} of {duration}"
    )
}
