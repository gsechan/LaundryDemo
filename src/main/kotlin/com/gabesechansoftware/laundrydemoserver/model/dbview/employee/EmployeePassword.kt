package com.gabesechansoftware.laundrydemoserver.model.dbview.employee

import com.gabesechansoftware.laundrydemoserver.model.dbview.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "employeepasswords")
class EmployeePassword(

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    var employee: Employee? = null,

    var hash: String? = null,

) : BaseEntity()
