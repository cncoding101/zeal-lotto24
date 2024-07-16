package org.example.demoservice.customer

data class CustomerEvent(
    val customerEventId: String,
    val customerEventType: CustomerEventType,
    val customer: Customer
)
