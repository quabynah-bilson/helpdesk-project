package io.helpdesk.model.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.google.gson.internal.LinkedTreeMap
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = Question.TABLE_NAME)
data class Question(
    @PrimaryKey
    @ColumnInfo(name = "_id")
    @SerializedName("_id")
    val id: String,
    val title: String,
    val answer: String,
//    val priority: TicketPriority = TicketPriority.Low,
) : Parcelable {

    // no-arg constructor for deserialization
    constructor() : this("", "", "")

    companion object {
        const val TABLE_NAME = "faqs"
    }
}
