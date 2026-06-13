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
