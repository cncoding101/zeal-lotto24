package org.example.demoservice.customer

import org.example.demoservice.sequence.SequenceGeneratorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class CustomerService @Autowired constructor(
    private val customerRepository: CustomerRepository,
    private val customerNumberProvider: CustomerNumberProvider,
    private val sequenceGenerator: SequenceGeneratorService
) {
    val tenantIdRegex = "^[a-zA-Z0-9_-]+$".toRegex()

    fun registerCustomer(
        tenantId: String,
        email: String,
        name: String?,
        surname: String?,
        phoneNumber: String?,
        address: Address?
    ): Customer {
        if (!tenantId.matches(tenantIdRegex)) {
            throw IllegalArgumentException("Invalid tenant ID format. Only alphanumeric characters, underscore, and hyphen are allowed.")
        }

        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        if (!email.matches(emailRegex)) {
            throw IllegalArgumentException("Invalid email format")
        }

        try {
            val sequence = sequenceGenerator.generateSequence("customers_sequence");
            val customerNumber = customerNumberProvider.nextCustomerNumber(sequence)
            val customer = Customer(
                tenantId = tenantId,
                customerNumber = customerNumber,
                email = email,
                name = name,
                surname = surname,
                phoneNumber = phoneNumber,
                address = address
            )
            return customerRepository.save(customer)
        } catch (ex: DuplicateKeyException) {
            throw CustomerRegistrationException("Failed to register customer due to email not being unique", ex)
        } catch (ex: Exception) {
            throw CustomerRegistrationException("Failed to register customer", ex)
        }
    }

    fun getCustomers(tenantId: String, pageNumber: Int, pageSize: Int): Page<Customer> {
        if (!tenantId.matches(tenantIdRegex)) {
            throw IllegalArgumentException("Invalid tenant ID format. Only alphanumeric characters, underscore, and hyphen are allowed.")
        }

        val pageable: Pageable = PageRequest.of(pageNumber, pageSize, Sort.by("customerNumber").ascending())
        val test = customerRepository.findAllByTenantId(tenantId, pageable);
        return test;
    }

    @Cacheable(value = ["customers"], key = "#tenantId + '_' + #customerNumber")
    fun getCustomer(tenantId: String, customerNumber: String): Customer {
        if (!tenantId.matches(tenantIdRegex)) {
            throw IllegalArgumentException("Invalid tenant ID format. Only alphanumeric characters, underscore, and hyphen are allowed.")
        }

        return customerRepository.findByTenantIdAndCustomerNumber(tenantId, customerNumber)
            ?: throw CustomerNotFoundException(tenantId, customerNumber)
    }
}
