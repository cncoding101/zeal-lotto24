package org.example.demoservice

import org.bson.Document
import org.example.demoservice.api.v1.CustomerRestController
import org.example.demoservice.api.v1.model.RegistrationRequest
import org.example.demoservice.customer.Customer
import org.example.demoservice.testconfig.MongoDBTestContainerConfig
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Query
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@ContextConfiguration(
    classes = [MongoDBTestContainerConfig::class]
)
@SpringBootTest(
    classes = [DemoServiceApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
class DemoServiceApplicationTests {

    private val logger = LoggerFactory.getLogger(DemoServiceApplicationTests::class.java)

    @Autowired
    private lateinit var mongoOperations: MongoOperations

    @Autowired
    private lateinit var customerRestController: CustomerRestController

    @BeforeEach
    fun setUp() {
        mongoOperations.collectionNames.forEach {
            mongoOperations.getCollection(it).deleteMany(Document())
        }
    }

    @Test
    fun registerAndFindCustomer() {
        val customer = customerRestController.registerCustomer(
            "test-tenant",
            RegistrationRequest("email@example.com")
        )
        val foundCustomer = customerRestController.getCustomer("test-tenant", customer.customerNumber)

        Assertions.assertEquals(customer, foundCustomer)
        Assertions.assertEquals("email@example.com", foundCustomer.email)
    }

    @Test
    fun getCustomerListForTenant() {
        val customer1 = customerRestController.registerCustomer(
            "test-tenant",
            RegistrationRequest("customer1@example.com")
        )
        val customer2 = customerRestController.registerCustomer(
            "test-tenant",
            RegistrationRequest("customer2@example.com")
        )

        val payload = customerRestController.getCustomers("test-tenant", 0, 10)

        Assertions.assertEquals(2, payload.customers.size)
        Assertions.assertTrue(payload.customers.contains(customer1))
        Assertions.assertTrue(payload.customers.contains(customer2))
    }

    private fun logDatabaseState() {
        val customers = mongoOperations.find(Query(), Customer::class.java)
        logger.info("Current customers in the database: $customers")
    }

    @Test
    fun getCustomerListForNonExistentTenant() {
        val payload = customerRestController.getCustomers("non-existent-test-tenant", 0, 10)
        Assertions.assertTrue(payload.customers.isEmpty())
    }

    @Test
    fun createAndFetchCustomer() {
        val registrationRequest = RegistrationRequest("newcustomer@example.com")
        val newCustomer = customerRestController.registerCustomer("test-tenant", registrationRequest)
        val fetchedCustomer = customerRestController.getCustomer("test-tenant", newCustomer.customerNumber)

        Assertions.assertNotNull(fetchedCustomer)
        Assertions.assertEquals(newCustomer.customerNumber, fetchedCustomer.customerNumber)
        Assertions.assertEquals(registrationRequest.email, fetchedCustomer.email)
    }

    @Test
    fun fetchCustomerWithNonExistentTenantId() {
        val exception = assertThrows<Exception> {
            customerRestController.getCustomer("non-existent-test-tenant", "some-customer-id")
        }
        Assertions.assertTrue(exception.message!!.contains("Customer some-customer-id not found in tenant non-existent-test-tenant"))
    }

    @Test
    fun fetchCustomerWithNonExistentCustomerId() {
        val exception = assertThrows<Exception> {
            customerRestController.getCustomer("test-tenant", "non-existent-customer-id")
        }
        Assertions.assertTrue(exception.message!!.contains("Customer non-existent-customer-id not found in tenant test-tenant"))
    }

    @Test
    fun registerCustomerWithInvalidEmailFormat() {
        val exception = assertThrows<Exception> {
            customerRestController.registerCustomer("test-tenant", RegistrationRequest("invalid-email"))
        }
        Assertions.assertTrue(exception.cause!!.message!!.contains("Invalid email format"))
    }

    @Test
    fun registerCustomerWithBlankEmail() {
        val exception = assertThrows<Exception> {
            customerRestController.registerCustomer("test-tenant", RegistrationRequest(""))
        }
        Assertions.assertTrue(exception.cause!!.message!!.contains("Email must not be blank"))
    }

    @Test
    fun fetchCustomerWithSpecialCharactersInTenantId() {
        val specialCharTenantId = "tenant!@#$"
        val exception = assertThrows<Exception> {
            customerRestController.getCustomer(specialCharTenantId, "some-customer-id")
        }
        Assertions.assertTrue(exception.message!!.contains("Invalid tenant ID format. Only alphanumeric characters, underscore, and hyphen are allowed."))
    }

    @Test
    fun registerCustomerWithExistingEmail() {
        customerRestController.registerCustomer(
            "test-tenant",
            RegistrationRequest("unique@example.com")
        )

        val exception = assertThrows<Exception> {
            customerRestController.registerCustomer("test-tenant", RegistrationRequest("unique@example.com"))
        }
        Assertions.assertTrue(exception.message!!.contains("Failed to register customer due to email not being unique"))
    }
}
