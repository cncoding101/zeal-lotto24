package org.example.demoservice.customer

import org.springframework.stereotype.Component

@Component
class CustomerNumberProvider {
    companion object {
        private const val NUMBER_LENGTH = 5
    }

    fun formatCustomerNumber(sequence: Long): String {
        return String.format("%0${NUMBER_LENGTH}d", sequence)
    }
}

