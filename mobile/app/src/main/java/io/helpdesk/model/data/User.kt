package io.helpdesk.model.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * user data model
 */
@Parcelize
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @ColumnInfo(name = "_id") val id: String,
    @ColumnInfo(name = "username")
    val email: String,
    var avatar: String?,
    @ColumnInfo(name = "phoneNumber")
    var phone: String?,
) : Parcelable