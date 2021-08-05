package io.helpdesk.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.helpdesk.core.util.Result
import io.helpdesk.core.util.ioScope
import io.helpdesk.model.data.User
import io.helpdesk.model.data.UserType
import io.helpdesk.repository.BaseAuthenticationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

/**
 * Works with the [BaseAuthenticationRepository] to handle user authentication
 */
@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepository: BaseAuthenticationRepository) :
    ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)

    val authState = _authState.asStateFlow()
    val loginState = authRepository.loginState

    // login with email & password
    fun login(email: String?, password: String?) = ioScope {
        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            _authState.emit(AuthState.Error("cannot validate fields"))
        } else {
            authRepository.login(email, password).collectLatest { result ->
                when (result) {
                    is Result.Loading, Result.Initial -> _authState.emit(AuthState.Loading)
                    is Result.Error -> _authState.emit(AuthState.Error(result.exception?.localizedMessage.toString()))
                    is Result.Success -> _authState.emit(AuthState.Success(result.data))
                }
            }
        }
    }

    // register with username, email & password
    fun register(username: String?, email: String?, password: String?, userType: UserType) =
        ioScope {
            _authState.emit(AuthState.Loading)
            if (email.isNullOrEmpty() || password.isNullOrEmpty() || username.isNullOrEmpty()) {
                _authState.emit(AuthState.Error("cannot validate fields"))
            } else {
                authRepository.register(username, email, password, userType)
                    .collectLatest { result ->
                        when (result) {
                            is Result.Loading, Result.Initial -> _authState.emit(AuthState.Loading)
                            is Result.Error -> _authState.emit(AuthState.Error(result.exception?.localizedMessage.toString()))
                            is Result.Success -> _authState.emit(AuthState.Success(result.data))
                        }
                    }
            }
        }

    // sign out
    fun logout() = ioScope { authRepository.logout() }
}

sealed class AuthState {
    data class Success(val user: User) : AuthState()
    data class Error(val reason: String) : AuthState()
    object Loading : AuthState()
    object Initial : AuthState()
}