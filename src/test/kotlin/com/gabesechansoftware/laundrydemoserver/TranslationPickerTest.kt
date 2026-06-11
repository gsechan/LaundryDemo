package com.gabesechansoftware.laundrydemoserver

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import kotlin.test.assertEquals

class TranslationPickerTest {

    val translationEnUS = Transaltion("testEnUS", "en-US")
    val translationEn = Transaltion("testEn", "en")
    val translationEsES = Transaltion("testEsES", "es-ES")
    val translationFrFr = Transaltion("testFrFR", "fr-FR")

    @Test
    fun `findNameMatchingBestLocale - empty names returns null`() {
        assertNull(findNameMatchingBestLocale(emptyList(), listOf("en-US")))
    }

    @Test
    fun `findNameMatchingBestLocale - empty locales returns null`() {
        assertNull(findNameMatchingBestLocale(listOf(translationEnUS), emptyList()))
    }

    @Test
    fun `findNameMatchingBestLocale - exact locale match exists and is returned`() {
        val result =findNameMatchingBestLocale(listOf(translationFrFr, translationEnUS), listOf("en-US"))
        assertEquals(translationEnUS.name, result)
    }

    @Test
    fun `findNameMatchingBestLocale - partial locale match exists and is returned`() {
        val result =findNameMatchingBestLocale(listOf(translationFrFr, translationEn), listOf("en-US"))
        assertEquals(translationEn.name, result)
    }

    @Test
    fun `findNameMatchingBestLocale - no match returns null`() {
        val result =findNameMatchingBestLocale(listOf(translationFrFr, translationEn), listOf("es-ES"))
        assertNull(result)
    }

    @Test
    fun `findNameMatchingBestLocale - both matches, returns full match`() {
        val result =findNameMatchingBestLocale(listOf(translationEn, translationEnUS), listOf("en-US"))
        assertEquals(translationEnUS.name, result)
    }

    @Test
    fun `findNameMatchingBestLocale - two locales, two full matches, returns first locale match`() {
        val result =findNameMatchingBestLocale(listOf(translationEsES, translationEnUS), listOf("en-US", "es-ES"))
        assertEquals(translationEnUS.name, result)
    }

    @Test
    fun `findNameMatchingBestLocale - two locales, partial on first full on second returns partial on first`() {
        val result =findNameMatchingBestLocale(listOf(translationEn, translationEsES), listOf("en-US", "es-ES"))
        assertEquals(translationEnUS.name, result)
    }

    @Test
    fun `findNameMatchingBestLocale - two locales, match full on second returns match`() {
        val result =findNameMatchingBestLocale(listOf(translationFrFr, translationEsES), listOf("en-US", "es-ES"))
        assertEquals(translationEnUS.name, result)
    }

    @Test
    fun `findNameMatchingBestLocale - two locales, match partial on second returns match`() {
        val result =findNameMatchingBestLocale(listOf(translationFrFr, translationEn), listOf("es-ES", "en-US"))
        assertEquals(translationEn.name, result)
    }

    @Test
    fun `findNameMatchingBestLocale - two locales,no matches`() {
        val result =findNameMatchingBestLocale(listOf(translationFrFr), listOf("es-ES", "en-US"))
        assertNull(result)
    }

}