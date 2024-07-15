package org.example.demoservice.api.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.demoservice.api.v1.model.ApiCustomer
import org.example.demoservice.api.v1.model.ApiCustomerList
import org.example.demoservice.api.v1.model.RegistrationRequest
import org.example.demoservice.api.v1.model.toApi
import org.example.demoservice.customer.CustomerService
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customer API")
class CustomerRestController(
    private val customerService: CustomerService,
) {

    @Operation(summary = "register a new customer on a tenant")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "the data of the newly registered customer", content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ApiCustomer::class))]
            )
        ]
    )
    @PostMapping("/{tenantId}")
    fun registerCustomer(
        @PathVariable tenantId: String,
        @RequestBody registationRequest: RegistrationRequest
    ): ApiCustomer {
        return customerService.registerCustomer(
            tenantId,
            registationRequest.email,
            registationRequest.name,
            registationRequest.surname,
            registationRequest.phoneNumber,
            registationRequest.address
        ).toApi()
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
    ): ApiCustomerList {
        return customerService.getCustomers(tenantId, pageNumber, pageSize).toApi()
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
    fun getCustomer(@PathVariable tenantId: String, @PathVariable customerNumber: String): ApiCustomer {
        return customerService.getCustomer(tenantId, customerNumber).toApi()
    }
}
