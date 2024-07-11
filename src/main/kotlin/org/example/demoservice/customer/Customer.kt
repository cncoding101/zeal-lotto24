package org.example.demoservice.customer

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "customers")
@TypeAlias("customer")
data class Customer(
    @Id
    val id: String? = null,
    val tenantId: String,
    val customerNumber: String,
    val email: String,
)
