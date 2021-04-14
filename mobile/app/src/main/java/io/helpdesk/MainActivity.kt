package io.helpdesk

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.helpdesk.model.data.UserType
import io.helpdesk.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private val authViewModel by viewModels<AuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // initialize navigation controller for fragments
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_fragment) as NavHostFragment
        navController = navHost.navController

        // Setting up bottom navigation requires that you also set up your navigation graph and menu xml
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setupWithNavController(navController)

        // show bottom navigation only for admins and for selected destinations
        navController.addOnDestinationChangedListener { _, destination, _ ->
            lifecycleScope.launchWhenCreated {
                authViewModel.userTypeState.collectLatest { userType ->
                    when (destination.id) {
                        R.id.nav_dashboard,
                        R.id.nav_users,
                        R.id.nav_faqs,
                        R.id.nav_tickets -> {
                            bottomNav.isVisible = userType == UserType.SuperAdmin

                            // add bottom padding to escape bottom navigation view
                            navHost.view?.setPadding(
                                0,
                                0,
                                0,
                                resources.getDimensionPixelOffset(R.dimen.spacing_64)
                            )

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

                        else -> bottomNav.isVisible = false
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean = NavigationUI.navigateUp(navController, null)
}