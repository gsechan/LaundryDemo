package com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin

import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AdminRepository: JpaRepository<Admin, UUID>
