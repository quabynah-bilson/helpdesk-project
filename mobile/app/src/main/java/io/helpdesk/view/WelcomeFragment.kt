package io.helpdesk.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import io.helpdesk.core.storage.BaseUserPersistentStorage
import io.helpdesk.databinding.FragmentWelcomeBinding
import io.helpdesk.model.data.UserType
import io.helpdesk.viewmodel.UserUIState
import io.helpdesk.viewmodel.UsersViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject

/**
 * welcome page
 */
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class WelcomeFragment : Fragment() {
    private var binding: FragmentWelcomeBinding? = null
    private val userViewModel by activityViewModels<UsersViewModel>()

    @Inject
    lateinit var storage: BaseUserPersistentStorage

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
        super.onViewCreated(view, savedInstanceState)

        // handle button click
        lifecycleScope.launchWhenCreated { observeCurrentUser() }

        lifecycleScope.launchWhenStarted {
            userViewModel.loadUsers(UserType.All)
        }

        lifecycleScope.launchWhenResumed {
            userViewModel.loadTechniciansState.collectLatest { state ->

                if (state is UserUIState.Success) {
                    println(state.users)
                }
            }
        }
    }

    private suspend fun observeCurrentUser() {
        val navController = findNavController()
        userViewModel.currentUser().collectLatest { user ->
            Timber.tag("user-type").d("type -> ${user?.type}")

            binding?.skipButton?.setOnClickListener {
                /*if (user == null) {
                    navController.navigate(WelcomeFragmentDirections.actionNavWelcomeToNavLogin())
                } else {
                    // destination
                    val dir = if (user.type == UserType.SuperAdmin) {
                        WelcomeFragmentDirections.actionNavWelcomeToNavUsers()
                    } else {
                        WelcomeFragmentDirections.actionNavWelcomeToNavHome()
                    }
                    navController.navigate(dir)
                }*/

                navController.navigate(WelcomeFragmentDirections.actionNavWelcomeToNavLogin())
            }
        }
    }


}