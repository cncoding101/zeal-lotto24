package org.example.demoservice.customer

import org.springframework.stereotype.Service

@Service
class CustomerService(
    private val customerRepository: CustomerRepository,
    private val customerNumberProvider: CustomerNumberProvider,
) {

    fun registerCustomer(tenantId: String, email: String): Customer {
        val customerNumber = customerNumberProvider.nextCustomerNumber()
        val customer = Customer(tenantId = tenantId, customerNumber = customerNumber, email = email)
        return customerRepository.save(customer)
    }

    fun getCustomers(tenantId: String): List<Customer> {
        return customerRepository.findAllByTenantId(tenantId)
    }

    fun getCustomer(tenantId: String, customerNumber: String): Customer {
        return customerRepository.findByTenantIdAndCustomerNumber(tenantId, customerNumber)
            ?: throw CustomerNotFoundException(tenantId, customerNumber)
    }
}
