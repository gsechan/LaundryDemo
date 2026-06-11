package com.gabesechansoftware.laundrydemoserver.model.dbview.repositories

import com.gabesechansoftware.laundrydemoserver.model.dbview.user.Address
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID


interface AddressRepository: JpaRepository<Address, UUID> {
    fun countAddressesByUser(user: User): Int
}