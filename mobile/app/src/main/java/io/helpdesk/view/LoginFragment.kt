package io.helpdesk.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import io.helpdesk.R
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class LoginFragment : Fragment() {
    @Inject
    lateinit var auth: FirebaseAuth

    companion object {
        private const val RC_SIGN_IN = 234
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenCreated {
            view.findViewById<SignInButton>(R.id.sign_in_button).setOnClickListener {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val client = GoogleSignIn.getClient(requireContext(), gso)

                startActivityForResult(
                    client.signInIntent,
                    RC_SIGN_IN
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        Timber.tag("Google Auth data -> $data")
        if (requestCode == RC_SIGN_IN && resultCode == Activity.RESULT_OK) {
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
                        .addOnCompleteListener(requireActivity()) { task ->
                            if (task.isSuccessful) {
                                val email = task.result?.user
                                Timber.tag("Sign in result").d("Signed in as -> $email")
                            } else {
                                Timber.tag("Sign in failed").e(task.exception?.localizedMessage)
                            }
                        }
                }
            } catch (e: Exception) {
                val TAG = "google authentication"
                Log.d(TAG, "onActivityResult: ${e.localizedMessage}")
                Timber.tag("Google Auth").e(e)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    override fun onStart() {
        super.onStart()
        val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(requireContext())
        Timber.tag("Get last sign in a/c").d("Last sign in -> ${lastSignedInAccount?.email}")
    }
}