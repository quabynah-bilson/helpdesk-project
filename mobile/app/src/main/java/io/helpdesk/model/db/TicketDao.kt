package io.helpdesk.model.db

import androidx.room.Dao
import androidx.room.Query
import io.helpdesk.model.data.Ticket
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao : BaseDao<Ticket> {

    @Query("select * from tickets where user = :user order by dueDate desc")
    fun allTickets(user: String): Flow<List<Ticket>>
}