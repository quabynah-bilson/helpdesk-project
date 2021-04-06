package io.helpdesk.model.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import io.helpdesk.model.data.User
import io.helpdesk.model.data.UserAndTicket
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao : BaseDao<User> {
    @Query("select * from users where userType = :userType order by _id desc")
    fun allUsers(userType: Int = 1): Flow<List<User>>

    @Query("select * from users where userType = 0 order by _id desc")
    fun getTechnicians(): Flow<List<User>>

    @Query("select * from users where userType = :type and _id = :id")
    suspend fun getUserByIdAndType(id: String, type: Int) : User?

    @Query("select * from users where username like :username")
    suspend fun getUserByUsername(username: String) : User?
}