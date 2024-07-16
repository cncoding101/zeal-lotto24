package org.example.demoservice.customer

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "customers")
@TypeAlias("customer")
@CompoundIndex(name = "tenant_customerNumber", def = "{'tenantId': 1, 'customerNumber': 1}")
data class Customer(
    @Id
    val id: String? = null,
    @Indexed
    val tenantId: String,
    @Indexed
    val customerNumber: String,
    @Indexed(unique = true)
    val email: String,
//    optional
    val name: String? = null,
    val surname: String? = null,
    val phoneNumber: String? = null,
    val address: Address? = null,
) {
    companion object {
        //        prevents persistent data
        @Transient
        const val SEQUENCE_NAME: String = "customers_sequence"
    }
}
