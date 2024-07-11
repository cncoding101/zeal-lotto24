package org.example.demoservice.customer

import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class CustomerNumberProvider {

    companion object {
        private const val NUMBER_LENGTH = 5
        private val MAX_CUSTOMER_NUMBER = "9".repeat(NUMBER_LENGTH).toLong()
    }

    fun nextCustomerNumber(): String {
        return Random.nextLong(1, MAX_CUSTOMER_NUMBER).toString().padStart(NUMBER_LENGTH, '0')
    }
}
