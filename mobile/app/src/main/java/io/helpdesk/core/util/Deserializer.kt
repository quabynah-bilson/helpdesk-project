package io.helpdesk.core.util

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import io.helpdesk.model.data.Question
import timber.log.Timber
import java.io.InputStream
import java.io.InputStreamReader

/**
 * JSON deserialization helper function
 *
 * https://howtodoinjava.com/gson/gson-parse-json-array
 */
fun Context.deserializeJson(source: String): List<Question> {

    return try {
        val inputStream: InputStream = resources.assets.open(source)
        val reader = InputStreamReader(inputStream)
        val gson = GsonBuilder().setPrettyPrinting().create()
        val type = object : TypeToken<ArrayList<Question>>() {}.type
        val data = reader.readText()
        val result = gson.fromJson<List<Question>>(data, type).toList()
        result
    } catch (e: Exception) {
        Timber.tag("deserializer").e(e)
        emptyList()
    }

}