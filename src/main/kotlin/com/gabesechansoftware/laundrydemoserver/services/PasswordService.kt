package com.gabesechansoftware.laundrydemoserver.services

import com.gabesechansoftware.laundrydemoserver.model.dbview.auth.Password
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.PasswordRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PasswordService(@Autowired private val passwordRepo: PasswordRepository)  {

    fun findPossiblePassword(org: UUID, phone: String): Password {
        return passwordRepo.findByOrganizationIdAndPhone(org, phone) ?:
          throw NoSuchElementException("No password found for org $org and phone $phone")
    }
}