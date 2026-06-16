package com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.employee

import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.EmployeePassword
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface EmployeePasswordRepository : JpaRepository<EmployeePassword, UUID> {
    @Query("SELECT p FROM EmployeePassword p WHERE p.employee.phone = :phone")
    fun findByEmployeePhone(@Param("phone") phone: String): List<EmployeePassword>

    @Query("SELECT p FROM EmployeePassword p WHERE p.employee.id = :employeeId")
    fun findByEmployeeId(@Param("employeeId") employeeId: UUID): EmployeePassword?
}
