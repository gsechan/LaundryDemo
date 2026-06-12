package com.gabesechansoftware.laundrydemoserver.users

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.auth.LoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadAddress
import com.gabesechansoftware.laundrydemoserver.model.customerview.UploadUser
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.AddressRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.OrganizationRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.UserRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.Address
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.model.validation.AddressValidator
import com.gabesechansoftware.laundrydemoserver.model.validation.PasswordValidator
import com.gabesechansoftware.laundrydemoserver.model.validation.UserValidator
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val loginAuthenticator: LoginAuthenticator,
    private val organizationRepository: OrganizationRepository,
    private val addressRepository: AddressRepository,
    private val addressValidator: AddressValidator = AddressValidator(),
    private val passwordValidator: PasswordValidator = PasswordValidator(),
    private val userValidator: UserValidator = UserValidator(),
) {

    @Transactional
    fun createUser(user: UploadUser, password: String, org: UUID): User {
        val errors = mutableListOf<String>()
        passwordValidator.validatePassword(password, errors)
        userValidator.validateUser(user, errors)
        if(errors.isEmpty()) {
            val dbUser = user.toDBUser(organizationRepository.getReferenceById(org))
            userRepository.save(dbUser)
            loginAuthenticator.setPasswordForUser(dbUser, password)
            return dbUser
        }
        else {
            throw APIErrorException(errors)
        }
    }


    fun addAddress(user: User, address: UploadAddress): Address {
        val errors = mutableListOf<String>()
        addressValidator.validateCustomerAddress(address, errors)
        val count = user.addresses.size
        val hasOtherAddress = count > 0

        /* TODO:  This constraint now needs to be known by the user validator and here.  Can we consolidate?
         If we added the address to user now and validated him, that would work.  But validator works on uploaduser, would need to
         either duplicate functionality there or convert-  this is less duplication and avoids conversion
         */
        if(count >= 5) {
            errors.add("Too many addresses")
        }
        val dbAddress = address.toDBAddress(user, !hasOtherAddress)
        if(errors.isEmpty()) {
            addressRepository.save(dbAddress)
            user.addresses.add(dbAddress)
            return dbAddress
        }
        else {
            throw APIErrorException(errors)
        }
    }

}