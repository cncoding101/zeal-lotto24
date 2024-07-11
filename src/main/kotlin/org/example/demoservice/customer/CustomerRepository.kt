package org.example.demoservice.customer

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerRepository : MongoRepository<Customer, String> {

    fun findByTenantIdAndCustomerNumber(tenantId: String, customerNumber: String): Customer?

    fun findAllByTenantId(tenantId: String): List<Customer>
}
