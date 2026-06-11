package com.gabesechansoftware.laundrydemoserver.users

import com.gabesechansoftware.laundrydemoserver.APIErrorException
import com.gabesechansoftware.laundrydemoserver.auth.LoginAuthenticator
import com.gabesechansoftware.laundrydemoserver.model.customerview.toCustomerFacing
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.AddressRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.OrganizationRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.repositories.UserRepository
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.Address
import com.gabesechansoftware.laundrydemoserver.model.dbview.user.User
import com.gabesechansoftware.laundrydemoserver.model.validation.AddressValidator
import com.gabesechansoftware.laundrydemoserver.model.validation.EmailValidator
import com.gabesechansoftware.laundrydemoserver.model.validation.PhoneValidator
import com.gabesechansoftware.laundrydemoserver.model.validation.validatePassword
import jakarta.transaction.Transactional
import com.gabesechansoftware.laundrydemoserver.model.customerview.Address as CustomerAddress
import com.gabesechansoftware.laundrydemoserver.model.customerview.User as CustomerUser
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val phoneValidator: PhoneValidator,
    private val emailValidator: EmailValidator,
    private val userRepository: UserRepository,
    private val loginAuthenticator: LoginAuthenticator,
    private val organizationRepository: OrganizationRepository,
    private val addressRepository: AddressRepository,
    private val addressValidator: AddressValidator = AddressValidator()
) {
    
    @Transactional
    fun createUser(user: CustomerUser, password: String, org: UUID): User {
        val errors = mutableListOf<String>()
        validatePassword(password, errors)
        validateUser(user, errors)
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



    private fun validateUser(user: CustomerUser, errors: MutableList<String>) {
        if(user.name.length < 2) {
            errors.add("Name too short")
        }
        phoneValidator.validatePhoneNumber(user.phone, errors)
        emailValidator.validateEmail(user.email!!, errors)
        user.addresses.forEach { address ->
            addressValidator.validateCustomerAddress(address, errors)
        }
    }


    fun addAddress(user: User, address: CustomerAddress): Address {
        val errors = mutableListOf<String>()
        addressValidator.validateCustomerAddress(address, errors)
        val hasOtherAddress = addressRepository.countAddressesByUser(user) > 0
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

    fun getUser(userId: UUID): CustomerUser{
        return userRepository.findById(userId).get().toCustomerFacing()
    }

    fun CustomerAddress.toAddress(user: User?, isDefault: Boolean): Address {
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