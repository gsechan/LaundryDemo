package com.gabesechansoftware.laundrydemoserver.model.validation

import com.gabesechansoftware.laundrydemoserver.model.dbview.Organization

class OrganizationValidator {

    fun validateOrganization(organization: Organization, errors: MutableList<String>) {
        val name = organization.name
        if(name == null) {
            errors.add("Name is required")
        }
        else if(name.length < 4) {
            errors.add("Name too short")
        }

        if(organization.defaultLocale == null) {
            errors.add("Default locale is required")
        }
    }
}
