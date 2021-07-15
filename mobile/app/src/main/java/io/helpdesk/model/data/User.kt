package io.helpdesk.model.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.google.gson.internal.LinkedTreeMap
import kotlinx.parcelize.Parcelize

enum class UserType { Technician, Customer, SuperAdmin, All }

/**
 * user data model
 */
@Parcelize
@Entity(tableName = User.TABLE_NAME)
data class User(
    @PrimaryKey
    @ColumnInfo(name = "_id") val id: String,
    @SerializedName("username")
    @ColumnInfo(name = "username")
    val email: String,
    val name: String,
    var avatar: String? = "",
    @ColumnInfo(name = "phoneNumber")
    var phone: String? = "",
    var token: String? = "",
    @SerializedName("type")
    @ColumnInfo(name = "userType")
    var type: UserType = UserType.Customer,
) : Parcelable {

    // no-arg constructor for deserialization
    constructor() : this("", "", "")

    companion object {
        const val TABLE_NAME = "users"
        fun parser(map: LinkedTreeMap<String, Any?>) = User(
            id = map["_id"].toString(),
            name = map["name"].toString(),
            email = map["username"].toString(),
            phone = map["phoneNumber"].toString(),
            avatar = map["avatar"].toString(),
            token = map["token"].toString(),
            type = UserType.values()[(map["user_type"] as Double).toInt()],
        )
    }
}