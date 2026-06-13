package com.gabesechansoftware.laundrydemoserver.model.dbview

import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BaseEntityTests {

    @Test
    fun `equals is true for reference equality`() {
        val entity = Organization()
        assertTrue(entity == entity)
    }

    @Test
    fun `equals is false for null`() {
        val entity = Organization()
        assertFalse( entity == null)
    }

    @Test
    fun `equals is false when called directly with a null argument`() {
        val entity = Organization()
        assertFalse(entity.equals(null))
    }

    @Test
    fun `equals is false for different types`() {
        val entity = Organization()
        val user  = User().apply { id = entity.id }
        assertFalse( entity == user)
    }

    @Test
    fun `equals is true for same ids`() {
        val entity = Organization()
        val entity2  = Organization().apply { id = entity.id }
        assertTrue( entity == entity2)
    }

    @Test
    fun `equals is true for different ids`() {
        val entity = Organization()
        val entity2  = Organization()
        assertFalse( entity == entity2)
    }

    @Test
    fun `hashcode is the hashcode of the id`() {
        val org = Organization()
        assertEquals(org.id.hashCode(), org.hashCode())
    }

    //test hashcode is id hashcode

}