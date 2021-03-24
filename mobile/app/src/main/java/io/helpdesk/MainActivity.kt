package io.helpdesk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.helpdesk.model.repos.BaseAuthenticationRepository
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: BaseAuthenticationRepository

    private val logger = Timber.tag("MainScreen")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launchWhenCreated {
            repository.googleAuth()
                .collectLatest {
                    logger.i("current auth state -> $it")
                }
        }
    }
}