package org.example.demoservice

import org.bson.Document
import org.example.demoservice.api.v1.model.ApiCustomer
import org.example.demoservice.api.v1.model.ApiCustomerList
import org.example.demoservice.api.v1.model.ErrorMessage
import org.example.demoservice.api.v1.model.RegistrationRequest
import org.example.demoservice.testconfig.MongoDBTestContainerConfig
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@ContextConfiguration(
    classes = [MongoDBTestContainerConfig::class]
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class CustomerRestControllerIntegrationTest {

    private val logger = LoggerFactory.getLogger(CustomerRestControllerIntegrationTest::class.java)

    @Autowired
    private lateinit var mongoOperations: MongoOperations

    @Autowired
    lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setUp() {
        mongoOperations.collectionNames.forEach {
            mongoOperations.getCollection(it).deleteMany(Document())
        }
    }

    @Test
    fun registerCustomer_getCustomer() {
        val customer = webTestClient.post()
            .uri("/api/v1/customers/{tenantId}", "test-tenant")
            .bodyValue(RegistrationRequest("email@example.com"))
            .exchange()
            .expectStatus().isCreated
            .expectBody(ApiCustomer::class.java)
            .returnResult()
            .responseBody!!

        val foundCustomer = webTestClient.get()
            .uri("/api/v1/customers/{tenantId}/{customerNumber}", "test-tenant", customer.customerNumber)
            .exchange()
            .expectStatus().isOk
            .expectBody(ApiCustomer::class.java)
            .returnResult()
            .responseBody!!

        Assertions.assertEquals(customer, foundCustomer)
        Assertions.assertEquals("email@example.com", foundCustomer.email)
    }

    @Test
    fun getCustomers_listForTenant() {
        val customer1 = webTestClient.post()
            .uri("/api/v1/customers/{tenantId}", "test-tenant")
            .bodyValue(RegistrationRequest("email1@example.com"))
            .exchange()
            .expectStatus().isCreated
            .expectBody(ApiCustomer::class.java)
            .returnResult()
            .responseBody!!


        val customer2 = webTestClient.post()
            .uri("/api/v1/customers/{tenantId}", "test-tenant")
            .bodyValue(RegistrationRequest("email2@example.com"))
            .exchange()
            .expectStatus().isCreated
            .expectBody(ApiCustomer::class.java)
            .returnResult()
            .responseBody!!


        val payload = webTestClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/api/v1/customers/{tenantId}")
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .build("test-tenant")
            }
            .exchange()
            .expectStatus().isOk
            .expectBody(ApiCustomerList::class.java)
            .returnResult()
            .responseBody!!

        Assertions.assertEquals(2, payload.customers.size)
        Assertions.assertTrue(payload.customers.contains(customer1))
        Assertions.assertTrue(payload.customers.contains(customer2))
    }

    @Test
    fun getCustomers_listForNonExistentTenant() {
        val payload = webTestClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/api/v1/customers/{tenantId}")
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .build("non-existent-test-tenant")
            }
            .exchange()
            .expectStatus().isOk
            .expectBody(ApiCustomerList::class.java)
            .returnResult()
            .responseBody!!

        Assertions.assertTrue(payload.customers.isEmpty())
    }

    @Test
    fun getCustomer_withNonExistentTenantId() {
        val customer = webTestClient.post()
            .uri("/api/v1/customers/{tenantId}", "test-tenant")
            .bodyValue(RegistrationRequest("email@example.com"))
            .exchange()
            .expectStatus().isCreated
            .expectBody(ApiCustomer::class.java)
            .returnResult()
            .responseBody!!


        val error = webTestClient.get()
            .uri("/api/v1/customers/{tenantId}/{customerNumber}", "non-existent-test-tenant", customer.customerNumber)
            .exchange()
            .expectStatus().isNotFound
            .expectBody(ErrorMessage::class.java)
            .returnResult()
            .responseBody!!

        Assertions.assertTrue(error.message!!.contains("Customer ${customer.customerNumber} not found in tenant non-existent-test-tenant"))
    }

    @Test
    fun registerCustomer_withInvalidEmailFormat() {
        val error = webTestClient.post()
            .uri("/api/v1/customers/{tenantId}", "test-tenant")
            .bodyValue(RegistrationRequest("invalid-email"))
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(ErrorMessage::class.java)
            .returnResult()
            .responseBody!!

        Assertions.assertTrue(error.message!!.contains("Invalid email format"))
    }

    @Test
    fun registerCustomer_withBlankEmail() {
        val error = webTestClient.post()
            .uri("/api/v1/customers/{tenantId}", "test-tenant")
            .bodyValue(RegistrationRequest(""))
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(ErrorMessage::class.java)
            .returnResult()
            .responseBody!!

        Assertions.assertTrue(error.message!!.contains("Email must not be blank"))
    }

    @Test
    fun getCustomer_withSpecialCharactersInTenantId() {
        val specialCharTenantId = "tenant!@#$"

        val error = webTestClient.get()
            .uri("/api/v1/customers/{tenantId}/{customerNumber}", specialCharTenantId, "some-customer-id")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(ErrorMessage::class.java)
            .returnResult()
            .responseBody!!

        Assertions.assertTrue(error.message!!.contains("Invalid tenant ID format. Only alphanumeric characters, underscore, and hyphen are allowed."))
    }

    @Test
    fun registerCustomer_withExistingEmail() {
       webTestClient.post()
            .uri("/api/v1/customers/{tenantId}", "test-tenant")
            .bodyValue(RegistrationRequest("unique@example.com"))
            .exchange()
            .expectStatus().isCreated

        val error = webTestClient.post()
            .uri("/api/v1/customers/{tenantId}", "test-tenant")
            .bodyValue(RegistrationRequest("unique@example.com"))
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(ErrorMessage::class.java)
            .returnResult()
            .responseBody!!

        Assertions.assertTrue(error.message!!.contains("Failed to register customer due to email not being unique"))
    }
}
