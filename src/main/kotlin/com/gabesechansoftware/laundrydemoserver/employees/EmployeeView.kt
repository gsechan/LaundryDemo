package com.gabesechansoftware.laundrydemoserver.employees

data class EmployeeView(
    val id: String,
    val name: String?,
    val email: String?,
    val phone: String?,
    val organizationId: String?,
)
