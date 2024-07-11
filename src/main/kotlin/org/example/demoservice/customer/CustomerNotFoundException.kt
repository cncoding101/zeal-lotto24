package org.example.demoservice.customer

@Suppress("serial")
class CustomerNotFoundException(
    tenantId: String,
    customerNumber: String,
) : RuntimeException("customer $customerNumber not found in tenant $tenantId")
