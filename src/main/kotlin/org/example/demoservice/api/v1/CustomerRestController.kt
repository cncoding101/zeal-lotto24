package org.example.demoservice.api.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import mu.KLogging
import org.example.demoservice.api.v1.model.*
import org.example.demoservice.customer.CustomerNotFoundException
import org.example.demoservice.customer.CustomerRegistrationException
import org.example.demoservice.customer.CustomerService
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
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
        @PathVariable @Pattern(
            regexp = "^[a-zA-Z0-9_-]+$",
            message = "Invalid tenant ID format. Only alphanumeric characters, underscore, and hyphen are allowed."
        ) @Valid tenantId: String,
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
        @PathVariable @Pattern(
            regexp = "^[a-zA-Z0-9_-]+$",
            message = "Invalid tenant ID format. Only alphanumeric characters, underscore, and hyphen are allowed."
        ) @Valid tenantId: String,
        @PageableDefault(page = 0, size = 10, sort = ["customerNumber"]) pageable: Pageable
    ): ResponseEntity<ApiCustomerList> {
        logger.info("Fetching a list of customer for tenant $tenantId")
        return ResponseEntity.status(HttpStatus.OK)
            .body(customerService.getCustomers(tenantId, pageable).toApi())
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
    fun getCustomer(
        @PathVariable @Pattern(
            regexp = "^[a-zA-Z0-9_-]+$",
            message = "Invalid tenant ID format. Only alphanumeric characters, underscore, and hyphen are allowed."
        ) @Valid tenantId: String, @PathVariable customerNumber: String
    ): ResponseEntity<ApiCustomer> {
        logger.info("Fetching customer with customer id $customerNumber for tenant $tenantId")
        return ResponseEntity.status(HttpStatus.OK)
            .body(customerService.getCustomer(tenantId, customerNumber).toApi())
    }

    @ExceptionHandler(CustomerRegistrationException::class)
    fun handleCustomerRegistrationException(ex: CustomerRegistrationException): ResponseEntity<ErrorMessage> {
        logger.error("CustomerRegistrationException observed: ${ex.message}", ex)

        val errorMessage = ErrorMessage(
            HttpStatus.BAD_REQUEST.value(),
            ex.message
        )

        return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(CustomerNotFoundException::class)
    fun handleCustomerNotFoundException(ex: CustomerNotFoundException): ResponseEntity<ErrorMessage> {
        logger.error("CustomerNotFoundException observed: ${ex.message}", ex)

        val errorMessage = ErrorMessage(
            HttpStatus.NOT_FOUND.value(),
            ex.message
        )

        return ResponseEntity(errorMessage, HttpStatus.NOT_FOUND)
    }
}
