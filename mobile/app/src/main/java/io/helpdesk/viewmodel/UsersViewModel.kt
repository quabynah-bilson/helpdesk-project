package io.helpdesk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.helpdesk.core.storage.BaseUserPersistentStorage
import io.helpdesk.model.data.User
import io.helpdesk.model.data.UserType
import io.helpdesk.model.db.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val storage: BaseUserPersistentStorage,
    private val dao: UserDao
) : ViewModel() {
    private val _uiState = MutableStateFlow<UserUIState>(UserUIState.Loading)
    val uiState: StateFlow<UserUIState> get() = _uiState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.emit(UserUIState.Loading)
            dao.getTechnicians().collectLatest { users ->
                if (users.isEmpty()) _uiState.emit(UserUIState.Error("no technicians found"))
                else _uiState.emit(UserUIState.Success(users))
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun currentUser(): Flow<User?> = channelFlow {
        if (storage.loginState.value) {
            val user = dao.getUserByIdAndType(id = storage.userId!!, type = storage.userType)
            Timber.tag("vm user").i("logged in as -> $user")
            launch(Dispatchers.Main) { offer(user) }
        }
    }

    @ExperimentalCoroutinesApi
    fun getTechnician(id: String): Flow<User?> = channelFlow {
        val userByIdAndType = dao.getUserByIdAndType(id, UserType.Technician.ordinal)
        withContext(Dispatchers.Main) {
            offer(userByIdAndType)
        }
    }
}

sealed class UserUIState {
    data class Success(val users: List<User>) : UserUIState()
    data class Error(val reason: String) : UserUIState()
    object Loading : UserUIState()
}