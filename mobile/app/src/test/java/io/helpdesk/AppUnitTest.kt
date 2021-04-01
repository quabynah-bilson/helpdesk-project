package io.helpdesk

import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import timber.log.Timber
import java.util.*

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
}