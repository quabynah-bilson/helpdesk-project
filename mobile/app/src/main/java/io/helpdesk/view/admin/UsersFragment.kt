package io.helpdesk.view.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.helpdesk.R
import io.helpdesk.databinding.FragmentUsersBinding
import io.helpdesk.model.data.UserType
import io.helpdesk.view.bottomsheet.UserProfileBottomSheet
import io.helpdesk.view.recyclerview.UsersListAdapter
import io.helpdesk.viewmodel.AuthViewModel
import io.helpdesk.viewmodel.UserUIState
import io.helpdesk.viewmodel.UsersViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

/**
 * Users List UI for admin
 */
class UsersFragment : Fragment(), PopupMenu.OnMenuItemClickListener {
    private var binding: FragmentUsersBinding? = null

    private val usersViewModel by activityViewModels<UsersViewModel>()
    private val authViewModel by activityViewModels<AuthViewModel>()
    private val queryUserType = MutableStateFlow(UserType.All)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.run {
            lifecycleScope.launchWhenCreated {

                queryUserType.collectLatest { type ->
                    userType = type
                    usersViewModel.run {
                        loadUsers(type)
                        loadTechniciansState.collectLatest { state ->
                            if (state is UserUIState.Success) {
                                Timber.tag("users-db").i("users obtained -> ${state.users.size}")
                                val usersAdapter = UsersListAdapter { user ->
                                    UserProfileBottomSheet.newInstance(user)
                                        .show(childFragmentManager, UserProfileBottomSheet.TAG)
                                }.apply { submitData(PagingData.from(state.users)) }

                                // setup recyclerview
                                usersList.run {
                                    adapter = usersAdapter
                                    setHasFixedSize(true)
                                }
                            }

                            usersList.isVisible = state is UserUIState.Success
                            emptyContainer.isVisible = state is UserUIState.Error
                            progressIndicator.isVisible = state is UserUIState.Loading
                        }
                    }
                }
            }

            sortIcon.setOnClickListener {
                val popupMenu = PopupMenu(requireContext(), it)
                with(popupMenu) {
                    popupMenu.setOnMenuItemClickListener(this@UsersFragment)
                    popupMenu.inflate(R.menu.user_type_popup_menu)
                    show()
                }
            }

            fabAddUser.setOnClickListener { findNavController().navigate(UsersFragmentDirections.actionNavUsersToNavRegister()) }

            executePendingBindings()
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.itemId) {

            R.id.user_type_all -> {
                queryUserType.tryEmit(UserType.All)
                true
            }

            R.id.user_type_customer -> {
                queryUserType.tryEmit(UserType.Customer)
                true
            }

            R.id.user_type_technician -> {
                queryUserType.tryEmit(UserType.Technician)
                true
            }

            R.id.sign_out -> {
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setTitle(getString(R.string.leave_app_prompt_title))
                    setMessage(getString(R.string.sign_out_prompt_content))
                    setPositiveButton("yes") { dialog, _ ->
                        run {
                            // leave app
                            dialog.dismiss()
                            authViewModel.logout()
                            val navController = findNavController()
                            navController.navigate(UsersFragmentDirections.actionNavUsersToNavWelcome())
                        }
                    }
                    setNegativeButton("no") { dialog, _ -> dialog.cancel() }
                    create()
                }.show()
                true
            }
            else -> false
        }
    }


}