package com.clex.android.data

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * JVM unit tests for pure-Kotlin helpers on [ClexChainApi].
 *
 * These do not touch `android.*` and run on the host JVM via
 * `./gradlew :app:testDebugUnitTest`.
 */
class ClexChainApiTest {

    @Test
    fun hashBytes_emptyInput_returnsSha256OfEmpty() {
        val expected = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        assertEquals(expected, ClexChainApi.hashBytes(ByteArray(0)))
    }

    @Test
    fun hashBytes_abc_returnsKnownSha256() {
        val expected = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
        assertEquals(expected, ClexChainApi.hashBytes("abc".toByteArray()))
    }

    @Test
    fun fileCategory_classifiesCommonMimeTypes() {
        assertEquals("image", ClexChainApi.fileCategory("image/png"))
        assertEquals("image", ClexChainApi.fileCategory("image/jpeg"))
        assertEquals("video", ClexChainApi.fileCategory("video/mp4"))
        assertEquals("audio", ClexChainApi.fileCategory("audio/mpeg"))
        assertEquals("pdf", ClexChainApi.fileCategory("application/pdf"))
        assertEquals("archive", ClexChainApi.fileCategory("application/zip"))
        assertEquals("archive", ClexChainApi.fileCategory("application/x-tar"))
        assertEquals("archive", ClexChainApi.fileCategory("application/gzip"))
        assertEquals("document", ClexChainApi.fileCategory("application/msword"))
        assertEquals("document", ClexChainApi.fileCategory("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        assertEquals("document", ClexChainApi.fileCategory("text/plain"))
        assertEquals("other", ClexChainApi.fileCategory("application/octet-stream"))
    }
}
