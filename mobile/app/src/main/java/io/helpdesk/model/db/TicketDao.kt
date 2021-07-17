package io.helpdesk.model.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import io.helpdesk.model.data.Ticket
import io.helpdesk.model.data.UserAndTicket
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao : BaseDao<Ticket> {

    @Transaction
    @Query("select * from tickets where user = :user and not deleted order by priority desc")
    fun allTicketsForCustomer(user: String): Flow<List<UserAndTicket>>

    @Transaction
    @Query("select * from tickets where technician = :technician and not deleted order by priority desc")
    fun allTicketsForTechnician(technician: String): Flow<List<UserAndTicket>>

    @Transaction
    @Query("select * from tickets where not deleted order by priority desc")
    fun getUsersAndTickets(): Flow<List<UserAndTicket>>

    @Transaction
    @Query("select * from tickets where _id = :id and not deleted")
    fun getTicketById(id: String): Flow<Ticket?>

}