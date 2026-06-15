package com.gabesechansoftware.laundrydemoserver.users

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.EntityDoesNotExistException
import com.gabesechansoftware.laundrydemoserver.authentication.UserLoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.model.customerview.PatchAddress
import com.gabesechansoftware.laundrydemoserver.model.customerview.PatchUser
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadAddress
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadUser
import com.gabesechansoftware.laundrydemoserver.model.customerview.applyPatch
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.AddressRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.OrganizationRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.customer.UserRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.Address
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.model.validation.AddressValidator
import com.gabesechansoftware.laundrydemoserver.model.validation.UserValidator
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userLoginAuthenticator: UserLoginAuthenticator,
    private val organizationRepository: OrganizationRepository,
    private val addressRepository: AddressRepository,
    private val addressValidator: AddressValidator = AddressValidator(),
    private val userValidator: UserValidator = UserValidator(),
) {

    fun listByOrganization(orgId: UUID): List<User> {
        return userRepository.findByOrganizationId(orgId)
    }

    @Transactional
    fun createUser(user: UploadUser, password: String, org: UUID): User {
        val errors = mutableListOf<String>()
        val dbUser = user.toDBUser(organizationRepository.getReferenceById(org))
        userValidator.validateUser(dbUser, errors)
        if(errors.isEmpty()) {
            userRepository.save(dbUser)
            userLoginAuthenticator.createPasswordForUser(dbUser, password)
            return dbUser
        }
        else {
            throw APIErrorException(errors)
        }
    }


    fun addAddress(user: User, address: UploadAddress): Address {
        val errors = mutableListOf<String>()
        val count = user.addresses.size
        val hasOtherAddress = count > 0
        val dbAddress = address.toDBAddress(user, !hasOtherAddress)
        addressValidator.validateAddress(dbAddress, errors)

        /* TODO:  This constraint now needs to be known by the user validator and here.  Can we consolidate?
         If we added the address to user now and validated him, that would work.  But validator works on uploaduser, would need to
         either duplicate functionality there or convert-  this is less duplication and avoids conversion.  Might make sense
         when we have an update User function, and this can call that?
         */
        if(count >= 5) {
            errors.add("Too many addresses")
        }
        if(errors.isEmpty()) {
            addressRepository.save(dbAddress)
            user.addresses.add(dbAddress)
            return dbAddress
        }
        else {
            throw APIErrorException(errors)
        }
    }

    @Transactional
    fun deleteAddress(user: User, addressId: UUID) {
        val address = user.addresses.find { it.id == addressId }
            ?: throw EntityDoesNotExistException("Address $addressId does not exist")
        user.addresses.remove(address)
        addressRepository.delete(address)
    }

    @Transactional
    fun updateAddress(user: User, addressId: UUID, patch: PatchAddress): Address {
        val address = user.addresses.find { it.id == addressId }
            ?: throw EntityDoesNotExistException("Address $addressId does not exist")

        val errors = mutableListOf<String>()
        address.applyPatch(patch)
        addressValidator.validateAddress(address, errors)
        if(errors.isNotEmpty()) {
            throw APIErrorException(errors)
        }

        addressRepository.save(address)
        return address
    }

    @Transactional
    fun updateUser(user:User, patch: PatchUser): User {
        val errors = mutableListOf<String>()
        user.applyPatch(patch)
        userValidator.validateUser(user, errors)
        if(errors.isNotEmpty()) {
            throw APIErrorException(errors)
        }
        userRepository.save(user)
        if(patch.password != null) {
            userLoginAuthenticator.updatePasswordForUser(user, patch.password)
        }
        return user
    }

}