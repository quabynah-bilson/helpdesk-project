package io.helpdesk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.helpdesk.core.util.Result
import io.helpdesk.core.util.ioScope
import io.helpdesk.model.data.User
import io.helpdesk.model.data.UserType
import io.helpdesk.repository.BaseUserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val repository: BaseUserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<UserUIState>(UserUIState.Loading)
    val loadTechniciansState = _uiState.asStateFlow()

    fun saveUser(user: User?) = ioScope {
        if (user == null) return@ioScope
        repository.addUser(user).collectLatest { result ->
            when (result) {
                is Result.Initial, Result.Loading -> {
                    Timber.tag("save-user").i("saving user")
                }
                is Result.Error -> {
                    Timber.tag("save-user")
                        .i("failed to saved user: ${result.exception?.localizedMessage}")
                }
                is Result.Success -> {
                    Timber.tag("save-user").i("successfully saved user")
                }
            }
        }
    }

    fun loadUsers(userType: UserType = UserType.Technician) = ioScope {
        repository.usersByType(userType.ordinal).collectLatest { result ->
            when (result) {
                is Result.Success -> {
                    val users = result.data

                    if (users.isEmpty()) _uiState.emit(UserUIState.Error("no technicians found"))
                    else _uiState.emit(UserUIState.Success(users))
                }

                is Result.Error -> _uiState.emit(
                    UserUIState.Error(
                        result.exception?.localizedMessage ?: "no technicians found"
                    )
                )

                else -> _uiState.emit(UserUIState.Loading)
            }
        }
    }

    // @ExperimentalCoroutinesApi
    suspend fun currentUser(): Flow<User?> = channelFlow {
        repository.currentUser().collectLatest { result ->
            when (result) {
                is Result.Success -> {
                    Timber.tag("current-user").i("logged in as -> ${result.data}")
                    offer(result.data)
                }

                is Result.Error -> {
                    offer(null)
                }

                else -> {
                }
            }
        }
        awaitClose()
    }.stateIn(viewModelScope)

    fun getUserById(id: String): Flow<User?> = channelFlow {
        repository.getUserById(id).collectLatest { result ->
            when (result) {
                is Result.Success -> {
                    offer(result.data)
                }
                is Result.Error -> {
                    offer(null)
                }
                else -> {
                }
            }
        }
    }

    fun deleteUser(user: User?) = ioScope { if (user != null) repository.deleteUser(user) }
}

sealed class UserUIState {
    data class Success(val users: List<User>) : UserUIState()
    data class Error(val reason: String) : UserUIState()
    object Loading : UserUIState()
}