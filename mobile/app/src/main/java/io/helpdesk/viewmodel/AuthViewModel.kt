package io.helpdesk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.helpdesk.core.util.Result
import io.helpdesk.core.util.ioScope
import io.helpdesk.model.data.User
import io.helpdesk.model.data.UserType
import io.helpdesk.repository.BaseAuthenticationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Works with the [BaseAuthenticationRepository] to handle user authentication
 */
@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepository: BaseAuthenticationRepository) :
    ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    private val _userTypeState = MutableStateFlow(UserType.Customer)

    val authState: StateFlow<AuthState> get() = _authState
    val loginState: StateFlow<Boolean> get() = authRepository.loginState
    val userTypeState: StateFlow<UserType> get() = _userTypeState

    // set user type
    fun updateUserType(type: Int) = viewModelScope.launch {
        authRepository.updateUserType(type).collectLatest {
            _userTypeState.emit(UserType.values()[type])
        }
    }

    // login with email & password
    fun login(email: String?, password: String?) = ioScope {
        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            _authState.emit(AuthState.Error("cannot validate fields"))
        } else {
            _authState.emit(AuthState.Loading)
            authRepository.login(email, password).collectLatest { result ->
                when (result) {
                    is Result.Loading, Result.Initial -> _authState.emit(AuthState.Loading)
                    is Result.Error -> _authState.emit(AuthState.Error(result.toString()))
                    is Result.Success -> _authState.emit(AuthState.Success(result.data))
                }
            }
        }
    }

    // register with username, email & password
    fun register(username: String?, email: String?, password: String?) = ioScope {
        _authState.emit(AuthState.Loading)
        if (email.isNullOrEmpty() || password.isNullOrEmpty() || username.isNullOrEmpty()) {
            _authState.emit(AuthState.Error("cannot validate fields"))
        } else {
            _authState.emit(AuthState.Loading)

            authRepository.register(username, email, password).collectLatest { result ->
                when (result) {
                    is Result.Loading, Result.Initial -> _authState.emit(AuthState.Loading)
                    is Result.Error -> _authState.emit(AuthState.Error(result.toString()))
                    is Result.Success -> _authState.emit(AuthState.Success(result.data))
                }
            }
        }
    }

    // sign out
    fun logout() = authRepository.logout()
}

sealed class AuthState {
    data class Success(val user: User) : AuthState()
    data class Error(val reason: String) : AuthState()
    object Loading : AuthState()
    object Initial : AuthState()
}