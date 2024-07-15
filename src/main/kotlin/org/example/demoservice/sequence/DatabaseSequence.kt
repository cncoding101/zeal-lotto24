package org.example.demoservice.sequence

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "database_sequences")
@TypeAlias("database_sequence")
data class DatabaseSequence(
    @Id
    val id: String,
    val seq: Long,
)
