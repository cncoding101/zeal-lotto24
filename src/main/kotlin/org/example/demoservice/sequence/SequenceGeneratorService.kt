package org.example.demoservice.sequence

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service

@Service
class SequenceGeneratorService @Autowired constructor(
    private val mongoOperations: MongoOperations
) {

    fun generateSequence(seqName: String, inc: Long = 1): Long {
        val counter = mongoOperations.findAndModify(
            Query(Criteria.where("_id").`is`(seqName)),
            Update().inc("seq", inc),
            FindAndModifyOptions().returnNew(true).upsert(true),
            DatabaseSequence::class.java
        )
        return counter?.seq ?: inc
    }
}
