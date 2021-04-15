package io.helpdesk.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import io.helpdesk.R
import io.helpdesk.core.storage.BaseUserPersistentStorage
import io.helpdesk.core.util.logger
import io.helpdesk.databinding.FragmentWelcomeBinding
import io.helpdesk.model.data.UserType
import io.helpdesk.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * welcome page
 */
@AndroidEntryPoint
class WelcomeFragment : Fragment() {
    private var binding: FragmentWelcomeBinding? = null
    private val authViewModel by activityViewModels<AuthViewModel>()

    @Inject
    lateinit var storage: BaseUserPersistentStorage

    // reset system bar color
    override fun onDestroyView() {
        requireActivity().window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        requireActivity().window?.navigationBarColor =
            ContextCompat.getColor(requireActivity(), R.color.white)
        super.onDestroyView()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding?.root
    }

    // show colored system bar
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        requireActivity().window?.navigationBarColor =
            ContextCompat.getColor(requireActivity(), R.color.blue_200)
        super.onViewCreated(view, savedInstanceState)

        // handle button click
        lifecycleScope.launch {
            storage.clear()
            storage.loginState.collectLatest { state ->
                logger.i("login state -> $state")
            }

            authViewModel.loginState.collectLatest { loggedIn ->
                Timber.tag("user-login-state").d("state -> $loggedIn")
                binding?.run {
                    if (loggedIn) {
                        authViewModel.userTypeState.collectLatest { type ->
                            Timber.tag("user-type").d("type -> $type")
                            skipButton.setOnClickListener {
                                // destination
                                val dir = if (type == UserType.SuperAdmin) {
                                    WelcomeFragmentDirections.actionNavWelcomeToNavDashboard()
                                } else {
                                    WelcomeFragmentDirections.actionNavWelcomeToNavHome()
                                }
                                findNavController().navigate(dir)
                            }
                        }
                    } else {
                        skipButton.setOnClickListener {
                            findNavController().navigate(WelcomeFragmentDirections.actionNavWelcomeToNavLogin())
                        }
                    }
                }
            }
        }

    }


}