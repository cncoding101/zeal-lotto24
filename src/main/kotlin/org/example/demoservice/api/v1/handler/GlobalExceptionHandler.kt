package org.example.demoservice.api.v1.handler

import mu.KLogging
import org.example.demoservice.api.v1.model.ErrorMessage
import org.springframework.dao.DuplicateKeyException
import org.example.demoservice.customer.CustomerNotFoundException
import org.example.demoservice.customer.CustomerRegistrationException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.stereotype.Component
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@Component
@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    companion object : KLogging()

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        logger.error("MethodArgumentNotValidException observed: ${ex.message}", ex)
        val errors = ex.bindingResult.allErrors
            .map { error -> error.defaultMessage!! }
            .sorted()

        val errorMessage = ErrorMessage(
            HttpStatus.BAD_REQUEST.value(),
            errors.joinToString(", ") { it }
        )

        return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
    }

    override fun handleHttpMediaTypeNotSupported(
        ex: HttpMediaTypeNotSupportedException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        logger.error("HttpMediaTypeNotSupportedException observed: ${ex.message}", ex)

        val errorMessage = ErrorMessage(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
            ex.message ?: "Unsupported Media Type"
        )

        return ResponseEntity(errorMessage, HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        logger.error("HttpMessageNotReadableException observed: ${ex.message}", ex)

        val errorMessage = ErrorMessage(
            HttpStatus.BAD_REQUEST.value(),
            ex.message ?: "Bad Request"
        )

        return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
    }


    @ExceptionHandler
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<ErrorMessage> {
        logger.error("IllegalStateException observed: ${ex.message}", ex)

        val errorMessage = ErrorMessage(
            HttpStatus.NOT_FOUND.value(),
            ex.message
        )

        return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
    }


    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException, request: WebRequest): ResponseEntity<Any> {
        logger.error("IllegalArgumentException observed: ${ex.message}", ex)

        val errorMessage = ErrorMessage(
            HttpStatus.BAD_REQUEST.value(),
            ex.message ?: "Bad request"
        )

        return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
    }


    @ExceptionHandler(DuplicateKeyException::class)
    fun handleDuplicateKeyException(ex: DuplicateKeyException): ResponseEntity<ErrorMessage> {
        logger.error("DuplicateKeyException observed: ${ex.message}", ex)

        val errorMessage = ErrorMessage(
            HttpStatus.BAD_REQUEST.value(),
            "Duplicate key: ${ex.message}"
        )

        return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
    }
}