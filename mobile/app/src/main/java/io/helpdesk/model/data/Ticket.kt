package io.helpdesk.model.data

import android.os.Parcelable
import androidx.room.*
import kotlinx.parcelize.Parcelize

enum class TicketCompletionState {
    Pending,
    Done,
    Cancelled,
}

enum class TicketType {
    Question,
}

enum class TicketPriority {
    Low, Medium, High,
}

/**
 * ticket data model
 */
@Parcelize
@Entity(tableName = Ticket.TABLE_NAME)
data class Ticket(
    @PrimaryKey
    @ColumnInfo(name = "_id")
    val id: String,
    val user: String,
    val name: String,
    var technician: String,
    var description: String = "no descriptions",
    var comment: String = "no comments",
    var status: TicketCompletionState = TicketCompletionState.Pending,
    var type: TicketType = TicketType.Question,
    val linkedTickets: List<String> = emptyList(),
    var priority: TicketPriority = TicketPriority.Medium,
    @ColumnInfo(name = "createdAt")
    val timestamp: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis() + 720000000,
    var deleted: Boolean = false,
    var commentUpdatedAt: Long? = null,
) : Parcelable {

    // no-arg constructor for deserialization
    constructor() : this("", "", "", "")

    companion object {
        const val TABLE_NAME = "tickets"
    }
}

/**
 * one-to-one relationship between ticket and user
 *
 * https://developer.android.com/training/data-storage/room/relationships#kotlin
 */
@Parcelize
data class UserAndTicket(
    @Relation(
        parentColumn = "technician",
        entityColumn = "_id",
    )
    val user: User,
    @Embedded
    val ticket: Ticket
) : Parcelable
