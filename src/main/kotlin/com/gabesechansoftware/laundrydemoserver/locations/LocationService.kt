package com.gabesechansoftware.laundrydemoserver.locations

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.EntityDoesNotExistException
import com.gabesechansoftware.laundrydemoserver.model.dbview.EmbeddedAddress
import com.gabesechansoftware.laundrydemoserver.model.dbview.Location
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.LocationRepository
import com.gabesechansoftware.laundrydemoserver.model.validation.LocationValidator
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

data class UploadLocation(
    val name: String,
    val street1: String,
    val street2: String?,
    val city: String,
    val state: String,
    val country: String,
    val postcode: String,
)

data class PatchLocation(
    val name: String?,
    val street1: String?,
    val street2: String?,
    val city: String?,
    val state: String?,
    val country: String?,
    val postcode: String?,
)

@Service
class LocationService(
    private val locationRepository: LocationRepository,
    private val locationValidator: LocationValidator = LocationValidator(),
) {

    fun listByOrg(organizationId: UUID): List<Location> =
        locationRepository.findByOrganizationId(organizationId)

    fun getLocation(locationId: UUID): Location =
        locationRepository.findById(locationId)
            .orElseThrow { EntityDoesNotExistException("Location $locationId does not exist") }

    @Transactional
    fun createLocation(organizationId: UUID, upload: UploadLocation): Location {
        val location = Location(
            name = upload.name,
            address = EmbeddedAddress(
                street1 = upload.street1,
                street2 = upload.street2,
                city = upload.city,
                state = upload.state,
                country = upload.country,
                postcode = upload.postcode,
            ),
            organizationId = organizationId,
        )
        validate(location)
        locationRepository.save(location)
        return location
    }

    @Transactional
    fun updateLocation(locationId: UUID, patch: PatchLocation): Location {
        val location = locationRepository.findById(locationId)
            .orElseThrow { EntityDoesNotExistException("Location $locationId does not exist") }

        patch.name?.let { location.name = it }

        val addr = location.address
        if (patch.street1 != null || patch.street2 != null || patch.city != null ||
            patch.state != null || patch.country != null || patch.postcode != null
        ) {
            location.address = EmbeddedAddress(
                street1 = patch.street1 ?: addr.street1,
                street2 = patch.street2 ?: addr.street2,
                city = patch.city ?: addr.city,
                state = patch.state ?: addr.state,
                country = patch.country ?: addr.country,
                postcode = patch.postcode ?: addr.postcode,
            )
        }

        validate(location)
        locationRepository.save(location)
        return location
    }

    @Transactional
    fun deleteLocation(locationId: UUID) {
        val location = locationRepository.findById(locationId)
            .orElseThrow { EntityDoesNotExistException("Location $locationId does not exist") }
        locationRepository.delete(location)
    }

    private fun validate(location: Location) {
        val errors = mutableListOf<String>()
        locationValidator.validateLocation(location, errors)
        if (errors.isNotEmpty()) {
            throw APIErrorException(errors)
        }
    }
}
