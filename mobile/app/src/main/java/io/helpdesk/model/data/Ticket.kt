package io.helpdesk.model.data

import android.os.Parcelable
import androidx.room.*
import kotlinx.parcelize.Parcelize

enum class TicketCompletionState {
    Done, Pending, Cancelled
}

enum class TicketType {
    Question,
}

enum class TicketPriority {
    High, Medium, Low
}

/**
 * ticket data model
 */
@Parcelize
@Entity(tableName = "tickets")
data class Ticket(
    @PrimaryKey
    @ColumnInfo(name = "_id")
    val id: String,
    val user: String,
    val name: String,
    val comment: String,
    var status: TicketCompletionState,
    var type: TicketType,
    var priority: TicketPriority,
    @ColumnInfo(name = "createdAt")
    val timestamp: String,
    val dueDate: String,
) : Parcelable

/**
 * one-to-one relationship between ticket and user
 */
data class UserAndTicket(
    @Embedded val user: User,
    @Relation(
        parentColumn = "_id",
        entityColumn = "user",
    )
    val ticket: Ticket
)