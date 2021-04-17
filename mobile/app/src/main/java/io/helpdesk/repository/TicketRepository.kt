package io.helpdesk.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.helpdesk.core.storage.BaseUserPersistentStorage
import io.helpdesk.core.util.Result
import io.helpdesk.core.util.await
import io.helpdesk.core.util.fold
import io.helpdesk.core.util.foldDoc
import io.helpdesk.model.data.Ticket
import io.helpdesk.model.data.TicketPriority
import io.helpdesk.model.data.UserAndTicket
import io.helpdesk.model.data.UserType
import io.helpdesk.model.db.LocalDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

interface BaseTicketRepository {
    suspend fun postNewTicket(
        title: String,
        description: String,
    ): Flow<Result<Boolean>>

    suspend fun updateTicket(ticket: Ticket): Flow<Result<Boolean>>

    suspend fun getTicketById(id: String): Flow<Result<Ticket?>>

    suspend fun deleteTicket(ticket: Ticket): Flow<Result<Boolean>>

    suspend fun allTickets(): Flow<Result<List<UserAndTicket>>>
}

/**
 * Ticket repository implementation
 */
class TicketRepository @Inject constructor(
    private val scope: CoroutineScope,
    private val storage: BaseUserPersistentStorage,
    db: LocalDatabase,
    firestore: FirebaseFirestore,
) : BaseTicketRepository {
    private val dao = db.ticketDao()
    private val userDao = db.userDao()

    private val ticketCollection = firestore.collection(Ticket.TABLE_NAME)

    @ExperimentalCoroutinesApi
    override suspend fun postNewTicket(
        title: String,
        description: String,
    ): Flow<Result<Boolean>> = channelFlow {
        offer(Result.Loading)
        // evaluate user auth state
        if (storage.userId != null) {
            // get technicians
            userDao.getTechnicians().collectLatest { technicians ->
                if (technicians.isEmpty()) {
                    offer(Result.Error(Exception("no technicians found")))
                } else {
                    // set priority at random
                    val seed = Random.nextInt(2)
                    val ticketPriority = TicketPriority.values()[seed]

                    // create a new ticket
                    val ticket = Ticket(
                        id = UUID.randomUUID().toString(),
                        user = storage.userId!!,
                        name = title,
                        description = description,
                        priority = ticketPriority,
                        technician = technicians[Random.nextInt(technicians.size)].id,
                    )
                    launch(Dispatchers.IO) { dao.insert(ticket) }
                    ticketCollection
                        .document(ticket.id)
                        .set(ticket, SetOptions.merge())
                    offer(Result.Success(true))
                }
            }
        } else {
            offer(Result.Error(Exception("user not logged in")))
        }
        awaitClose()
    }.stateIn(scope)

    @ExperimentalCoroutinesApi
    override suspend fun updateTicket(ticket: Ticket): Flow<Result<Boolean>> = channelFlow {
        offer(Result.Loading)
        try {
            dao.insert(ticket)
            ticketCollection.document(ticket.id).set(ticket, SetOptions.merge()).await(scope)
            offer(Result.Success(true))
        } catch (e: Exception) {
            offer(Result.Error(e))
        }
        awaitClose()
    }.stateIn(scope)

    @ExperimentalCoroutinesApi
    override suspend fun deleteTicket(ticket: Ticket): Flow<Result<Boolean>> = channelFlow {
        offer(Result.Loading)
        try {
            val updatedTicket = ticket.copy(deleted = true)
            dao.update(updatedTicket)
            ticketCollection.document(ticket.id).set(updatedTicket, SetOptions.merge())
            offer(Result.Success(true))
        } catch (e: Exception) {
            offer(Result.Error(e))
        }
        awaitClose()
    }.stateIn(scope)

    @ExperimentalCoroutinesApi
    override suspend fun allTickets(): Flow<Result<List<UserAndTicket>>> = channelFlow {
        offer(Result.Loading)
        if (storage.userId == null) {
            offer(Result.Error(Exception("only logged in users can access this data")))
        } else {
            val ticketsQuery =
                if (storage.userType == UserType.SuperAdmin.ordinal) dao.getUsersAndTickets()
                else dao.allTickets(storage.userId!!)

            ticketsQuery.collectLatest { tickets ->
                // Update View with the latest tickets
                // Writes to the value property of MutableStateFlow,
                // adding a new element to the flow and updating all
                // of its collectors
                offer(Result.Success(tickets))
            }

            // fetch from server and update locally
            ticketCollection.get().fold<Ticket>(
                scope,
                { tickets ->
                    tickets.forEach { item ->
                        dao.insert(item)
                    }
                },
                { exception ->
                    offer(Result.Error(exception))
                },
            )
        }
        awaitClose()
    }.stateIn(scope)

    @ExperimentalCoroutinesApi
    override suspend fun getTicketById(id: String): Flow<Result<Ticket?>> = channelFlow {
        dao.getTicketById(id).collectLatest { ticket ->
            Timber.tag("get-ticket-by-id").i("found: $ticket")
            offer(Result.Success(ticket))
        }
        ticketCollection.document(id).get().foldDoc<Ticket>(
            scope,
            { ticket ->
                if (ticket != null) scope.launch { dao.insert(ticket) }
            },
            { exception ->
                Timber.tag("get-ticket-by-id")
                    .e("could not find ticket: ${exception?.localizedMessage}")
            })
        awaitClose()
    }.stateIn(scope)
}