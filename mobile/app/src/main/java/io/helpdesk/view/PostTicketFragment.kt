package io.helpdesk.view

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import io.helpdesk.databinding.FragmentPostTicketBinding
import io.helpdesk.view.recyclerview.TechnicianAvatarListAdapter
import io.helpdesk.viewmodel.PostTicketUIState
import io.helpdesk.viewmodel.TicketsViewModel
import io.helpdesk.viewmodel.UserUIState
import io.helpdesk.viewmodel.UsersViewModel
import kotlinx.coroutines.flow.collectLatest

class PostTicketFragment : Fragment() {
    private val args by navArgs<PostTicketFragmentArgs>()
    private var binding: FragmentPostTicketBinding? = null

    private val ticketsViewModel by activityViewModels<TicketsViewModel>()
    private val usersViewModel by activityViewModels<UsersViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostTicketBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenCreated {
            binding?.run {

                val navController = findNavController()

                // get args
                with(titleField) {
                    addTextChangedListener { text: Editable? ->
                        postTicket.isEnabled = !text.isNullOrEmpty()
                    }

                    val question = args.question
                    if (question != null) {
                        setText(question.title)
                    }
                }


                // leave page
                backButton.setOnClickListener { navController.popBackStack() }

                // clear fields
                clearFields.setOnClickListener {
                    MaterialAlertDialogBuilder(requireContext()).apply {
                        setTitle("Reset all fields...")
                        setMessage("Do you wish to reset all fields? Your content will be deleted if you continue.")
                        setPositiveButton("yes") { d, _ ->
                            titleField.text?.clear()
                            descField.text?.clear()
                            d.dismiss()
                        }
                        setNegativeButton("no") { d, _ -> d.cancel() }
                        create()
                    }.show()
                }

                // adapter setup
                var hasUsers = false
                var technician: String? = null
                val usersAdapter = TechnicianAvatarListAdapter { user ->
                    println("selected user -> ${user.name}")
                    technician = user.id
                }

                // post ticket
                postTicket.setOnClickListener {
                    if (titleField.text.isNullOrEmpty()) {
                        titleField.error = "Enter a name for this ticket first"
                        return@setOnClickListener
                    }

                    ticketsViewModel.postNewTicket(
                        title = titleField.text.toString(),
                        navController = navController,
                        comment = descField.text.toString(),
                        technician = technician
                    )
                }

                // observe user list state
                usersViewModel.uiState.collectLatest { state ->
                    when (state) {
                        is UserUIState.Success -> {
                            usersAdapter.submitData(state.users)
                            hasUsers = usersAdapter.itemCount != 0
                        }

                        is UserUIState.Loading -> {
                            /*do nothing*/
                        }

                        is UserUIState.Error -> {
                            Snackbar.make(scrollContainer, state.reason, Snackbar.LENGTH_LONG)
                                .show()
                        }

                    }

                    // show error
                    noTechniciansText.isVisible = state is UserUIState.Success && !hasUsers

                    // show users' list
                    techniciansList.run {
                        isVisible = state is UserUIState.Success && hasUsers
                        setHasFixedSize(true)
                        adapter = usersAdapter
                    }

                    // show loading
                    loadingUsers.isVisible = state is UserUIState.Loading
                }

                ticketsViewModel.postTicketUIState.collectLatest { state ->
                    when (state) {
                        is PostTicketUIState.Loading -> {
                            /*do nothing*/
                        }

                        is PostTicketUIState.Error -> {
                            // show error
                            Snackbar.make(scrollContainer, state.reason, Snackbar.LENGTH_LONG)
                                .show()
                        }

                        is PostTicketUIState.Success -> {
                            // show success
                            Snackbar.make(
                                scrollContainer,
                                "ticket created successfully",
                                Snackbar.LENGTH_LONG
                            ).show()
                            navController.popBackStack()
                        }

                        is PostTicketUIState.Initial -> {
                            /*do nothing*/
                        }
                    }

                    // show loading
                    progressIndicator.root.isVisible = state is PostTicketUIState.Loading
                }
            }

        }

    }
}