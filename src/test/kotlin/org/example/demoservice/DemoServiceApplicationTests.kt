package org.example.demoservice

import org.bson.Document
import org.example.demoservice.api.v1.CustomerRestController
import org.example.demoservice.api.v1.model.RegistrationRequest
import org.example.demoservice.testconfig.MongoDBTestContainerConfig
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoOperations
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
        val customer = customerRestController.registerCustomer("test-tenant", RegistrationRequest("email@example.com"))
        val foundCustomer = customerRestController.getCustomer("test-tenant", customer.customerNumber)

        Assertions.assertEquals(customer, foundCustomer)
        Assertions.assertEquals("email@example.com", foundCustomer.email)
    }
}
