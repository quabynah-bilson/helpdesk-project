package io.helpdesk.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.helpdesk.core.storage.BaseUserPersistentStorage
import io.helpdesk.core.util.*
import io.helpdesk.model.data.*
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
    private val firestore: FirebaseFirestore,
) : BaseTicketRepository {
    private val dao = db.ticketDao()
    private val userDao = db.userDao()

    private val ticketCollection = firestore.collection(Ticket.TABLE_NAME)

    @ExperimentalCoroutinesApi
    override suspend fun postNewTicket(
        title: String,
        description: String,
    ): Flow<Result<Boolean>> = channelFlow {
        trySend(Result.Loading)

        // evaluate user auth state
        if (storage.userId != null) {
            firestore.collection(User.TABLE_NAME).get()
                .fold<User>(scope, { users ->
                    userDao.insertAll(users)
                    val technicians = mutableListOf<User>()
                    users.forEach {
                        if (it.type == UserType.Technician) {
                            technicians.add(it)
                        }
                    }
                    if (technicians.isEmpty()) {
                        trySend(Result.Error(Exception("no technicians found")))
                    } else {
                        // set priority at random
                        val ticketPriority = TicketPriority.values()[Random.nextInt(2)]

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
                        trySend(Result.Success(true))
                    }
                }, { exception ->
                    trySend(Result.Error(exception))
                })
        } else {
            trySend(Result.Error(Exception("user not logged in")))
        }
        awaitClose()
    }.stateIn(scope)

    @ExperimentalCoroutinesApi
    override suspend fun updateTicket(ticket: Ticket): Flow<Result<Boolean>> = channelFlow {
        trySend(Result.Loading)
        try {
            dao.insert(ticket)
            ticketCollection.document(ticket.id).set(ticket, SetOptions.merge()).await(scope)
            trySend(Result.Success(true))
        } catch (e: Exception) {
            trySend(Result.Error(e))
        }
        awaitClose()
    }.stateIn(scope)

    @ExperimentalCoroutinesApi
    override suspend fun deleteTicket(ticket: Ticket): Flow<Result<Boolean>> = channelFlow {
        trySend(Result.Loading)
        try {
            val updatedTicket = ticket.copy(deleted = true)
            dao.update(updatedTicket)
            ticketCollection.document(ticket.id).set(updatedTicket, SetOptions.merge())
            trySend(Result.Success(true))
        } catch (e: Exception) {
            trySend(Result.Error(e))
        }
        awaitClose()
    }.stateIn(scope)

    @ExperimentalCoroutinesApi
    override suspend fun allTickets(): Flow<Result<List<UserAndTicket>>> = channelFlow {
        trySend(Result.Loading)
        if (storage.userId == null) {
            trySend(Result.Error(Exception("only logged in users can access this data")))
        } else {
            val ticketsQuery =
                when (storage.userType) {
                    UserType.Customer.ordinal -> dao.allTicketsForCustomer(storage.userId!!)
                    UserType.Technician.ordinal -> dao.allTicketsForTechnician(storage.userId!!)
                    else -> dao.getUsersAndTickets()
                }

            // fetch from server and update locally
            ticketCollection.observeCollection<Ticket>(
                scope,
                { results ->
                    results.forEach { item ->
                        Timber.tag("tickets").d(item.toString())
                        dao.insert(item)
                    }

                    ticketsQuery.collectLatest { tickets ->
                        // Update View with the latest tickets
                        // Writes to the value property of MutableStateFlow,
                        // adding a new element to the flow and updating all
                        // of its collectors
                        trySend(Result.Success(tickets))
                    }
                },
                { exception ->
                    trySend(Result.Error(exception))
                },
            )
        }
        awaitClose()
    }.stateIn(scope)

    @ExperimentalCoroutinesApi
    override suspend fun getTicketById(id: String): Flow<Result<Ticket?>> = channelFlow {
        dao.getTicketById(id).collectLatest { ticket ->
            Timber.tag("get-ticket-by-id").i("found: $ticket")
            trySend(Result.Success(ticket))
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