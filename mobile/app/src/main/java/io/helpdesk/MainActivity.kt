package io.helpdesk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import io.helpdesk.model.repos.BaseAuthenticationRepository
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: BaseAuthenticationRepository

    @Inject
    lateinit var auth: FirebaseAuth

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // initialize navigation controller for fragments
        navController =
            (supportFragmentManager.findFragmentById(R.id.nav_fragment) as NavHostFragment).navController

        lifecycleScope.launchWhenCreated {

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.tag("Google Auth data -> $data")
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                    ?.getResult(ApiException::class.java)
                if (account == null) {
                    Timber.tag("Google Auth").e("Account not found")
                } else {
                    auth
                        .signInWithCredential(
                            GoogleAuthProvider.getCredential(
                                account.idToken,
                                null
                            )
                        )
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val email = task.result?.user
                                Timber.tag("Sign in result").d("Signed in as -> $email")
                            } else {
                                Timber.tag("Sign in failed").e(task.exception?.localizedMessage)
                            }
                        }
                }
            } catch (e: Exception) {
                Timber.tag("Google Auth").e(e)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean = NavigationUI.navigateUp(navController, null)
}