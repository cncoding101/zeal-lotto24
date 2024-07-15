package org.example.demoservice.customer

class CustomerRegistrationException(message: String, cause: Throwable) :
    RuntimeException(message, cause)