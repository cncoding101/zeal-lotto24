package org.example.demoservice.api.v1

import mu.KLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.example.demoservice.api.v1.model.ApiCustomer
import org.example.demoservice.api.v1.model.ApiCustomerList
import org.example.demoservice.api.v1.model.RegistrationRequest
import org.example.demoservice.api.v1.model.toApi
import org.example.demoservice.customer.CustomerService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customer API")
@Validated
class CustomerRestController(
    private val customerService: CustomerService,
) {

    companion object : KLogging()

    @Operation(summary = "register a new customer on a tenant")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "the data of the newly registered customer", content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ApiCustomer::class))]
            )
        ]
    )
    @PostMapping("/{tenantId}")
    fun registerCustomer(
        @PathVariable tenantId: String,
        @RequestBody @Valid registationRequest: RegistrationRequest
    ): ResponseEntity<ApiCustomer> {
        logger.info("Registration of new customer to tenant $tenantId with payload: $registationRequest")
        return ResponseEntity.status(HttpStatus.CREATED).body(
            customerService.registerCustomer(
                tenantId,
                registationRequest.email,
                registationRequest.name,
                registationRequest.surname,
                registationRequest.phoneNumber,
                registationRequest.address
            ).toApi()
        )
    }

    @Operation(summary = "get all registered customers of a tenant")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "A list of all registered customers of the specified tenant",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ApiCustomerList::class))]
            )
        ]
    )
    @GetMapping("/{tenantId}")
    fun getCustomers(
        @PathVariable tenantId: String,
        @RequestParam(defaultValue = "0", name = "pageNumber") pageNumber: Int,
        @RequestParam(defaultValue = "10", name = "pageSize") pageSize: Int
    ): ResponseEntity<ApiCustomerList> {
        logger.info("Fetching a list of customer for tenant $tenantId")
        return ResponseEntity.status(HttpStatus.OK)
            .body(customerService.getCustomers(tenantId, pageNumber, pageSize).toApi())
    }

    @Operation(summary = "get a specific customer of a tenant")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "the customer data", content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ApiCustomer::class))]
            )
        ]
    )
    @GetMapping("/{tenantId}/{customerNumber}")
    fun getCustomer(@PathVariable tenantId: String, @PathVariable customerNumber: String): ResponseEntity<ApiCustomer> {
        logger.info("Fetching customer with customer id $customerNumber for tenant $tenantId")
        return ResponseEntity.status(HttpStatus.OK)
            .body(customerService.getCustomer(tenantId, customerNumber).toApi())
    }
}
