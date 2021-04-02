package io.helpdesk.view.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.helpdesk.databinding.FragmentLoginBinding
import io.helpdesk.viewmodel.AuthState
import io.helpdesk.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.collectLatest


@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var binding: FragmentLoginBinding? = null


    private val authViewModel by activityViewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        lifecycleScope.launchWhenCreated {
            binding?.run {

                val navController = findNavController()

                usernameField.addTextChangedListener { text ->
                    loginButton.isEnabled =
                        !text.isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(text.toString())
                            .matches()
                }

                passwordField.addTextChangedListener { text ->
                    loginButton.isEnabled =
                        !text.isNullOrEmpty() && text.length >= 6
                }

                noAccountClickable.setOnClickListener {
                    val directions = LoginFragmentDirections.actionNavLoginToNavRegister()
                    navController.navigate(directions)
                }

                loginButton.setOnClickListener {
                    authViewModel.login(
                        email = usernameField.text.toString(),
                        password = passwordField.text.toString()
                    )
                }
                authViewModel.authState.collectLatest { state ->
                    when (state) {
                        is AuthState.Error -> Snackbar.make(
                            root,
                            state.reason,
                            Snackbar.LENGTH_LONG
                        ).show()

                        else -> {
                            /*do nothing*/
                        }
                    }
                }
            }
        }
    }

}