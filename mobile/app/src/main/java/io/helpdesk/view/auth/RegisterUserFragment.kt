package io.helpdesk.view.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import io.helpdesk.databinding.FragmentRegisterUserBinding

@AndroidEntryPoint
class RegisterUserFragment : Fragment() {
    private var binding: FragmentRegisterUserBinding? = null

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

                registerButton.setOnClickListener {
                    navController.navigate(
                        RegisterUserFragmentDirections.actionNavRegisterToNavUserType(
                            authParams = NewUserAuthParams(
                                username = nameField.text.toString(),
                                email = usernameField.text.toString(),
                                password = passwordField.text.toString(),
                            )
                        )
                    )
                }
            }
        }

    }
}