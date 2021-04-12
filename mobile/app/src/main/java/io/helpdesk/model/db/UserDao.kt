package io.helpdesk.model.db

import androidx.room.Dao
import androidx.room.Query
import io.helpdesk.model.data.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao : BaseDao<User> {
    @Query("select * from users order by _id desc")
    fun allUsers(): Flow<List<User>>

    @Query("select * from users where userType = 0 order by _id desc")
    fun getTechnicians(): Flow<List<User>>

    @Query("select * from users where userType = :type and _id = :id")
    fun getUserByIdAndType(id: String, type: Int): Flow<User?>

    @Query("select * from users where username like :username")
    fun getUserByUsername(username: String): Flow<User?>

    @Query("select * from users where _id = :id")
    fun getUserById(id: String): Flow<User?>
}