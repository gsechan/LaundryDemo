package com.gabesechansoftware.laundrydemoserver.admins

data class AdminView(
    val id: String,
    val name: String?,
    val email: String?,
    val phone: String?,
    // All permissions the admin holds across every role they belong to. Used by
    // the front end to gate UI elements.
    val permissions: List<String>,
    // The admin's current role memberships (role id + the membership row id, so
    // the front end can unassign without a separate lookup).
    val roleMemberships: List<RoleAssignmentView>,
)

data class RoleAssignmentView(
    val membershipId: String,
    val roleId: String,
)
