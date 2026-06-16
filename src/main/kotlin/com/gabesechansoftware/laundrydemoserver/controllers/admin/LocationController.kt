package com.gabesechansoftware.laundrydemoserver.controllers.admin

import com.gabesechansoftware.laundrydemoserver.NetworkErrorType
import com.gabesechansoftware.laundrydemoserver.NetworkResponse
import com.gabesechansoftware.laundrydemoserver.authentication.AuthenticatedAdmin
import com.gabesechansoftware.laundrydemoserver.authorization.AdminAuthorizationService
import com.gabesechansoftware.laundrydemoserver.authorization.AdminPermissions
import com.gabesechansoftware.laundrydemoserver.locations.LocationService
import com.gabesechansoftware.laundrydemoserver.locations.PatchLocation
import com.gabesechansoftware.laundrydemoserver.locations.UploadLocation
import com.gabesechansoftware.laundrydemoserver.model.dbview.Location
import com.gabesechansoftware.laundrydemoserver.model.dbview.admin.Admin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class LocationAddressView(
    val street1: String,
    val street2: String?,
    val city: String,
    val state: String,
    val country: String,
    val postcode: String,
)

data class LocationView(
    val id: String,
    val organizationId: String,
    val name: String,
    val address: LocationAddressView,
)

fun Location.toView() = LocationView(
    id = id.toString(),
    organizationId = organizationId.toString(),
    name = name,
    address = LocationAddressView(
        street1 = address.street1,
        street2 = address.street2,
        city = address.city,
        state = address.state,
        country = address.country,
        postcode = address.postcode,
    ),
)

data class CreateLocationRequest(val location: UploadLocation)
data class PatchLocationRequest(val location: PatchLocation)

@RestController
class LocationController(
    private val locationService: LocationService,
    private val adminAuthorizationService: AdminAuthorizationService,
) {

    @GetMapping("/admin/organizations/{orgId}/locations")
    fun listLocations(
        @PathVariable orgId: UUID,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<List<LocationView>> {
        return NetworkResponse(locationService.listByOrg(orgId).map { it.toView() })
    }

    @GetMapping("/admin/organizations/{orgId}/locations/{id}")
    fun getLocation(
        @PathVariable orgId: UUID,
        @PathVariable id: UUID,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<LocationView> {
        return NetworkResponse(locationService.getLocation(id).toView())
    }

    @PostMapping("/admin/organizations/{orgId}/locations")
    fun createLocation(
        @PathVariable orgId: UUID,
        @RequestBody request: CreateLocationRequest,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<LocationView> {
        if (!adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.EDIT_ORG), authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to create locations")
        }
        val location = locationService.createLocation(orgId, request.location)
        return NetworkResponse(location.toView())
    }

    @PatchMapping("/admin/organizations/{orgId}/locations/{id}")
    fun updateLocation(
        @PathVariable orgId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: PatchLocationRequest,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<LocationView> {
        if (!adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.EDIT_ORG), authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to edit locations")
        }
        val location = locationService.updateLocation(id, request.location)
        return NetworkResponse(location.toView())
    }

    @DeleteMapping("/admin/organizations/{orgId}/locations/{id}")
    fun deleteLocation(
        @PathVariable orgId: UUID,
        @PathVariable id: UUID,
        @AuthenticatedAdmin authedAdmin: Admin,
    ): NetworkResponse<Unit> {
        if (!adminAuthorizationService.permissionsCheckAll(listOf(AdminPermissions.EDIT_ORG), authedAdmin)) {
            return NetworkResponse(NetworkErrorType.NOT_AUTHORIZED, "Not authorized to delete locations")
        }
        locationService.deleteLocation(id)
        return NetworkResponse(Unit)
    }
}
