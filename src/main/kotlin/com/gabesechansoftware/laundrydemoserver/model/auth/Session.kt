package com.gabesechansoftware.laundrydemoserver.model.auth

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class Session {
    @Id
    var id: Long? = null

    /*
    id
    user id
    org id (denormalized for lookup)
    token  //unique
    expiration  //utc timestamp
    */
}