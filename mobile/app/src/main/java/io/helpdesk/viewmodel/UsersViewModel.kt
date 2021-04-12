package io.helpdesk.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.helpdesk.core.util.Result
import io.helpdesk.core.util.ioScope
import io.helpdesk.model.data.User
import io.helpdesk.model.data.UserType
import io.helpdesk.repository.BaseUserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val repository: BaseUserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<UserUIState>(UserUIState.Loading)
    val uiState: StateFlow<UserUIState> get() = _uiState

    init {

        ioScope {
            repository.usersByType(UserType.Technician.ordinal).collectLatest { result ->
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
    }

    @ExperimentalCoroutinesApi
    fun currentUser(): Flow<User?> = channelFlow {
        repository.currentUser().collectLatest { result ->
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

    @ExperimentalCoroutinesApi
    fun getTechnician(id: String): Flow<User?> = channelFlow {
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
}

sealed class UserUIState {
    data class Success(val users: List<User>) : UserUIState()
    data class Error(val reason: String) : UserUIState()
    object Loading : UserUIState()
}