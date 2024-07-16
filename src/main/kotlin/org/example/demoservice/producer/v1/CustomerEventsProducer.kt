package org.example.demoservice.producer.v1

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import org.example.demoservice.customer.CustomerEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class CustomerEventsProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    @Value("\${spring.kafka.topic}")
    private val topic: String
) {

    companion object : KLogging()

    fun sendCustomerEvent(customerEvent: CustomerEvent): CompletableFuture<SendResult<String, String>> {
        val key = customerEvent.customerEventId
        val value = objectMapper.writeValueAsString(customerEvent)

        // 1. blocking call to get metadata from cluster (happens only once)
        // 2. sends message and returns completableFuture
        val completableFuture = kafkaTemplate.send(topic, key, value)
        return completableFuture
            .whenComplete { sendResult, throwable ->
                if (throwable != null) {
                    handleFailure(throwable)
                } else {
                    handleSuccess(key, value, sendResult)
                }
            }
    }

    private fun handleFailure(ex: Throwable) {
        logger.error("Error Sending the Message and the exception is ${ex.message}", ex)
    }

    private fun handleSuccess(key: String, value: String, result: SendResult<String, String>) {
        logger.info(
            "Message Sent SuccessFully for the key : $key and the value is $value , partition is $result",
            key,
            value,
            result.recordMetadata.partition()
        )
    }
}