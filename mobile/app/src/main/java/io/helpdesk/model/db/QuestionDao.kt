package io.helpdesk.model.db

import androidx.room.Dao
import androidx.room.Query
import io.helpdesk.model.data.Question
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao : BaseDao<Question> {
    @Query("select * from faqs order by priority desc")
    fun faqs(): Flow<List<Question>>
}