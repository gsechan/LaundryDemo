package com.gabesechansoftware.laundrydemoserver.controllers.admin

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.authentication.AuthenticatedAdmin
import com.gabesechansoftware.laundrydemoserver.authorization.AdminAuthorizationService
import com.gabesechansoftware.laundrydemoserver.authorization.AdminPermissions
import com.gabesechansoftware.laundrydemoserver.model.dbview.Organization
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import com.gabesechansoftware.laundrydemoserver.organizations.OrganizationService
import com.gabesechansoftware.laundrydemoserver.organizations.PatchOrganization
import com.gabesechansoftware.laundrydemoserver.organizations.UploadOrganization
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID


data class OrganizationView(
    val id: String,
    val name: String?,
    val defaultLocale: String?,
    val isDeleted: Boolean,
)

fun Organization.toView() = OrganizationView(id.toString(), name, defaultLocale, isDeleted)

data class CreateOrganizationRequest(val organization: UploadOrganization)
data class PatchOrganizationRequest(val organization: PatchOrganization)


@RestController
class OrganizationController(
    private val organizationService: OrganizationService,
    private val adminAuthorizationService: AdminAuthorizationService,
) {

    @GetMapping("/admin/organizations")
    fun listOrganizations(
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<List<OrganizationView>> {
        return NetworkResponse(organizationService.listAll().map { it.toView() })
    }

    @PostMapping("/admin/organizations")
    fun createOrganization(
        @RequestBody request: CreateOrganizationRequest,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<OrganizationView> {
        if (!adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.CREATE_ORG), authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to create organizations")
        }
        val organization = organizationService.createOrganization(request.organization)
        return NetworkResponse(organization.toView())
    }

    @PatchMapping("/admin/organizations/{id}")
    fun updateOrganization(
        @PathVariable id: UUID,
        @RequestBody request: PatchOrganizationRequest,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<OrganizationView> {
        val canEdit = adminAuthorizationService.permissionsCheckAny(
            listOf(AdminPermissions.EDIT_ORG, AdminPermissions.CREATE_ORG),
            authedAdmin
        )
        if (!canEdit) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to edit organizations")
        }
        // Changing the soft-delete flag requires delete permission, not just edit.
        if (request.organization.isDeleted != null &&
            !adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.DELETE_ORG), authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to change the deleted state of organizations")
        }
        val organization = organizationService.updateOrganization(id, request.organization)
        return NetworkResponse(organization.toView())
    }

    @DeleteMapping("/admin/organizations/{id}")
    fun deleteOrganization(
        @PathVariable id: UUID,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<Unit> {
        if (!adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.DELETE_ORG), authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to delete organizations")
        }
        organizationService.deleteOrganization(id)
        return NetworkResponse(Unit)
    }
}
