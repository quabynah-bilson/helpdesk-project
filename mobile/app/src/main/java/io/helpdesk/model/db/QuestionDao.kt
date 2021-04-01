package io.helpdesk.model.db

import androidx.room.Dao
import androidx.room.Query
import io.helpdesk.model.data.Question

@Dao
interface QuestionDao : BaseDao<Question> {
    @Query("select * from faqs order by priority asc")
    suspend fun faqs(): List<Question>
}