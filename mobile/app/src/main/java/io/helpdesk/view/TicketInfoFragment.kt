package io.helpdesk.view

import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import io.helpdesk.R
import io.helpdesk.core.util.getColorInt
import io.helpdesk.databinding.FragmentTicketInfoBinding
import io.helpdesk.model.data.*
import io.helpdesk.view.bottomsheet.*
import io.helpdesk.viewmodel.TicketsViewModel
import io.helpdesk.viewmodel.UsersViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import java.sql.Date
import java.util.*


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

    @DelicateCoroutinesApi
    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.run {
            deleteTicket.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setTitle("Confirm deletion")
                    setMessage("Do you wish to delete this ticket?\nThis action cannot be undone")
                    setPositiveButton("Cancel") { dialog, _ -> dialog.cancel() }
                    setNegativeButton("Yes, delete") { dialog, _ ->
                        ticketsViewModel.deleteTicket(ticket!!)
                        dialog.dismiss()
                        findNavController().popBackStack()
                        Toast.makeText(
                            requireContext(),
                            "Deleted \"${args.ticket.name}\" successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    show()
                }
            }

            backButton.setOnClickListener {
                /*requireActivity().onBackPressed()*/
                findNavController().navigateUp()
            }

            // get requestor for ticket
            lifecycleScope.launchWhenCreated {
                usersViewModel.getUserById(args.ticket.user).collectLatest { requestor ->
                    binding?.user = requestor
                }
            }

            // get technician details
            lifecycleScope.launch(Dispatchers.Main) { getTechnicianInfo() }

            // get current user
            lifecycleScope.launchWhenResumed {
                usersViewModel.currentUser().collectLatest { currentUser ->
                    Timber.tag("user details").d("current user -> $currentUser")
                    updateTicketStatus.isVisible =
                        currentUser?.type != UserType.Customer && args.ticket.user != currentUser?.id
                    deleteTicket.isInvisible = currentUser?.id != args.ticket.user
                    updateTicketStatus.setOnClickListener {
                        currentUser?.type?.let { type ->
                            TicketOptionsBottomSheet.newInstance(
                                ticket!!,
                                type,
                                this@TicketInfoFragment
                            ).show(
                                childFragmentManager,
                                TicketOptionsBottomSheet::class.java.canonicalName
                            )
                        }
                    }
                }
            }

            executePendingBindings()
        }
    }

    private suspend fun getTechnicianInfo() {
        // get technician
        usersViewModel.getUserById(args.ticket.technician).collectLatest { technician ->
            if (technician != null) binding?.technician = technician
            ticketsViewModel.getTicketById(args.ticket.id).collectLatest { data ->
                binding?.run {
                    ticket = data
                    ticketVM = ticketsViewModel
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && ticket != null) {
                        ticketTimestamp.text = parseTicketDate(ticket!!.timestamp)

                        if (ticket!!.commentUpdatedAt != null) {
                            ticketCommentTimestamp.text =
                                parseTicketDate(ticket!!.commentUpdatedAt!!)
                        }
                    }
                    ticketStatusColor = when (data?.status) {
                        TicketCompletionState.Cancelled -> requireContext().getColorInt(
                            R.color.ticket_status_cancelled
                        )
                        TicketCompletionState.Pending -> requireContext().getColorInt(
                            R.color.ticket_status_pending
                        )
                        TicketCompletionState.Done -> requireContext().getColorInt(
                            R.color.ticket_status_done
                        )

                        else -> requireContext().getColorInt(
                            R.color.ticket_status_pending
                        )
                    }
                }
            }
        }
    }

    override fun onItemSelected(technician: User) {
        ticketsViewModel.updateTicket(ticket = binding?.ticket?.copy(technician = technician.id))
        if (binding != null) Snackbar.make(
            binding!!.root,
            "Ticket reassigned successfully",
            Snackbar.LENGTH_SHORT
        ).show()
        findNavController().navigateUp()
    }

    override fun onComplete(feedback: String) {
        ticketsViewModel.updateTicket(
            ticket = binding?.ticket?.copy(
                comment = feedback,
                commentUpdatedAt = System.currentTimeMillis(),
            )
        )
    }

    override fun onItemSelected(ticket: Ticket, item: TicketOptionsItem) {
        with(childFragmentManager) {
            when (item) {
                TicketOptionsItem.SendFeedback -> {
                    TicketFeedbackBottomSheet.newInstance(this@TicketInfoFragment)
                        .show(this, TicketFeedbackBottomSheet.TAG)
                }

                TicketOptionsItem.MarkAs -> {
                    var completion: TicketCompletionState =
                        binding?.ticket?.status ?: TicketCompletionState.Pending
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
                            ticketsViewModel.updateTicket(binding?.ticket?.copy(status = completion))
                            dialog.dismiss()
                        }
                        setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                        show()
                    }
                }

                TicketOptionsItem.UpdatePriority -> {
                    var priority: TicketPriority = binding?.ticket?.priority ?: TicketPriority.Low
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
                            ticketsViewModel.updateTicket(binding?.ticket?.copy(priority = priority))
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseTicketDate(timestamp: Long): String =
        with(SimpleDateFormat("EEE, MMM d 'at' HH:mm", Locale.getDefault())) {
            val logger = Timber.tag("date-parser")
            var format = "not set"
            try {
                val date = Date(timestamp)
                format = format(calendar.time)
                logger.d("parsed-date -> $format")
            } catch (e: Exception) {
                logger.e(e.localizedMessage)
            }
            return@with format
        }

}