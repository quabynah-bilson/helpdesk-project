package io.helpdesk.model.data

import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.SerializedName
import com.google.gson.internal.LinkedTreeMap
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
@Entity(tableName = Ticket.TABLE_NAME)
data class Ticket(
    @PrimaryKey
    @ColumnInfo(name = "_id")
    val id: String,
    val user: String,
    val name: String,
    var technician: String,
    val description: String = "no descriptions",
    val comment: String = "no comments",
    var status: TicketCompletionState = TicketCompletionState.Pending,
    var type: TicketType = TicketType.Question,
    val linkedTickets: List<String> = emptyList(),
    var priority: TicketPriority = TicketPriority.Medium,
    @ColumnInfo(name = "createdAt")
    val timestamp: String = Date(System.currentTimeMillis()).toString(),
    val dueDate: String = Date(System.currentTimeMillis() + 720000000).toString(),
    var deleted: Boolean = false,
) : Parcelable {

    // no-arg constructor for deserialization
    constructor() : this("", "", "", "")

    companion object {
        const val TABLE_NAME = "tickets"

        @Suppress("UNCHECKED_CAST")
        fun parser(map: LinkedTreeMap<String, Any?>) = Ticket(
            id = map["_id"].toString(),
            user = map["user"].toString(),
            name = map["name"].toString(),
            technician = map["technician"].toString(),
            description = map["description"].toString(),
            comment = map["comment"].toString(),
            status = TicketCompletionState.values()[(map["status"] as Double).toInt()],
            type = TicketType.values()[(map["type"] as Double).toInt()],
            linkedTickets = map["linkedTickets"] as List<String>? ?: emptyList(),
            timestamp = map["timestamp"].toString(),
            dueDate = map["due_date"].toString(),
            deleted = map["deleted"] as Boolean,
            priority = TicketPriority.values()[(map["priority"] as Double).toInt()]
        )
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
