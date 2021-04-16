package io.helpdesk.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.helpdesk.databinding.FragmentTicketInfoBinding
import io.helpdesk.model.data.*
import io.helpdesk.view.bottomsheet.*
import io.helpdesk.viewmodel.TicketsViewModel
import io.helpdesk.viewmodel.UsersViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber


class TicketInfoFragment : Fragment(), OnTicketOptionSelectListener, OnTechnicianSelectListener,
    OnFeedbackCompleteListener {

    private var binding: FragmentTicketInfoBinding? = null
    private val args by navArgs<TicketInfoFragmentArgs>()
    private val usersViewModel by activityViewModels<UsersViewModel>()
    private val ticketsViewModel by activityViewModels<TicketsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTicketInfoBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding?.root
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val argTicket: Ticket = args.ticket
        println("ticket found => ${ticketsViewModel.parseTicketDate(argTicket.timestamp)}")

        binding?.run {
            deleteTicket.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setTitle("Confirm deletion")
                    setMessage("Do you wish to delete this ticket?\nThis action cannot be undone")
                    setPositiveButton("Cancel") { dialog, _ -> dialog.cancel() }
                    setNegativeButton("Yes, delete") { dialog, _ ->
                        ticketsViewModel.deleteTicket(argTicket)
                        dialog.dismiss()
                    }
                    show()
                }
            }

            backButton.setOnClickListener { findNavController().popBackStack() }

            with(usersViewModel) {
                updateTicketStatus.setOnClickListener {
                    TicketOptionsBottomSheet.newInstance(argTicket, this@TicketInfoFragment).show(
                        childFragmentManager,
                        TicketOptionsBottomSheet::class.java.canonicalName
                    )
                }

                lifecycleScope.launchWhenCreated {
                    // get technician
                    getUserById(argTicket.technician).collectLatest { technician ->
                        ticket = argTicket
                        if (technician != null) user = technician
                        executePendingBindings()
                    }
                }
            }

            GlobalScope.launch(Dispatchers.Main) {
                // get current user
                usersViewModel.currentUser().collectLatest { currentUser ->
                    Timber.tag("user details").d("current user -> $currentUser")
                    updateTicketStatus.isVisible = currentUser?.type != UserType.Customer
                    deleteTicket.isInvisible = currentUser?.id != argTicket.user
                    executePendingBindings()
                }
            }
        }
    }

    override fun onItemSelected(technician: User) {
        ticketsViewModel.updateTicket(ticket = args.ticket.copy(technician = technician.id))
    }

    override fun onComplete(feedback: String) {
        ticketsViewModel.updateTicket(ticket = args.ticket.copy(comment = feedback))
    }

    override fun onItemSelected(ticket: Ticket, item: TicketOptionsItem) {
        with(childFragmentManager) {
            when (item) {
                TicketOptionsItem.SendFeedback -> {
                    TicketFeedbackBottomSheet.newInstance(this@TicketInfoFragment)
                        .show(this, TicketFeedbackBottomSheet.TAG)
                }

                TicketOptionsItem.MarkAs -> {
                    var completion: TicketCompletionState = args.ticket.status
                    MaterialAlertDialogBuilder(requireContext()).apply {
                        setTitle("Mark ticket as...")
                        val options = arrayOf(
                            TicketCompletionState.Pending.name,
                            TicketCompletionState.Cancelled.name,
                            TicketCompletionState.Done.name,
                        )
                        setSingleChoiceItems(
                            options,
                            options.indexOf(completion.name)
                        ) { dialog, index ->
                            completion = when (index) {
                                0 -> TicketCompletionState.Pending
                                1 -> TicketCompletionState.Cancelled
                                else -> TicketCompletionState.Done
                            }

                            // update ticket
                            ticketsViewModel.updateTicket(args.ticket.copy(status = completion))
                            dialog.dismiss()
                        }
                        setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                        show()
                    }
                }

                TicketOptionsItem.UpdatePriority -> {
                    var priority: TicketPriority = args.ticket.priority
                    MaterialAlertDialogBuilder(requireContext()).apply {
                        setTitle("Ticket priority")
                        val options = arrayOf(
                            TicketPriority.Low.name,
                            TicketPriority.Medium.name,
                            TicketPriority.High.name,
                        )
                        setSingleChoiceItems(
                            options,
                            options.indexOf(priority.name)
                        ) { dialog, index ->
                            priority = when (index) {
                                0 -> TicketPriority.Low
                                1 -> TicketPriority.Medium
                                else -> TicketPriority.High
                            }
                            dialog.dismiss()

                            // update ticket
                            ticketsViewModel.updateTicket(args.ticket.copy(priority = priority))
                        }
                        setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                        show()
                    }
                }
                TicketOptionsItem.Reassign -> {
                    TechniciansBottomSheet.newInstance(this@TicketInfoFragment)
                        .show(this, TechniciansBottomSheet.TAG)
                }
            }
        }
    }

}