package org.example.demoservice.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaConfiguration(
    @Value("\${spring.kafka.topic}")
    private val topic: String
) {

    @Bean
    fun customerEvents(): NewTopic {
        return TopicBuilder.name(topic).partitions(1).replicas(1).build()
    }
}