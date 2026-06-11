package com.gabesechansoftware.laundrydemoserver

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

fun <T>assertNotEmpty(x: Collection<T>) = assertFalse(x.isEmpty())
fun <T>assertEmpty(x: Collection<T>) = assertTrue(x.isEmpty())
fun <T>assertSize(size:Int, x: Collection<T>) = assertEquals(size, x.size)