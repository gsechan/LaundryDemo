package com.gabesechansoftware.laundrydemoserver.admins

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.EntityDoesNotExistException
import com.gabesechansoftware.laundrydemoserver.authentication.AdminLoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.model.adminview.UploadAdmin
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin.AdminRepository
import com.gabesechansoftware.laundrydemoserver.model.validation.AdminValidator
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID


@Service
class AdminService(
    private val adminRepository: AdminRepository,
    private val adminLoginAuthenticator: AdminLoginAuthenticator,
    private val adminValidator: AdminValidator = AdminValidator(),
) {

    fun listAll(): List<Admin> {
        return adminRepository.findAll()
    }

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

    @Transactional
    fun deleteAdmin(requester: Admin, adminId: UUID) {
        if(requester.id == adminId) {
            throw APIErrorException(listOf("You cannot delete your own account"))
        }
        val admin = adminRepository.findById(adminId)
            .orElseThrow { EntityDoesNotExistException("Admin $adminId does not exist") }
        adminRepository.delete(admin)
    }
}
