package com.gabesechansoftware.laundrydemoserver.users

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.auth.LoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.AddressRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.OrganizationRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.UserRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.Address
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.model.validation.AddressValidator
import com.gabesechansoftware.laundrydemoserver.model.validation.PasswordValidator
import com.gabesechansoftware.laundrydemoserver.model.validation.UserValidator
import jakarta.transaction.Transactional
import com.gabesechansoftware.laundrydemoserver.model.customerview.Address as CustomerAddress
import com.gabesechansoftware.laundrydemoserver.model.customerview.User as CustomerUser
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
    fun createUser(user: CustomerUser, password: String, org: UUID): User {
        val errors = mutableListOf<String>()
        passwordValidator.validatePassword(password, errors)
        userValidator.validateUser(user, errors)
        user.addresses.forEach {
            addressValidator.validateCustomerAddress(it, errors)
        }
        if(user.addresses.size > 5) {
            errors.add("Too many addresses")
        }
        if(errors.isEmpty()) {
            val dbUser = User().apply {
                name = user.name
                email = user.email
                phone = user.phone
                organization = organizationRepository.getReferenceById(org)
                addresses = user.addresses.mapIndexed { index, customerAddress ->
                    customerAddress.toAddress(null, index == 0)
                }.toMutableList()
            }
            userRepository.save(dbUser)
            loginAuthenticator.setPasswordForUser(dbUser, password)
            return dbUser
        }
        else {
            throw APIErrorException(errors)
        }
    }


    fun addAddress(user: User, address: CustomerAddress): Address {
        val errors = mutableListOf<String>()
        addressValidator.validateCustomerAddress(address, errors)
        val count = addressRepository.countAddressesByUser(user)
        val hasOtherAddress = count > 0
        if(count >= 5) {
            errors.add("Too many addresses")
        }
        val dbAddress = address.toAddress(user, !hasOtherAddress)
        if(errors.isEmpty()) {
            addressRepository.save(dbAddress)
            user.addresses.add(dbAddress)
            return dbAddress
        }
        else {
            throw APIErrorException(errors)
        }
    }

    private fun CustomerAddress.toAddress(user: User?, isDefault: Boolean): Address {
        return Address().apply {
            street1 = this@toAddress.street1
            street2 = this@toAddress.street2
            city = this@toAddress.city
            state = this@toAddress.state
            country = this@toAddress.country
            postcode = this@toAddress.postcode
            this.isDefault = isDefault
            this.user = user
        }
    }

}