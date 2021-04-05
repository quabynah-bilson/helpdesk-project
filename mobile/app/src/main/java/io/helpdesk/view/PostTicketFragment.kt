package io.helpdesk.view

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.PagingData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import io.helpdesk.R
import io.helpdesk.databinding.FragmentPostTicketBinding
import io.helpdesk.view.recyclerview.TechnicianAvatarListAdapter
import io.helpdesk.viewmodel.PostTicketUIState
import io.helpdesk.viewmodel.TicketsViewModel
import io.helpdesk.viewmodel.UserUIState
import io.helpdesk.viewmodel.UsersViewModel
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

class PostTicketFragment : Fragment() {
    private val args by navArgs<PostTicketFragmentArgs>()
    private var binding: FragmentPostTicketBinding? = null

    private val ticketsViewModel by activityViewModels<TicketsViewModel>()
    private val usersViewModel by activityViewModels<UsersViewModel>()


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
        binding = FragmentPostTicketBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding?.root
    }

    // show colored system bar
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        requireActivity().window?.navigationBarColor =
            ContextCompat.getColor(requireActivity(), R.color.blue_200)
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
                            val usersAdapter = TechnicianAvatarListAdapter(state.users) { user ->
                                technician = user.id
                                Snackbar.make(
                                    scrollContainer,
                                    "${user.name} selected",
                                    Snackbar.LENGTH_LONG
                                )
                                    .show()
                            }
                            with(usersAdapter) {
                                submitData(PagingData.from(state.users))
                                techniciansList.adapter = this
                            }
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
                    }

                    // show loading
                    loadingUsers.isVisible = state is UserUIState.Loading
                }

                ticketsViewModel.postTicketUIState.collectLatest { state ->
                    Timber.tag("post ticket").d("state -> $state")
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