package io.helpdesk.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.helpdesk.core.storage.BaseUserPersistentStorage
import io.helpdesk.core.util.*
import io.helpdesk.model.data.User
import io.helpdesk.model.data.UserType
import io.helpdesk.model.db.UserDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

interface BaseUserRepository {
    fun updateUser(user: User): Flow<Result<Unit>>

    fun currentUser(): Flow<Result<User?>>

    fun addUser(user: User): Flow<Result<Unit>>

    fun getUserById(id: String): Flow<Result<User>>

    fun allUsers(): Flow<Result<List<User>>>

    fun usersByType(type: Int): Flow<Result<List<User>>>

    suspend fun deleteUser(user: User)
}

class UserRepository @Inject constructor(
    private val scope: CoroutineScope,
    private val dao: UserDao,
    private val storage: BaseUserPersistentStorage,
    firestore: FirebaseFirestore,
) : BaseUserRepository {

    private val userCollection = firestore.collection(User.TABLE_NAME)

    @ExperimentalCoroutinesApi
    override fun updateUser(user: User): Flow<Result<Unit>> = channelFlow {
        launch(Dispatchers.IO) { dao.update(user) }
        userCollection.document(user.id).set(user, SetOptions.merge()).await(scope)
    }

    @ExperimentalCoroutinesApi
    override fun currentUser(): Flow<Result<User?>> = channelFlow {
        offer(Result.Loading)
        if (storage.userId == null) {
            offer(Result.Error(Exception("no user found")))
        } else {
            dao.getUserByIdAndType(id = storage.userId!!, type = storage.userType)
                .collectLatest { currentUser ->
                    offer(Result.Success(currentUser))
                }

            userCollection.document(storage.userId!!).observe<User>(this) { user, exception ->
                if (user != null) {
                    launch(Dispatchers.IO) { dao.insert(user) }
                }

                if (exception != null) {
                    offer(Result.Error(exception))
                }
            }
        }
        awaitClose()
    }

    @ExperimentalCoroutinesApi
    override fun addUser(user: User): Flow<Result<Unit>> = channelFlow {
        offer(Result.Loading)
        launch(Dispatchers.IO) { dao.insert(user) }
        userCollection.document(user.id).set(user, SetOptions.merge()).await(scope)
        offer(Result.Initial)
    }

    @ExperimentalCoroutinesApi
    override fun allUsers(): Flow<Result<List<User>>> = channelFlow {
        offer(Result.Loading)
        dao.allUsers().collectLatest { users ->
            offer(Result.Success(users))
        }

        userCollection.get()
            .fold<User>(this, { users ->
                users.forEach { launch(Dispatchers.IO) { dao.insert(it) } }
            }, { exception -> offer(Result.Error(exception)) })

        awaitClose()
    }

    @ExperimentalCoroutinesApi
    override fun usersByType(type: Int): Flow<Result<List<User>>> = channelFlow {
        offer(Result.Loading)

        dao.allUsers().collectLatest { users ->
            val filteredList = when (type) {
                UserType.All.ordinal, UserType.SuperAdmin.ordinal -> users
                else -> users.filter { person -> person.type == UserType.values()[type] }
            }
            offer(Result.Success(filteredList))
        }

        userCollection.whereEqualTo("type", type).get()
            .fold<User>(this, { users ->
                launch(Dispatchers.IO) { users.forEach { dao.insert(it) } }
            }, { exception -> offer(Result.Error(exception)) })

        awaitClose()
    }

    @ExperimentalCoroutinesApi
    override fun getUserById(id: String): Flow<Result<User>> = channelFlow {
        offer(Result.Loading)
        dao.getUserById(id).collectLatest { user ->
            if (user == null) offer(Result.Error(Exception("no user found")))
            else offer(Result.Success(user))
        }

        userCollection.document(id).get()
            .foldDoc<User>(this, { user ->
                if (user == null) {
                    offer(Result.Error(Exception("no user found")))
                } else {
                    launch(Dispatchers.IO) { dao.insert(user) }
                }
            }, { exception -> offer(Result.Error(exception)) })

        awaitClose()
    }

    override suspend fun deleteUser(user: User) {
        scope.launch {
            dao.delete(user)
            userCollection.document(user.id).delete().await(scope)
        }
    }
}