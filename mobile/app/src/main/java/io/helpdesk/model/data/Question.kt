package io.helpdesk.model.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.google.gson.internal.LinkedTreeMap
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "faqs")
data class Question(
    @PrimaryKey
    @ColumnInfo(name = "_id")
    @SerializedName("_id")
    val id: String,
    @ColumnInfo(name = "question")
    @SerializedName("question")
    val title: String,
    val answer: String,
    val priority: TicketPriority = TicketPriority.Low,
) : Parcelable {

    companion object {
        fun parser(map: LinkedTreeMap<String, Any?>) = Question(
            id = map["_id"].toString(),
            title = map["question"].toString(),
            answer = map["answer"].toString(),
            priority = TicketPriority.values()[(map["priority"] as Double).toInt()]
        )
    }
}
