package com.gabesechansoftware.laundrydemoserver.model.dbview.repositories

import com.gabesechansoftware.laundrydemoserver.model.dbview.user.Address
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID


interface AddressRepository: JpaRepository<Address, UUID>