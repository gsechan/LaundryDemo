package com.gabesechansoftware.laundrydemoserver.services

import com.gabesechansoftware.laundrydemoserver.model.Organization
import com.gabesechansoftware.laundrydemoserver.repositories.OrganizationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class OrganizationService(@Autowired private val orgRepo: OrganizationRepository) {

}