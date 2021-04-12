package io.helpdesk.repository

import android.util.Patterns
import com.google.firebase.firestore.FirebaseFirestore
import io.helpdesk.core.storage.BaseUserPersistentStorage
import io.helpdesk.core.util.Result
import io.helpdesk.core.util.await
import io.helpdesk.core.util.fold
import io.helpdesk.model.data.User
import io.helpdesk.model.db.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * base authentication repository
 */
interface BaseAuthenticationRepository {

    fun login(email: String, password: String): Flow<Result<User>>

    fun register(username: String, email: String, password: String): Flow<Result<User>>

    fun logout(): Flow<Result<Unit>>

    fun updateUserType(type: Int): Flow<Result<Unit>>

    val loginState: StateFlow<Boolean>
}

class AuthenticationRepository @Inject constructor(
    private val userDao: UserDao,
    private val storage: BaseUserPersistentStorage,
    firestore: FirebaseFirestore,
) :
    BaseAuthenticationRepository {

    private val userCollection = firestore.collection(User.TABLE_NAME)

    @ExperimentalCoroutinesApi
    override fun login(email: String, password: String): Flow<Result<User>> = channelFlow {
        when {
            !validateCredentials(email = email) -> offer(Result.Error(Exception("invalid email address")))

            !validateCredentials(password = password) -> offer(Result.Error(Exception("invalid password")))

            else -> {
                offer(Result.Loading)

                // get user from database
                userCollection.whereEqualTo("email", email).get().fold<User>(
                    { users ->
                        // get user by email
                        val userByUsername = users.firstOrNull { user -> user.email == email }

                        if (userByUsername == null) offer(Result.Error(Exception("no user found")))
                        else {
                            storage.userId = userByUsername.id
                            storage.userType = userByUsername.type.ordinal

                            launch(Dispatchers.IO) { userDao.insert(userByUsername) }

                            offer(Result.Success(userByUsername))
                        }
                    },
                    { err ->
                        offer(Result.Error(err))
                    }
                )
            }
        }
    }


    @ExperimentalCoroutinesApi
    override fun register(username: String, email: String, password: String): Flow<Result<User>> =
        channelFlow {
            when {
                !validateCredentials(email = email) -> offer(Result.Error(Exception("invalid email address")))

                !validateCredentials(password = password) -> offer(Result.Error(Exception("invalid password")))

                else -> {
                    offer(Result.Loading)

                    val user =
                        User(id = UUID.randomUUID().toString(), email = email, name = username)
                    userCollection.document(user.id).set(user).await()
                    storage.userType = user.type.ordinal
                    storage.userId = user.id
                    launch(Dispatchers.IO) { userDao.insert(user) }
                    offer(Result.Success(user))
                }
            }
        }

    @ExperimentalCoroutinesApi
    override fun logout(): Flow<Result<Unit>> = channelFlow {
        storage.clear()
        offer(Result.Initial)
    }

    @ExperimentalCoroutinesApi
    override fun updateUserType(type: Int): Flow<Result<Unit>> = channelFlow {
        storage.userType = type
        offer(Result.Initial)
    }

    override val loginState: StateFlow<Boolean>
        get() = storage.loginState

    private fun validateCredentials(email: String? = null, password: String? = null): Boolean =
        when {
            email != null -> {
                Patterns.EMAIL_ADDRESS.matcher(email).matches()
            }
            password != null -> {
                password.length >= 4
            }
            else -> false
        }
}