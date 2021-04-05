package io.helpdesk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.paging.PagingData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import io.helpdesk.R
import io.helpdesk.core.storage.BaseUserPersistentStorage
import io.helpdesk.model.data.Ticket
import io.helpdesk.model.data.TicketPriority
import io.helpdesk.model.data.UserAndTicket
import io.helpdesk.model.db.LocalDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class TicketsViewModel @Inject constructor(
    db: LocalDatabase,
    private val storage: BaseUserPersistentStorage,
    private val firestore: FirebaseFirestore,
) : ViewModel() {
    private val dao = db.ticketDao()
    private val userDao = db.userDao()

    // Backing property to avoid state updates from other classes
    private val _ticketsUIState = MutableStateFlow<LatestTicketUIState>(LatestTicketUIState.Loading)
    private val _postTicketUIState = MutableStateFlow<PostTicketUIState>(PostTicketUIState.Initial)

    // The UI collects from this StateFlow to get its state updates
    val ticketsUIState: StateFlow<LatestTicketUIState> = _ticketsUIState
    val postTicketUIState: StateFlow<PostTicketUIState> = _postTicketUIState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            // simulate 1.5s delay
            delay(1500L)

            if (!storage.loginState.value) {
                _ticketsUIState.emit(LatestTicketUIState.Error("please login to see your tickets"))
                return@launch
            }

            dao.allTickets(storage.userId!!).collectLatest { tickets ->
                // Update View with the latest tickets
                // Writes to the value property of MutableStateFlow,
                // adding a new element to the flow and updating all
                // of its collectors
                val pagingData = PagingData.from(tickets)
                _ticketsUIState.emit(LatestTicketUIState.Success(pagingData))
            }
        }
    }

    fun postNewTicket(
        title: String,
        comment: String = "",
        navController: NavController,
        technician: String? = null,
    ) =
        viewModelScope.launch(Dispatchers.IO) {
            // loading
            _postTicketUIState.emit(PostTicketUIState.Loading)

            // evaluate user auth state
            if (storage.loginState.value) {
                viewModelScope.launch(Dispatchers.IO) {

                    // get technicians
                    userDao.getTechnicians().collectLatest { technicians ->
                        if (technicians.isEmpty()) {
                            _postTicketUIState.emit(PostTicketUIState.Error("not technicians found"))
                            return@collectLatest
                        }

                        // set priority at random
                        val seed = Random.nextInt(2)
                        val ticketPriority = TicketPriority.values()[seed]

                        // create a new ticket
                        val ticket = Ticket(
                            id = UUID.randomUUID().toString(),
                            user = storage.userId!!,
                            name = title,
                            comment = comment,
                            priority = ticketPriority,
                            technician = technician ?: technicians.last().id,
                        )
                        dao.insert(ticket)
                        firestore.collection(Ticket.TABLE_NAME)
                            .document(ticket.id)
                            .set(ticket, SetOptions.merge())
                        withContext(Dispatchers.Main) {
                            _postTicketUIState.emit(PostTicketUIState.Success)
                            navController.popBackStack()
                        }
                    }
                }
            } else {
                _postTicketUIState.emit(PostTicketUIState.Error("you are not logged in yet"))
                launch(Dispatchers.Main) {
                    navController.navigate(R.id.nav_login)
                }
            }
        }
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