package org.example.demoservice.error

data class ErrorMessage(
    var status: Int? = null,
    var message: String? = null
)