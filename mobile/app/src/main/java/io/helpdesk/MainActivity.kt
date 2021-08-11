package io.helpdesk

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import io.helpdesk.databinding.ActivityMainBinding
import io.helpdesk.model.data.UserType
import io.helpdesk.viewmodel.UsersViewModel
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

// technician -> wisdom@mail.com
// customer -> christian@gimpa.edu.gh
// admin -> admin@helpdesk.io

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val usersViewModel by viewModels<UsersViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    fun setupUI() {
        // initialize navigation controller for fragments
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_fragment) as NavHostFragment
        navController = navHost.navController

        // Setting up bottom navigation requires that you also set up your navigation graph and menu xml
        binding.bottomNav.setupWithNavController(navController)

        // show bottom navigation only for admins and for selected destinations
        lifecycleScope.launchWhenCreated {
            usersViewModel.currentUser().collectLatest { user ->
                Timber.tag("current-user").i("user -> $user")
                navController.addOnDestinationChangedListener { _, destination, _ ->
                    Timber.tag("nav-destination").i("destination [${destination.id}]")
                    val supportedDestinations = arrayOf(
                        R.id.nav_users,
                        R.id.nav_faqs,
                        R.id.nav_tickets,
                    )

                    binding.run {
                        bottomNav.isVisible = user != null &&
                                user.type == UserType.SuperAdmin &&
                                supportedDestinations.contains(destination.id)

                        when (destination.id) {
                            R.id.nav_users,
                            R.id.nav_faqs,
                            R.id.nav_welcome,
                            R.id.nav_user_type,
                            R.id.nav_ticket_info,
                            R.id.nav_tickets -> {
                                window?.run {
                                    navigationBarColor = ContextCompat.getColor(
                                        this@MainActivity,
                                        R.color.helpdesk_blue_800
                                    )

                                    if (destination.id == R.id.nav_ticket_info && user?.type == UserType.Customer) {
                                        navigationBarColor = ContextCompat.getColor(
                                            this@MainActivity,
                                            R.color.white
                                        )
                                    }
                                }

                                // add bottom padding to escape bottom navigation view
                                navHost.view?.setPadding(
                                    0,
                                    0,
                                    0,
                                    if (destination.id == R.id.nav_welcome
                                        || destination.id == R.id.nav_user_type
                                        || destination.id == R.id.nav_ticket_info
                                        || destination.id == R.id.nav_post_ticket
                                        || destination.id == R.id.nav_register
                                    ) 0 else resources.getDimensionPixelOffset(R.dimen.spacing_64)
                                )
                            }

                            else -> {
                                window?.run {
                                    navigationBarColor =
                                        ContextCompat.getColor(this@MainActivity, R.color.white)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean = NavigationUI.navigateUp(navController, null)
}