package com.gabesechansoftware.laundrydemoserver.model.customerview

import com.gabesechansoftware.laundrydemoserver.assertSize
import com.gabesechansoftware.laundrydemoserver.model.dbview.Organization
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User as DBUser
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.Address as DBAddress
import kotlin.test.Test
import kotlin.test.assertEquals

class UserConversionTests {
    @Test
    fun `toCustomer on Address converts correctly`() {
        val address = DBAddress(street1 = "s1", street2 = "s2", city = "city", state = "state", country = "country", postcode = "postalCode", isDefault = true)
        val result = address.toCustomer()
        assertAddressEqual(address, result)
    }

    @Test
    fun `toCustomer on User converts correctly`() {
        val address = DBAddress(street1 = "s1", street2 = "s2", city = "city", state = "state", country = "country", postcode = "postalCode", isDefault = true)
        val organization = Organization("Laundry","en-us")
        val user = DBUser(name = "Gabe", email = "test@example.com", phone = "3128675309", organization = organization, addresses = mutableListOf(address))

        val result = user.toCustomer()
        assertEquals(user.name, result.name)
        assertEquals(user.email, result.email)
        assertEquals(user.phone, result.phone)
        assertSize(1, result.addresses)
        assertAddressEqual(address, result.addresses[0])

    }

    @Test
    fun `toCustomer on User puts default address first`() {
        val address = DBAddress(street1 = "s1", street2 = "s2", city = "city", state = "state", country = "country", postcode = "postalCode", isDefault = false)
        val address2 = DBAddress(street1 = "s2", street2 = "s2", city = "city", state = "state", country = "country", postcode = "postalCode", isDefault = true)
        val organization = Organization("Laundry","en-us")
        val user = DBUser(name = "Gabe", email = "test@example.com", phone = "3128675309", organization = organization, addresses = mutableListOf(address, address2))

        val result = user.toCustomer()
        assertEquals(user.name, result.name)
        assertEquals(user.email, result.email)
        assertEquals(user.phone, result.phone)
        assertSize(2, result.addresses)
        assertAddressEqual(address2, result.addresses[0])
        assertAddressEqual(address, result.addresses[1])

    }


    @Test
    fun `applyPatch on User with all fields set updates name, email and phone`() {
        val organization = Organization("Laundry", "en-us")
        val user = DBUser(name = "Gabe", email = "test@example.com", phone = "3128675309", organization = organization, addresses = mutableListOf())
        val patch = PatchUser(name = "NewName", email = "new@example.com", phone = "3125550000", password = "newpassword")

        user.applyPatch(patch)

        assertEquals("NewName", user.name)
        assertEquals("new@example.com", user.email)
        assertEquals("3125550000", user.phone)
    }

    @Test
    fun `applyPatch on User leaves fields with null patch values unchanged`() {
        val organization = Organization("Laundry", "en-us")
        val user = DBUser(name = "Gabe", email = "test@example.com", phone = "3128675309", organization = organization, addresses = mutableListOf())
        val patch = PatchUser(name = "NewName", email = null, phone = null, password = null)

        user.applyPatch(patch)

        assertEquals("NewName", user.name)
        assertEquals("test@example.com", user.email)
        assertEquals("3128675309", user.phone)
    }

    @Test
    fun `applyPatch on User with all null values leaves the user unchanged`() {
        val organization = Organization("Laundry", "en-us")
        val user = DBUser(name = "Gabe", email = "test@example.com", phone = "3128675309", organization = organization, addresses = mutableListOf())
        val patch = PatchUser(name = null, email = null, phone = null, password = null)

        user.applyPatch(patch)

        assertEquals("Gabe", user.name)
        assertEquals("test@example.com", user.email)
        assertEquals("3128675309", user.phone)
    }

    @Test
    fun `applyPatch on User does not change the organization or addresses`() {
        val address = DBAddress(street1 = "s1", street2 = "s2", city = "city", state = "state", country = "country", postcode = "postalCode", isDefault = true)
        val organization = Organization("Laundry", "en-us")
        val user = DBUser(name = "Gabe", email = "test@example.com", phone = "3128675309", organization = organization, addresses = mutableListOf(address))
        val patch = PatchUser(name = "NewName", email = "new@example.com", phone = "3125550000", password = "newpassword")

        user.applyPatch(patch)

        assertEquals(organization, user.organization)
        assertSize(1, user.addresses)
        assertEquals(address, user.addresses[0])
    }

    @Test
    fun `applyPatch with all fields set updates every field`() {
        val address = DBAddress(street1 = "s1", street2 = "s2", city = "city", state = "state", country = "country", postcode = "postalCode", isDefault = true)
        val patch = PatchAddress(
            street1 = "newStreet1",
            street2 = "newStreet2",
            city = "newCity",
            state = "newState",
            country = "newCountry",
            postcode = "newPostcode",
        )

        address.applyPatch(patch)

        assertEquals("newStreet1", address.street1)
        assertEquals("newStreet2", address.street2)
        assertEquals("newCity", address.city)
        assertEquals("newState", address.state)
        assertEquals("newCountry", address.country)
        assertEquals("newPostcode", address.postcode)
    }

    @Test
    fun `applyPatch leaves fields with null patch values unchanged`() {
        val address = DBAddress(street1 = "s1", street2 = "s2", city = "city", state = "state", country = "country", postcode = "postalCode", isDefault = true)
        val patch = PatchAddress(
            street1 = "newStreet1",
            street2 = null,
            city = "newCity",
            state = null,
            country = null,
            postcode = null,
        )

        address.applyPatch(patch)

        assertEquals("newStreet1", address.street1)
        assertEquals("s2", address.street2)
        assertEquals("newCity", address.city)
        assertEquals("state", address.state)
        assertEquals("country", address.country)
        assertEquals("postalCode", address.postcode)
    }

    @Test
    fun `applyPatch with all null values leaves the address unchanged`() {
        val address = DBAddress(street1 = "s1", street2 = "s2", city = "city", state = "state", country = "country", postcode = "postalCode", isDefault = true)
        val patch = PatchAddress(street1 = null, street2 = null, city = null, state = null, country = null, postcode = null)

        address.applyPatch(patch)

        assertEquals("s1", address.street1)
        assertEquals("s2", address.street2)
        assertEquals("city", address.city)
        assertEquals("state", address.state)
        assertEquals("country", address.country)
        assertEquals("postalCode", address.postcode)
    }

    private fun assertAddressEqual(dbAddress: DBAddress, customerAddress: Address) {
        assertEquals(dbAddress.id.toString(), customerAddress.id)
        assertEquals(dbAddress.street1, customerAddress.street1)
        assertEquals(dbAddress.street2, customerAddress.street2)
        assertEquals(dbAddress.city, customerAddress.city)
        assertEquals(dbAddress.country, customerAddress.country)
        assertEquals(dbAddress.state, customerAddress.state)
        assertEquals(dbAddress.postcode, customerAddress.postcode)
    }
}
