package io.helpdesk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.helpdesk.core.storage.BaseUserPersistentStorage
import io.helpdesk.model.data.User
import io.helpdesk.model.db.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
                else _uiState.emit(UserUIState.Success(PagingData.from(users)))
            }
        }
    }

    fun currentUser(): Flow<User?> = flow {
        if (storage.loginState.value) {
            viewModelScope.launch(Dispatchers.IO) {
                val user =
                    dao.getUserByIdAndType(id = storage.userId!!, type = storage.userType)
                emit(user)
            }
        }
    }
}

sealed class UserUIState {
    data class Success(val users: PagingData<User>) : UserUIState()
    data class Error(val reason: String) : UserUIState()
    object Loading : UserUIState()
}