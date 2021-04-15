package io.helpdesk

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.helpdesk.databinding.ActivityMainBinding
import io.helpdesk.model.data.UserType
import io.helpdesk.viewmodel.UsersViewModel
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val usersViewModel by viewModels<UsersViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialize navigation controller for fragments
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_fragment) as NavHostFragment
        navController = navHost.navController

        // Setting up bottom navigation requires that you also set up your navigation graph and menu xml
        binding.run {
            bottomNav.setupWithNavController(navController)

            // show bottom navigation only for admins and for selected destinations
            lifecycleScope.launchWhenCreated {
                usersViewModel.currentUser().collectLatest { user ->
                    Timber.tag("current-user").i("user -> $user")
                    navController.addOnDestinationChangedListener { _, destination, _ ->
                        val supportedDestinations = arrayOf(
                            R.id.nav_dashboard,
                            R.id.nav_users,
                            R.id.nav_faqs,
                            R.id.nav_tickets
                        )
                        bottomNav.isVisible =
                            supportedDestinations.contains(destination.id) && user?.type == UserType.SuperAdmin

                        when (destination.id) {
                            R.id.nav_dashboard,
                            R.id.nav_users,
                            R.id.nav_faqs,
                            R.id.nav_welcome,
                            R.id.nav_user_type,
                            R.id.nav_tickets -> {
                                window?.run {
                                    navigationBarColor = ContextCompat.getColor(
                                        this@MainActivity,
                                        R.color.helpdesk_blue_800
                                    )
                                }

                                if (destination.id != R.id.nav_welcome && destination.id != R.id.nav_user_type && destination.id != R.id.nav_user_type) {
                                    // add bottom padding to escape bottom navigation view
                                    navHost.view?.setPadding(
                                        0,
                                        0,
                                        0,
                                        resources.getDimensionPixelOffset(R.dimen.spacing_64)
                                    )
                                }

                                onBackPressedDispatcher.addCallback(
                                    this@MainActivity,
                                    object : OnBackPressedCallback(true) {
                                        override fun handleOnBackPressed() {
                                            if (destination.id == R.id.nav_dashboard) {
                                                MaterialAlertDialogBuilder(this@MainActivity).apply {
                                                    setTitle(getString(R.string.leave_app_prompt_title))
                                                    setMessage(getString(R.string.leave_app_prompt_content))
                                                    setPositiveButton("yes") { dialog, _ ->
                                                        run {
                                                            // leave app
                                                            dialog.dismiss()
                                                            finish()
                                                        }
                                                    }
                                                    setNegativeButton("no") { dialog, _ -> dialog.cancel() }
                                                    create()
                                                }.show()
                                            } else {
                                                // otherwise, select the initial page
                                                navController.navigate(R.id.nav_dashboard)
                                            }
                                        }
                                    })
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