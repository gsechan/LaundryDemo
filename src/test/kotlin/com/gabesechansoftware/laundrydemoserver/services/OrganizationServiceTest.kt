package com.gabesechansoftware.laundrydemoserver.services

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals

@SpringBootTest
class OrganizationServiceTest {
    @Autowired
    private lateinit var service: OrganizationService

    @Test
    fun whenApplicationStarts_thenHibernateCreatesInitialRecords() {
        val books = service.findAll()
    }
}