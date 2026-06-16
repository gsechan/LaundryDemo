package com.gabesechansoftware.laundrydemoserver.model.validation

import com.gabesechansoftware.laundrydemoserver.model.dbview.employee.Employee

class EmployeeValidator(
    private val phoneValidator: PhoneValidator = PhoneValidator(),
    private val emailValidator: EmailValidator = EmailValidator(),
) {

    fun validateEmployee(employee: Employee, errors: MutableList<String>) {
        val phone = employee.phone
        if (phone == null) {
            errors.add("Phone is required")
        } else {
            phoneValidator.validatePhoneNumber(phone, errors)
        }

        val email = employee.email
        if (email == null) {
            errors.add("Email is required")
        } else {
            emailValidator.validateEmail(email, errors)
        }
    }
}
