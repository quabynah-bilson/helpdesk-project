package io.helpdesk

import android.os.Build
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import timber.log.Timber
import java.time.LocalDate
import java.time.Period
import java.util.*
import kotlin.random.Random

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class AppUnitTest {
    @Test
    fun generateIds() {
        for (i in 0..16) {
            println("id -> ${UUID.randomUUID()}")
        }
    }

    @Test
    fun getDateOfBirth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val minus = LocalDate.now().minus(Period.ofDays((Random.nextInt(365 * 70))))
            println("date od birth -> $minus")
        }
    }
}