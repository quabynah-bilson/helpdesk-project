package io.helpdesk.model.data

import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.sql.Date

enum class TicketCompletionState {
    Pending, Done, Cancelled,
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
@Entity(tableName = "tickets")
data class Ticket(
    @PrimaryKey
    @SerializedName("_id")
    @ColumnInfo(name = "_id")
    val id: String,
    val user: String,
    val name: String,
    val comment: String = "no comments",
    var status: TicketCompletionState = TicketCompletionState.Pending,
    var technician: String,
    var type: TicketType = TicketType.Question,
    val linkedTickets: List<String> = emptyList(),
    var priority: TicketPriority = TicketPriority.Medium,
    @SerializedName("createdAt")
    @ColumnInfo(name = "createdAt")
    val timestamp: String = Date(System.currentTimeMillis()).toString(),
    val dueDate: String = Date(System.currentTimeMillis() + 720000000).toString(),
) : Parcelable

/**
 * one-to-one relationship between ticket and user
 */
data class UserAndTicket(
    @Embedded val user: User,
    @Relation(
        parentColumn = "_id",
        entityColumn = "technician",
    )
    val ticket: Ticket
)