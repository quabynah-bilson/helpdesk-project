package io.helpdesk.repository

import android.util.Patterns
import com.google.firebase.firestore.FirebaseFirestore
import io.helpdesk.core.storage.BaseUserPersistentStorage
import io.helpdesk.core.util.Result
import io.helpdesk.core.util.await
import io.helpdesk.core.util.fold
import io.helpdesk.core.util.globalScope
import io.helpdesk.model.data.User
import io.helpdesk.model.db.UserDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber
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

                globalScope {
                    // get user from database
                    userCollection.whereEqualTo("email", email).get().fold<User>(
                        { users ->
                            Timber.tag("login-result").i("users -> $users")
                            // get user by email
                            val userByUsername = users.firstOrNull { user -> user.email == email }

                            if (userByUsername == null) {
                                launch {
                                    awaitClose {
                                        offer(Result.Error(Exception("no user found")))
                                    }
                                }
                            } else {
                                storage.userId = userByUsername.id
                                storage.userType = userByUsername.type.ordinal

                                userDao.insert(userByUsername)

                                launch {
                                    awaitClose {
                                        offer(Result.Success(userByUsername))
                                    }
                                }
                            }
                        },
                        { err ->
                            launch {
                                awaitClose {
                                    offer(Result.Error(err))
                                }
                            }
                        }
                    )
                }


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
                    userDao.insert(user)
                    launch {
                        awaitClose { offer(Result.Success(user)) }
                    }

                }
            }
        }

    override fun logout(): Flow<Result<Unit>> = flow {
        println("logging out")
        storage.clear()
        emit(Result.Initial)
    }

    @ExperimentalCoroutinesApi
    override fun updateUserType(type: Int): Flow<Result<Unit>> = flow {
        storage.userType = type
        emit(Result.Initial)
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