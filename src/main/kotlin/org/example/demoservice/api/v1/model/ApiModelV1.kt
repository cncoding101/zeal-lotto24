package org.example.demoservice.api.v1.model

import org.example.demoservice.customer.Address
import org.example.demoservice.customer.Customer
import org.springframework.data.domain.Page

data class ApiCustomerList(
    val customers: List<ApiCustomer>,
    val pageNumber: Int,
    val pageSize: Int
)

data class ApiCustomer(
    val customerNumber: String,
    val email: String,
)

data class RegistrationRequest(
    val email: String,
    val name: String? = null,
    val surname: String? = null,
    val phoneNumber: String? = null,
    val address: Address? = null,
)

fun Customer.toApi() = ApiCustomer(
    customerNumber = customerNumber,
    email = email,
)

fun Page<Customer>.toApi() = ApiCustomerList(
    customers = this.content.map {
        ApiCustomer(
            customerNumber = it.customerNumber,
            email = it.email,
        )
    },
    pageNumber = this.pageable.pageNumber,
    pageSize = this.pageable.pageSize
)
