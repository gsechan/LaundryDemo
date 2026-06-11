package com.gabesechansoftware.laundrydemoserver.model.validation

class PasswordValidator {
    fun validatePassword(password: String, errors: MutableList<String>) {
        if (password.length < 8) {
            errors.add("Password too short")
        }
    }
}
