package io.helpdesk.model.db

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

/**
 * base data access object implementation
 */
interface BaseDao<T> {
    @Insert
    suspend fun insert(item: T)

    @Update
    suspend fun update(item: T)

    @Insert
    suspend fun insertAll(items: List<T>)

    @Delete
    suspend fun delete(item: T)
}