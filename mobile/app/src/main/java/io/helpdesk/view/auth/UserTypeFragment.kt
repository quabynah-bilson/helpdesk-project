package io.helpdesk.view.auth

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.helpdesk.R
import io.helpdesk.databinding.FragmentUserTypeBinding
import io.helpdesk.databinding.ProgressIndicatorBinding
import io.helpdesk.model.data.UserType
import io.helpdesk.view.admin.UsersFragmentDirections
import io.helpdesk.viewmodel.AuthState
import io.helpdesk.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@Parcelize
data class NewUserAuthParams(val email: String, val password: String, val username: String) :
    Parcelable

@AndroidEntryPoint
class UserTypeFragment : Fragment() {
    private var binding: FragmentUserTypeBinding? = null
    private var progressBinding: ProgressIndicatorBinding? = null

    private val authViewModel by activityViewModels<AuthViewModel>()
    private val args by navArgs<UserTypeFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserTypeBinding.inflate(inflater, container, false)
        progressBinding = ProgressIndicatorBinding.bind(binding?.progressIndicator?.root!!)
        // Inflate the layout for this fragment
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // colors
        val selectedCardContentColor = resources.getColor(R.color.white, requireActivity().theme)
        val unselectedCardContentColor = resources.getColor(R.color.black, requireActivity().theme)

        lifecycleScope.launchWhenCreated {
            binding?.run {

                val navController = findNavController()

                // navigate to home screen
                saveButton.setOnClickListener {
                    authViewModel.register(
                        username = args.authParams.username,
                        email = args.authParams.email,
                        password = args.authParams.password,
                        userType = authViewModel.userTypeState.value,
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
                            Snackbar.make(root, "Account type saved", Snackbar.LENGTH_LONG).show()
                            navController.navigate(UsersFragmentDirections.actionNavUsersToNavRegister())
                        }

                        else -> {
                            /*do nothing*/
                        }
                    }

                    progressBinding?.run {
                        this.root.isVisible = state is AuthState.Loading
                        saveButton.isVisible = state !is AuthState.Loading
                    }
                }

                authViewModel.userTypeState.collectLatest { state ->
                    Timber.tag("user type selector").d("current state -> $state")
                    with(customerCard) {
                        isSelected = state == UserType.Customer
                        setOnClickListener {
                            if (state == UserType.Customer) return@setOnClickListener
                            authViewModel.updateUserType(UserType.Customer.ordinal)
                        }

                        if (isSelected) {
                            customerCardTitle.setTextColor(selectedCardContentColor)
                            customerCardFeature1.setTextColor(selectedCardContentColor)
                            customerCardFeature2.setTextColor(selectedCardContentColor)
                            customerCardFeature3.setTextColor(selectedCardContentColor)
                            customerCardFeature4.setTextColor(selectedCardContentColor)
                        } else {
                            customerCardTitle.setTextColor(unselectedCardContentColor)
                            customerCardFeature1.setTextColor(unselectedCardContentColor)
                            customerCardFeature2.setTextColor(unselectedCardContentColor)
                            customerCardFeature3.setTextColor(unselectedCardContentColor)
                            customerCardFeature4.setTextColor(unselectedCardContentColor)
                        }
                    }

                    with(technicianCard) {
                        isSelected = state == UserType.Technician
                        setOnClickListener {
                            if (state == UserType.Technician) return@setOnClickListener
                            authViewModel.updateUserType(UserType.Technician.ordinal)
                        }

                        if (isSelected) {
                            technicianCardTitle.setTextColor(selectedCardContentColor)
                            technicianCardFeature1.setTextColor(selectedCardContentColor)
                            technicianCardFeature2.setTextColor(selectedCardContentColor)
                            technicianCardFeature3.setTextColor(selectedCardContentColor)
                        } else {
                            technicianCardTitle.setTextColor(unselectedCardContentColor)
                            technicianCardFeature1.setTextColor(unselectedCardContentColor)
                            technicianCardFeature2.setTextColor(unselectedCardContentColor)
                            technicianCardFeature3.setTextColor(unselectedCardContentColor)
                        }
                    }
                }
            }
        }
    }
}