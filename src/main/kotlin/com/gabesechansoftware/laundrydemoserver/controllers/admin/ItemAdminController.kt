package com.gabesechansoftware.laundrydemoserver.controllers.admin

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.authentication.AuthenticatedAdmin
import com.gabesechansoftware.laundrydemoserver.authorization.AdminAuthorizationService
import com.gabesechansoftware.laundrydemoserver.authorization.AdminPermissions
import com.gabesechansoftware.laundrydemoserver.catalog.ItemService
import com.gabesechansoftware.laundrydemoserver.catalog.PatchItem
import com.gabesechansoftware.laundrydemoserver.catalog.PatchItemName
import com.gabesechansoftware.laundrydemoserver.catalog.UploadItem
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.Item
import com.gabesechansoftware.laundrydemoserver.model.dbview.catalog.ItemName
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID


data class AdminItemNameView(
    val id: String,
    val name: String?,
    val locale: String?,
)

fun ItemName.toView() = AdminItemNameView(id.toString(), name, locale)

data class AdminItemView(
    val id: String,
    val organizationId: String?,
    val price: String?,
    val itemType: String,
    val names: List<AdminItemNameView>,
)

fun Item.toAdminView() = AdminItemView(
    id = id.toString(),
    organizationId = organization?.toString(),
    price = price?.toString(),
    itemType = itemType.name,
    names = names.map { it.toView() },
)

data class PatchItemRequest(val item: PatchItem)
data class PostItemRequest(val item: UploadItem)
data class PostItemNameRequest(val name: String, val locale: String)
data class PatchItemNameRequest(val name: String?, val locale: String?)


@RestController
class ItemAdminController(
    private val itemService: ItemService,
    private val adminAuthorizationService: AdminAuthorizationService,
) {

    private fun canEditOrg(admin: Admin) =
        adminAuthorizationService.permissionsCheckAny(
            listOf(AdminPermissions.EDIT_ORG, AdminPermissions.CREATE_ORG),
            admin
        )

    @GetMapping("/admin/organizations/{orgId}/items")
    fun listItems(
        @PathVariable orgId: UUID,
    ): NetworkResponse<List<AdminItemView>> {
        return NetworkResponse(itemService.getItems(orgId).map { it.toAdminView() })
    }

    @PostMapping("/admin/organizations/{orgId}/items")
    fun createItem(
        @PathVariable orgId: UUID,
        @RequestBody request: PostItemRequest,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<AdminItemView> {
        if (!canEditOrg(authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to create items")
        }
        return NetworkResponse(itemService.createItem(orgId, request.item).toAdminView())
    }

    @PatchMapping("/admin/organizations/{orgId}/items/{itemId}")
    fun updateItem(
        @PathVariable orgId: UUID,
        @PathVariable itemId: UUID,
        @RequestBody request: PatchItemRequest,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<AdminItemView> {
        if (!canEditOrg(authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to edit items")
        }
        return NetworkResponse(itemService.updateItem(orgId, itemId, request.item).toAdminView())
    }

    @DeleteMapping("/admin/organizations/{orgId}/items/{itemId}")
    fun deleteItem(
        @PathVariable orgId: UUID,
        @PathVariable itemId: UUID,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<Unit> {
        if (!canEditOrg(authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to delete items")
        }
        itemService.deleteItem(orgId, itemId)
        return NetworkResponse(Unit)
    }

    @PostMapping("/admin/organizations/{orgId}/items/{itemId}/names")
    fun addItemName(
        @PathVariable orgId: UUID,
        @PathVariable itemId: UUID,
        @RequestBody request: PostItemNameRequest,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<AdminItemNameView> {
        if (!canEditOrg(authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to edit items")
        }
        return NetworkResponse(itemService.addItemName(orgId, itemId, request.name, request.locale).toView())
    }

    @PatchMapping("/admin/organizations/{orgId}/items/{itemId}/names/{nameId}")
    fun updateItemName(
        @PathVariable orgId: UUID,
        @PathVariable itemId: UUID,
        @PathVariable nameId: UUID,
        @RequestBody request: PatchItemNameRequest,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<AdminItemNameView> {
        if (!canEditOrg(authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to edit items")
        }
        val updated = itemService.updateItemName(orgId, itemId, nameId, PatchItemName(request.name, request.locale))
        return NetworkResponse(updated.toView())
    }

    @DeleteMapping("/admin/organizations/{orgId}/items/{itemId}/names/{nameId}")
    fun deleteItemName(
        @PathVariable orgId: UUID,
        @PathVariable itemId: UUID,
        @PathVariable nameId: UUID,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<Unit> {
        if (!canEditOrg(authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to edit items")
        }
        itemService.deleteItemName(orgId, itemId, nameId)
        return NetworkResponse(Unit)
    }
}
