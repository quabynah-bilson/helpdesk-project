package io.helpdesk.model.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.google.gson.internal.LinkedTreeMap
import kotlinx.parcelize.Parcelize

enum class UserType { Technician, Customer, SuperAdmin, All }

// val (immutable variable) -> meaning that the value once set cannot be changed
// var (mutable variable) -> meaning that the value once set can be changed later

/**
 * user data model
 */
@Parcelize
@Entity(tableName = User.TABLE_NAME)
data class User(
    @PrimaryKey
    @ColumnInfo(name = "_id")
    val id: String,
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
    }
}