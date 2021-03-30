package io.helpdesk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.AndroidEntryPoint
import io.helpdesk.model.repos.BaseAuthenticationRepository
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
        navController =
            (supportFragmentManager.findFragmentById(R.id.nav_fragment) as NavHostFragment).navController

        lifecycleScope.launchWhenCreated {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            val client = GoogleSignIn.getClient(this@MainActivity, gso)
            client.silentSignIn().addOnCompleteListener(this@MainActivity) { task ->
                if (task.isSuccessful) {
                    val email = task.result?.email
                    Timber.tag("Sign in result").d("Signed in as -> $email")
                } else {
                    Timber.tag("Sign in failed").e(task.exception?.localizedMessage)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this)
        Timber.tag("Get last sign in a/c").d("Last sign in -> ${lastSignedInAccount?.email}")
    }

    override fun onSupportNavigateUp(): Boolean = NavigationUI.navigateUp(navController, null)
}