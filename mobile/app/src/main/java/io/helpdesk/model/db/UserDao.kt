package io.helpdesk.model.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import io.helpdesk.model.data.User
import io.helpdesk.model.data.UserAndTicket
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao : BaseDao<User> {
    @Query("select * from users order by _id desc")
    fun allUsers(): Flow<List<User>>

    @Transaction
    @Query("select * from users")
    fun getUsersAndTickets(): Flow<List<UserAndTicket>>
}