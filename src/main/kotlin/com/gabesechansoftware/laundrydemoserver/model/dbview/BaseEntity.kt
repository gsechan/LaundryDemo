package com.gabesechansoftware.laundrydemoserver.model.dbview

import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.hibernate.Hibernate
import java.util.UUID

@MappedSuperclass
abstract class BaseEntity(
    @Id
    var id: UUID = UUID.randomUUID()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as BaseEntity
        return id == other.id
    }

    override fun hashCode() = id.hashCode()
}