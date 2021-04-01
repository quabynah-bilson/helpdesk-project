package io.helpdesk.core.util

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.io.InputStream
import java.io.InputStreamReader

/**
 * JSON deserialization helper function
 */
inline fun <reified T> Context.deserializeJson(
    source: String,
    parser: (map: LinkedTreeMap<String, Any?>) -> T
): List<T> {

    return try {
        val inputStream: InputStream = resources.assets.open(source)
        val reader = InputStreamReader(inputStream)
        val gson = GsonBuilder().setPrettyPrinting().create()
        val type = object : TypeToken<ArrayList<T>>() {}.type
        val data = reader.readText()
        gson.fromJson<List<T>>(data, type).toList()
            .map { item ->
                @Suppress("UNCHECKED_CAST")
                parser(item as LinkedTreeMap<String, Any?>)
            }
    } catch (e: Exception) {
        Timber.tag("deserializer").e(e)
        emptyList()
    }

}