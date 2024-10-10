package com.shoppin_and_go.inventory_server.domain

import com.github.f4b6a3.ulid.UlidCreator
import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime
import java.util.*


@MappedSuperclass
abstract class BaseEntity {
    @Id
    @Column(columnDefinition = "BINARY(16)")
    val id: UUID = UlidCreator.getMonotonicUlid().toUuid()

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
        protected set

    @LastModifiedDate
    @Column(nullable = false)
    @Suppress("unused")
    var updatedAt: LocalDateTime = LocalDateTime.now()
        protected set
}