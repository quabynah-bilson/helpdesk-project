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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.helpdesk.databinding.FragmentRegisterUserBinding
import io.helpdesk.viewmodel.AuthState
import io.helpdesk.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterUserFragment : Fragment() {
    private var binding: FragmentRegisterUserBinding? = null

    private val authViewModel by activityViewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterUserBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        lifecycleScope.launchWhenCreated {

            binding?.run {

                val navController = findNavController()

                backButton.setOnClickListener { navController.popBackStack() }

                usernameField.addTextChangedListener { text ->
                    registerButton.isEnabled =
                        !text.isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(text.toString())
                            .matches()
                }

                nameField.addTextChangedListener { text ->
                    registerButton.isEnabled = !text.isNullOrEmpty() && text.length >= 4
                }

                passwordField.addTextChangedListener { text ->
                    registerButton.isEnabled =
                        !text.isNullOrEmpty() && text.length >= 6
                }

                noAccountClickable.setOnClickListener {
                    val directions = LoginFragmentDirections.actionNavLoginToNavRegister()
                    navController.navigate(directions)
                }

                registerButton.setOnClickListener {
                    authViewModel.register(
                        username = nameField.text.toString(),
                        email = usernameField.text.toString(),
                        password = passwordField.text.toString(),
                    )
                }
                
                authViewModel.authState.collectLatest { state ->
                    when (state) {
                        is AuthState.Error -> {
                            lifecycleScope.launch {
                                Snackbar.make(
                                    root,
                                    state.reason,
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                        }

                        is AuthState.Success -> {
                            val dir =
                                RegisterUserFragmentDirections.actionNavRegisterToNavUserType()
                            navController.navigate(dir)
                        }

                        else -> {
                            /*do nothing*/
                        }
                    }


                    // toggle views
                    backButton.isVisible = state !is AuthState.Loading
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