package com.gabesechansoftware.laundrydemoserver.admins

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.authentication.AdminLoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin.AdminRepository
import com.gabesechansoftware.laundrydemoserver.model.validation.AdminValidator
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

data class UploadAdmin(
    val name: String,
    val email: String,
    val phone: String,
)

@Service
class AdminService(
    private val adminRepository: AdminRepository,
    private val adminLoginAuthenticator: AdminLoginAuthenticator,
    private val adminValidator: AdminValidator = AdminValidator(),
) {

    @Transactional
    fun createAdmin(upload: UploadAdmin, password: String): Admin {
        val errors = mutableListOf<String>()
        val admin = Admin(name = upload.name, email = upload.email, phone = upload.phone)
        adminValidator.validateAdmin(admin, errors)
        if(errors.isNotEmpty()) {
            throw APIErrorException(errors)
        }
        adminRepository.save(admin)
        adminLoginAuthenticator.createPasswordForAdmin(admin, password)
        return admin
    }
}
