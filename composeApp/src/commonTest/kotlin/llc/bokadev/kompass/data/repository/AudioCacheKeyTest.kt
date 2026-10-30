package llc.bokadev.kompass.data.repository

import kotlin.test.*
import llc.bokadev.kompass.domain.model.chooseAudio

/** Downloads are identified by the full storage path; the language rule never invents a track. */
class AudioCacheKeyTest {

    @Test fun sameFileNameInDifferentFoldersGetsDifferentKeys() {
        val en = audioCacheKey("places/audio/en/tryphon.mp3")
        val fr = audioCacheKey("places/audio/fr/tryphon.mp3")
        assertNotEquals(en, fr)
        assertEquals("v2_places_audio_fr_tryphon.mp3", fr)
    }

    @Test fun urlsDropSchemeAndHostAndKeepTheirExtension() {
        val key = audioCacheKey("https://x.supabase.co/storage/v1/object/public/photos/places/audio/1777-m39.mp3")
        assertEquals("v2_storage_v1_object_public_photos_places_audio_1777-m39.mp3", key)
        assertEquals(key, audioCacheKey("https://x.supabase.co/storage/v1/object/public/photos/places/audio/1777-m39.mp3?token=abc"))
    }

    @Test fun longPathsStayShortButDistinct() {
        val a = audioCacheKey("a/".repeat(60) + "guide.mp3")
        val b = audioCacheKey("b/".repeat(60) + "guide.mp3")
        assertTrue(a.length < 120)
        assertNotEquals(a, b)
        assertEquals(a, audioCacheKey("a/".repeat(60) + "guide.mp3"), "stable across calls")
    }

    @Test fun languageRuleUsesTheLanguageElseEnglishElseNothing() {
        val files = mapOf("en" to "en.mp3", "fr" to "fr.mp3", "de" to " ")
        assertEquals("fr", chooseAudio(files, "fr")?.lang)
        val de = chooseAudio(files, "de")!!
        assertEquals("en", de.lang); assertTrue(de.isEnglishFallback)
        assertFalse(chooseAudio(files, "en")!!.isEnglishFallback)
        assertNull(chooseAudio(mapOf("fr" to "fr.mp3"), "en"))
        assertNull(chooseAudio(null, "en"))
    }
}
