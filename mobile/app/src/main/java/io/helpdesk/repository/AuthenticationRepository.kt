package io.helpdesk.repository

import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import io.helpdesk.core.storage.BaseUserPersistentStorage
import io.helpdesk.core.util.*
import io.helpdesk.model.data.User
import io.helpdesk.model.data.UserType
import io.helpdesk.model.db.UserDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * base authentication repository
 */
interface BaseAuthenticationRepository {

    suspend fun login(email: String, password: String): Flow<Result<User>>

    suspend fun register(
        username: String,
        email: String,
        password: String,
        userType: UserType
    ): Flow<Result<User>>

    suspend fun logout(): Flow<Result<Unit>>

    val loginState: Flow<Boolean>
}

class AuthenticationRepository @Inject constructor(
    private val scope: CoroutineScope,
    private val userDao: UserDao, // local data source
    private val storage: BaseUserPersistentStorage,
    firestore: FirebaseFirestore,   // remote data source
    private val auth: FirebaseAuth,
    private val messaging: FirebaseMessaging,
) : BaseAuthenticationRepository {

    private val userCollection = firestore.collection(User.TABLE_NAME)

    /**
     * This method signs in a [UserType.Customer], [UserType.SuperAdmin] or a [UserType.Technician]
     */
    @ExperimentalCoroutinesApi
    override suspend fun login(email: String, password: String): Flow<Result<User>> =
        channelFlow {
            when {
                /**
                 * sends an error message to the view model for invalid email addresses
                 */
                !validateCredentials(email = email) -> trySend(Result.Error(Exception("invalid email address")))

                /**
                 * sends an error message to the view model for invalid password
                 */
                !validateCredentials(password = password) -> trySend(Result.Error(Exception("invalid password")))

                else -> {
                    trySend(Result.Loading)

                    auth.signInWithEmailAndPassword(email, password).awaitAuthResult(scope)
                        .collectLatest { firebaseUser ->
                            if (firebaseUser == null) {
                                logger.e("user credentials may be invalid")
                                trySend(Result.Error(Exception("no user record found")))
                            } else {
                                if (email == "admin@helpdesk.io") {
                                    userCollection.document(firebaseUser.uid).get()
                                        .foldDoc<User>(scope,
                                            { user ->
                                                if (user == null) {
                                                    trySend(Result.Error(Exception("Admin details not found")))
                                                } else {
                                                    // store user type
                                                    storage.userId = user.id
                                                    storage.userType = user.type.ordinal

                                                    // device token
                                                    messaging.token.addOnCompleteListener {
                                                        scope.launch {
                                                            if (it.isSuccessful) {
                                                                val token = it.result
                                                                logger.d("token is [$token]")
                                                                val updatedUser =
                                                                    user.copy(token = token)
                                                                userDao.insert(updatedUser)
                                                                userCollection.document(updatedUser.id)
                                                                    .set(
                                                                        updatedUser,
                                                                        SetOptions.merge()
                                                                    )
                                                                trySend(Result.Success(updatedUser))
                                                            }
                                                        }
                                                    }
                                                }
                                            }, { err ->
                                                scope.launch {
                                                    auth.signOut()
                                                    trySend(Result.Error(err))
                                                }
                                            })


                                }

                                // get user from database
                                userCollection.document(firebaseUser.uid).get().foldDoc<User>(scope,
                                    { user ->
                                        scope.launch {
                                            logger
                                                .i("user logged in with data -> $user")
                                            if (user == null) {
                                                trySend(Result.Error(Exception("no user found")))
                                            } else {

                                                // store user type
                                                storage.userId = user.id
                                                storage.userType = user.type.ordinal

                                                messaging.token.addOnCompleteListener {
                                                    scope.launch {
                                                        if (it.isSuccessful) {
                                                            val token = it.result
                                                            logger.d("token is [$token]")
                                                            val updatedUser =
                                                                user.copy(token = token)
                                                            userDao.insert(updatedUser)
                                                            userCollection.document(updatedUser.id)
                                                                .set(
                                                                    updatedUser,
                                                                    SetOptions.merge()
                                                                )
                                                            trySend(Result.Success(updatedUser))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    { err ->
                                        scope.launch {
                                            auth.signOut()
                                            trySend(Result.Error(err))
                                        }
                                    }
                                )
                            }
                        }
                }
            }

            awaitClose()
        }.stateIn(scope)

    /**
     * This method signs is used by a [UserType.SuperAdmin] to create a new  [UserType.Customer] or a [UserType.Technician]
     */
    @ExperimentalCoroutinesApi
    override suspend fun register(
        username: String,
        email: String,
        password: String,
        userType: UserType,
    ): Flow<Result<User>> =
        channelFlow {
            when {
                !validateCredentials(email = email) -> trySend(Result.Error(Exception("invalid email address")))

                !validateCredentials(password = password) -> trySend(Result.Error(Exception("invalid password")))

                else -> {
                    trySend(Result.Loading)

                    auth.createUserWithEmailAndPassword(email, password).awaitAuthResult(scope)
                        .collectLatest { firebaseUser ->
                            if (firebaseUser == null) {
                                trySend(Result.Error(Exception("user already exists or there is an internet connection issue")))
                            } else {
                                val user =
                                    User(
                                        id = firebaseUser.uid,
                                        email = email,
                                        name = username,
                                        type = userType,
                                    )
                                /**
                                 * stores user details remotely on firebase
                                 */
                                userCollection.document(user.id).set(user, SetOptions.merge())
                                    .await(scope)

                                /**
                                 * stores user details locally using sql
                                 */
                                userDao.insert(user)


                                trySend(Result.Success(user))
                            }
                        }
                }
            }
            awaitClose()
        }.stateIn(scope)

    override suspend fun logout(): Flow<Result<Unit>> = flow {
        logger.i("logging out")
        storage.clear()
        emit(Result.Initial)
    }.stateIn(scope)

    override val loginState: Flow<Boolean>
        get() = storage.loginState

    /**
     * This method validates user authentication credentials (i.e. email & password)
     */
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