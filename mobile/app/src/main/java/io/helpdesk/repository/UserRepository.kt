package io.helpdesk.repository

import com.google.firebase.firestore.FirebaseFirestore
import io.helpdesk.core.storage.BaseUserPersistentStorage
import io.helpdesk.core.util.Result
import io.helpdesk.model.data.User
import io.helpdesk.model.db.UserDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import javax.inject.Inject

interface BaseUserRepository {
    fun updateUser(user: User): Flow<Result<Unit>>

    fun currentUser(): Flow<Result<User?>>

    fun addUser(user: User): Flow<Result<Unit>>
}


class UserRepository @Inject constructor(
    private val dao: UserDao,
    private val storage: BaseUserPersistentStorage,
    firestore: FirebaseFirestore,
) : BaseUserRepository {

    @ExperimentalCoroutinesApi
    override fun updateUser(user: User): Flow<Result<Unit>>  = channelFlow {  }

    @ExperimentalCoroutinesApi
    override fun currentUser(): Flow<Result<User?>> = channelFlow {  }

    @ExperimentalCoroutinesApi
    override fun addUser(user: User): Flow<Result<Unit>> = channelFlow {  }
}