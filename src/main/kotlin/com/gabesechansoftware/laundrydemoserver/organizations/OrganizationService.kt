package com.gabesechansoftware.laundrydemoserver.organizations

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.EntityDoesNotExistException
import com.gabesechansoftware.laundrydemoserver.model.dbview.Organization
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.OrganizationRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.SessionRepository
import com.gabesechansoftware.laundrydemoserver.model.validation.OrganizationValidator
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

data class UploadOrganization(
    val name: String,
    val defaultLocale: String,
)

data class PatchOrganization(
    val name: String?,
    val defaultLocale: String?,
    val isDeleted: Boolean?,
)

@Service
class OrganizationService(
    private val organizationRepository: OrganizationRepository,
    private val sessionRepository: SessionRepository,
    private val organizationValidator: OrganizationValidator = OrganizationValidator(),
) {

    fun listAll(): List<Organization> {
        return organizationRepository.findAll()
    }

    @Transactional
    fun createOrganization(upload: UploadOrganization): Organization {
        val organization = Organization(name = upload.name, defaultLocale = upload.defaultLocale)
        validate(organization)
        organizationRepository.save(organization)
        return organization
    }

    @Transactional
    fun updateOrganization(orgId: UUID, patch: PatchOrganization): Organization {
        val organization = organizationRepository.findById(orgId)
            .orElseThrow { EntityDoesNotExistException("Organization $orgId does not exist") }

        patch.name?.let { organization.name = it }
        patch.defaultLocale?.let { organization.defaultLocale = it }
        patch.isDeleted?.let { organization.isDeleted = it }

        validate(organization)
        organizationRepository.save(organization)

        // Soft-delete via patch: when the org is marked deleted, kick out its users.
        if (patch.isDeleted == true) {
            sessionRepository.deleteByOrganizationId(organization.id)
        }
        return organization
    }

    /** Soft delete: mark the org deleted and purge its users' sessions. */
    @Transactional
    fun deleteOrganization(orgId: UUID) {
        val organization = organizationRepository.findById(orgId)
            .orElseThrow { EntityDoesNotExistException("Organization $orgId does not exist") }
        organization.isDeleted = true
        organizationRepository.save(organization)
        sessionRepository.deleteByOrganizationId(orgId)
    }

    private fun validate(organization: Organization) {
        val errors = mutableListOf<String>()
        organizationValidator.validateOrganization(organization, errors)
        if(errors.isNotEmpty()) {
            throw APIErrorException(errors)
        }
    }
}
