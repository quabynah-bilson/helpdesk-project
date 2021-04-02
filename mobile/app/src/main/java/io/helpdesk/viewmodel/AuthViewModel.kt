package io.helpdesk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import dagger.hilt.android.lifecycle.HiltViewModel
import io.helpdesk.core.storage.BaseUserPersistentStorage
import io.helpdesk.model.data.User
import io.helpdesk.model.data.UserType
import io.helpdesk.model.db.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userDao: UserDao,
    private val storage: BaseUserPersistentStorage,
    private val firestore: FirebaseFirestore,
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    private val _userTypeState = MutableStateFlow(UserType.Customer)

    val authState: StateFlow<AuthState> get() = _authState
    val userTypeState: StateFlow<UserType> get() = _userTypeState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            storage.loginState.collectLatest { loggedIn ->
                if (loggedIn) {
                    val user = userDao.getUserByIdAndType(
                        id = storage.userId!!,
                        type = _userTypeState.value.ordinal,
                    )
                    _authState.emit(AuthState.Success(user))
                }
            }
        }
    }

    // set user type
    fun updateUserType(type: UserType) = viewModelScope.launch {
        _userTypeState.emit(type)
    }

    // login with email & password
    fun login(email: String?, password: String?) = viewModelScope.launch {
        _authState.emit(AuthState.Loading)
        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            _authState.emit(AuthState.Error("cannot validate fields"))
            return@launch
        }
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                val firebaseUser = task.result?.user
                if (firebaseUser == null) {
                    launch(Dispatchers.IO) {
                        _authState.emit(AuthState.Error("failed to sign in user"))
                    }
                } else {
                    var user: User
                    firestore.collection(User.TABLE_NAME)
                        .document(firebaseUser.uid)
                        .get(Source.SERVER)
                        .addOnCompleteListener { userTask ->
                            if (userTask.isSuccessful && userTask.result != null) {
                                user = userTask.result!!.toObject(User::class.java) ?: User(
                                    id = firebaseUser.uid,
                                    email = firebaseUser.email!!,
                                    avatar = firebaseUser.photoUrl?.toString(),
                                    type = _userTypeState.value,
                                    name = firebaseUser.displayName ?: "No name"
                                )

                                launch(Dispatchers.IO) {
                                    // save locally
                                    userDao.insert(user)

                                    storage.userId = firebaseUser.uid
                                    storage.userType = _userTypeState.value.ordinal
                                    _authState.emit(AuthState.Success(user))

                                }
                            }
                        }
                }

            } else {
                launch(Dispatchers.IO) {
                    _authState.emit(
                        AuthState.Error(
                            task.exception?.localizedMessage ?: "failed to sign in user"
                        )
                    )
                }
            }
        }
    }

    // register with username, email & password
    fun register(username: String, email: String, password: String) = viewModelScope.launch {
        _authState.emit(AuthState.Loading)
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                launch(Dispatchers.IO) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser == null) {
                        _authState.emit(AuthState.Error("cannot create new user account"))
                    } else {
                        val user = User(
                            id = firebaseUser.uid, email = firebaseUser.email!!,
                            avatar = firebaseUser.photoUrl?.toString(), type = _userTypeState.value,
                            name = firebaseUser.displayName ?: username
                        )

                        // save locally
                        userDao.insert(user)

                        // save remotely
                        firestore.collection(User.TABLE_NAME).document(user.id)
                            .set(user, SetOptions.merge())


                        storage.userId = firebaseUser.uid
                        storage.userType = _userTypeState.value.ordinal
                        _authState.emit(AuthState.Success(user))
                    }
                }

            } else {
                launch(Dispatchers.IO) {
                    _authState.emit(
                        AuthState.Error(
                            task.exception?.localizedMessage ?: "failed to sign in user"
                        )
                    )
                }
            }
        }
    }


}

sealed class AuthState {
    data class Success(val user: User) : AuthState()
    data class Error(val reason: String) : AuthState()
    object Loading : AuthState()
    object Initial : AuthState()
}