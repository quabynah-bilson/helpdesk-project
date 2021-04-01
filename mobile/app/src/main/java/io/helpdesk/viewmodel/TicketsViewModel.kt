package io.helpdesk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.helpdesk.model.data.Ticket
import io.helpdesk.model.db.TicketDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TicketsViewModel @Inject constructor(private val dao: TicketDao) : ViewModel() {
    // Backing property to avoid state updates from other classes
    private val _uiState = MutableStateFlow<LatestTicketUIState>(LatestTicketUIState.Loading)

    // The UI collects from this StateFlow to get its state updates
    val uiState: StateFlow<LatestTicketUIState> = _uiState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            dao.allTickets().collectLatest { tickets ->
                // Update View with the latest tickets
                // Writes to the value property of MutableStateFlow,
                // adding a new element to the flow and updating all
                // of its collectors
                _uiState.emit(LatestTicketUIState.Success(tickets))
            }
        }
    }

}

sealed class LatestTicketUIState {
    data class Success(val tickets: List<Ticket>) : LatestTicketUIState()
    data class Error(val reason: String) : LatestTicketUIState()
    object Loading : LatestTicketUIState()
}