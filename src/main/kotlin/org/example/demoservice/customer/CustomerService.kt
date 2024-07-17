package org.example.demoservice.customer

import org.example.demoservice.producer.v1.CustomerEventsProducer
import org.example.demoservice.sequence.SequenceGeneratorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.*

@Service
class CustomerService @Autowired constructor(
    private val customerRepository: CustomerRepository,
    private val customerNumberProvider: CustomerNumberProvider,
    private val sequenceGenerator: SequenceGeneratorService,
    private val customerEventsProducer: CustomerEventsProducer
) {

    fun registerCustomer(
        tenantId: String,
        email: String,
        name: String?,
        surname: String?,
        phoneNumber: String?,
        address: Address?
    ): Customer {
        try {
            val sequence = sequenceGenerator.generateSequence("customers_sequence")
            val customerNumber = customerNumberProvider.formatCustomerNumber(sequence)
            val customer = Customer(
                tenantId = tenantId,
                customerNumber = customerNumber,
                email = email,
                name = name,
                surname = surname,
                phoneNumber = phoneNumber,
                address = address
            )

            // produce a message event to inform others of a registration being made
            customerEventsProducer.sendCustomerEvent(
                CustomerEvent(
                    UUID.randomUUID().toString(),
                    CustomerEventType.NEW,
                    customer
                )
            )

            return customerRepository.save(customer)
        } catch (ex: DuplicateKeyException) {
            throw CustomerRegistrationException("Failed to register customer due to email not being unique", ex)
        } catch (ex: Exception) {
            throw CustomerRegistrationException("Failed to register customer", ex)
        }
    }

    fun getCustomers(tenantId: String, pageable: Pageable): Page<Customer> {
        val test = customerRepository.findAllByTenantId(tenantId, pageable)
        return test
    }

    @Cacheable(value = ["customers"], key = "#tenantId + '_' + #customerNumber")
    fun getCustomer(tenantId: String, customerNumber: String): Customer {
        return customerRepository.findByTenantIdAndCustomerNumber(tenantId, customerNumber)
            ?: throw CustomerNotFoundException(tenantId, customerNumber)
    }
}
