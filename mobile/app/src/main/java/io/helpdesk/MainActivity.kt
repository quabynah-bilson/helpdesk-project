package io.helpdesk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import dagger.hilt.android.AndroidEntryPoint
import io.helpdesk.model.data.AuthRequestParams
import io.helpdesk.model.repos.BaseAuthenticationRepository
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: BaseAuthenticationRepository

    private val logger = Timber.tag("MainScreen")
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // initialize navigation controller for fragments
        navController = Navigation.findNavController(this, R.id.nav_fragment)

        lifecycleScope.launchWhenCreated {
            repository.login(AuthRequestParams("quab@gmail.com", "Quabynah@2021"))
                .collectLatest {
                    logger.i("current auth state -> $it")
                }
        }
    }

    override fun onSupportNavigateUp(): Boolean = NavigationUI.navigateUp(navController, null)
}