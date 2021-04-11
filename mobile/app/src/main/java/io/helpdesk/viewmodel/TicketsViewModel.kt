package io.helpdesk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.paging.PagingData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.helpdesk.core.util.Result
import io.helpdesk.model.data.Ticket
import io.helpdesk.model.data.UserAndTicket
import io.helpdesk.repository.BaseTicketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TicketsViewModel @Inject constructor(private val repository: BaseTicketRepository) :
    ViewModel() {

    // Backing property to avoid state updates from other classes
    private val _ticketsUIState = MutableStateFlow<LatestTicketUIState>(LatestTicketUIState.Loading)
    private val _postTicketUIState = MutableStateFlow<PostTicketUIState>(PostTicketUIState.Initial)

    // The UI collects from this StateFlow to get its state updates
    val ticketsUIState: StateFlow<LatestTicketUIState> = _ticketsUIState
    val postTicketUIState: StateFlow<PostTicketUIState> = _postTicketUIState

    init {
        viewModelScope.launch {
            repository.allTickets().collectLatest { result ->
                when (result) {
                    is Result.Error -> _ticketsUIState.emit(LatestTicketUIState.Error(result.toString()))

                    is Result.Success -> _ticketsUIState.emit(
                        LatestTicketUIState.Success(
                            PagingData.from(
                                result.data
                            )
                        )
                    )
                    is Result.Loading -> _ticketsUIState.emit(LatestTicketUIState.Loading)
                }
            }
        }
    }

    fun postNewTicket(
        title: String,
        description: String = "",
        navController: NavController,
    ) = viewModelScope.launch(Dispatchers.IO) {
        repository.postNewTicket(title, description).collectLatest { result ->
            when (result) {
                is Result.Loading -> _postTicketUIState.emit(PostTicketUIState.Loading)
                is Result.Error -> _postTicketUIState.emit(PostTicketUIState.Error(result.toString()))
                is Result.Success -> {
                    _postTicketUIState.emit(PostTicketUIState.Success)
                    navController.popBackStack()
                }
            }
        }
    }

    fun updateTicket(ticket: Ticket) =
        viewModelScope.launch(Dispatchers.IO) { repository.updateTicket(ticket) }

    fun deleteTicket(ticket: Ticket) =
        viewModelScope.launch(Dispatchers.IO) { repository.deleteTicket(ticket) }
}

sealed class LatestTicketUIState {
    data class Success(val tickets: PagingData<UserAndTicket>) : LatestTicketUIState()
    data class Error(val reason: String) : LatestTicketUIState()
    object Loading : LatestTicketUIState()
}

sealed class PostTicketUIState {
    object Success : PostTicketUIState()
    data class Error(val reason: String) : PostTicketUIState()
    object Loading : PostTicketUIState()
    object Initial : PostTicketUIState()
}