package io.helpdesk.repository

import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.helpdesk.core.storage.BaseUserPersistentStorage
import io.helpdesk.core.util.*
import io.helpdesk.model.data.User
import io.helpdesk.model.data.UserType
import io.helpdesk.model.db.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * base authentication repository
 */
interface BaseAuthenticationRepository {

    fun login(email: String, password: String): Flow<Result<User>>

    fun register(
        username: String,
        email: String,
        password: String,
        userType: UserType
    ): Flow<Result<User>>

    fun logout(): Flow<Result<Unit>>

    val loginState: StateFlow<Boolean>
}

class AuthenticationRepository @Inject constructor(
    private val userDao: UserDao,
    private val storage: BaseUserPersistentStorage,
    firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : BaseAuthenticationRepository {

    private val userCollection = firestore.collection(User.TABLE_NAME)

    @ExperimentalCoroutinesApi
    override fun login(email: String, password: String): Flow<Result<User>> = channelFlow {
        when {
            !validateCredentials(email = email) -> offer(Result.Error(Exception("invalid email address")))

            !validateCredentials(password = password) -> offer(Result.Error(Exception("invalid password")))

            else -> {
                offer(Result.Loading)

                val firebaseUser =
                    auth.signInWithEmailAndPassword(email, password).awaitAuthResult(this)
                if (firebaseUser == null) {
                    logger.e("user credentials may be invalid")
                    offer(Result.Error(Exception("no user found")))
                } else {
                    // get user from database
                    userCollection.document(firebaseUser.uid).get().foldDoc<User>(this,
                        { user ->
                            logger
                                .i("user logged in with data -> $user")
                            if (user == null) {
                                offer(Result.Error(Exception("no user found")))
                            } else {

                                // todo -> store locally
//                                storage.userId = userByUsername.id
//                                storage.userType = userByUsername.type.ordinal

                                launch(Dispatchers.IO) { userDao.insert(user) }

                                offer(Result.Success(user))
                            }
                        },
                        { err ->
                            offer(Result.Error(err))
                        }
                    )
                }
            }
        }
    }


    @ExperimentalCoroutinesApi
    override fun register(
        username: String,
        email: String,
        password: String,
        userType: UserType,
    ): Flow<Result<User>> =
        channelFlow {
            when {
                !validateCredentials(email = email) -> offer(Result.Error(Exception("invalid email address")))

                !validateCredentials(password = password) -> offer(Result.Error(Exception("invalid password")))

                else -> {
                    offer(Result.Loading)

                    val firebaseUser =
                        auth.createUserWithEmailAndPassword(email, password).awaitAuthResult(this)

                    if (firebaseUser == null) {
                        offer(Result.Error(Exception("user already exists or there is an internet connection issue")))
                    } else {
                        val user =
                            User(
                                id = firebaseUser.uid,
                                email = email,
                                name = username,
                                type = userType,
                            )
                        userCollection.document(user.id).set(user).await()
                        userDao.insert(user)
                        offer(Result.Success(user))
                    }
                }

            }
        }

    override fun logout(): Flow<Result<Unit>> = flow {
        logger.i("logging out")
        storage.clear()
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