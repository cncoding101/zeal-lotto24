package org.example.demoservice.api.v1.model

//import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
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
    @get:NotBlank(message = "Email must not be blank")
//    @Email(message = "Invalid email format", regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")
    val email: String,
    val name: String? = null,
    val surname: String? = null,
    val phoneNumber: String? = null,
    val address: Address? = null,
)

data class ErrorMessage(
    var status: Int? = null,
    var message: String? = null
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
