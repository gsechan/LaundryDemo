package com.gabesechansoftware.laundrydemoserver.authorization

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.EntityDoesNotExistException
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.AdminRoleMembership
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin.AdminRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin.AdminRoleMembershipRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.admin.AdminRoleRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AdminRoleMembershipService(
    private val adminRepository: AdminRepository,
    private val adminRoleRepository: AdminRoleRepository,
    private val membershipRepository: AdminRoleMembershipRepository,
) {

    @Transactional
    fun assignRole(adminId: UUID, roleId: UUID): AdminRoleMembership {
        val admin = adminRepository.findById(adminId)
            .orElseThrow { EntityDoesNotExistException("Admin $adminId does not exist") }
        val role = adminRoleRepository.findById(roleId)
            .orElseThrow { EntityDoesNotExistException("Role $roleId does not exist") }

        if (membershipRepository.existsByAdminIdAndRoleId(adminId, roleId)) {
            throw APIErrorException(listOf("Admin already has this role"))
        }

        val membership = AdminRoleMembership(admin = admin, role = role)
        membershipRepository.save(membership)
        return membership
    }

    @Transactional
    fun removeMembership(membershipId: UUID) {
        val membership = membershipRepository.findById(membershipId)
            .orElseThrow { EntityDoesNotExistException("Role membership $membershipId does not exist") }
        membershipRepository.delete(membership)
    }
}
