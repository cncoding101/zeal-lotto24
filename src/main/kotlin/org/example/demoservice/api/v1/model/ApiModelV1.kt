package org.example.demoservice.api.v1.model

import org.example.demoservice.customer.Customer

data class ApiCustomerList(
    val customers: List<ApiCustomer>,
)

data class ApiCustomer(
    val customerNumber: String,
    val email: String,
)

data class RegistrationRequest(
    val email: String,
)

fun Customer.toApi() = ApiCustomer(
    customerNumber = customerNumber,
    email = email,
)

fun List<Customer>.toApi() = ApiCustomerList(
    customers = map {
        ApiCustomer(
            customerNumber = it.customerNumber,
            email = it.email,
        )
    }
)
