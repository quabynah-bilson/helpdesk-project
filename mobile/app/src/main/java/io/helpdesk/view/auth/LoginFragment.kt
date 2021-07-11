package io.helpdesk.view.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.helpdesk.databinding.FragmentLoginBinding
import io.helpdesk.model.data.UserType
import io.helpdesk.viewmodel.AuthState
import io.helpdesk.viewmodel.AuthViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var binding: FragmentLoginBinding? = null

    private val authViewModel by viewModels<AuthViewModel>()

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

                loginButton.setOnClickListener {
                    authViewModel.login(
                        email = usernameField.text.toString().trim(),
                        password = passwordField.text.toString().trim(),
                    )
                }
                authViewModel.authState.collectLatest { state ->
                    when (state) {
                        is AuthState.Error -> GlobalScope.launch(Dispatchers.Main) {
                            Snackbar.make(
                                root,
                                state.reason,
                                Snackbar.LENGTH_LONG
                            ).show()
                        }

                        is AuthState.Success -> {
                            // destination
                            val dir = if (state.user.type == UserType.SuperAdmin) {
                                LoginFragmentDirections.actionNavLoginToNavUsers()
                            } else {
                                LoginFragmentDirections.actionNavLoginToNavHome()
                            }
                            navController.navigate(dir)
                        }

                        else -> {
                            /*do nothing*/
                        }
                    }

                    // toggle views
                    bottomSection.isVisible = state !is AuthState.Loading
                    progressIndicator.isVisible = state is AuthState.Loading

                    // disable back press
                    requireActivity().onBackPressedDispatcher.addCallback(
                        viewLifecycleOwner,
                        object : OnBackPressedCallback(true) {
                            override fun handleOnBackPressed() {
                                if (state !is AuthState.Loading) navController.popBackStack()
                            }
                        })
                }
            }
        }
    }

}