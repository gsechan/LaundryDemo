package com.gabesechansoftware.laundrydemoserver.organizations

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.EntityDoesNotExistException
import com.gabesechansoftware.laundrydemoserver.model.dbview.Organization
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.OrganizationRepository
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
)

@Service
class OrganizationService(
    private val organizationRepository: OrganizationRepository,
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

        validate(organization)
        organizationRepository.save(organization)
        return organization
    }

    @Transactional
    fun deleteOrganization(orgId: UUID) {
        val organization = organizationRepository.findById(orgId)
            .orElseThrow { EntityDoesNotExistException("Organization $orgId does not exist") }
        organizationRepository.delete(organization)
    }

    private fun validate(organization: Organization) {
        val errors = mutableListOf<String>()
        organizationValidator.validateOrganization(organization, errors)
        if(errors.isNotEmpty()) {
            throw APIErrorException(errors)
        }
    }
}
