package org.example.demoservice.customer

@Suppress("serial")
class CustomerNotFoundException(
    tenantId: String,
    customerNumber: String,
) : RuntimeException("Customer $customerNumber not found in tenant $tenantId")
