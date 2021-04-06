package io.helpdesk.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.helpdesk.R
import io.helpdesk.databinding.FragmentTicketInfoBinding
import io.helpdesk.model.data.Ticket
import io.helpdesk.model.data.TicketPriority
import io.helpdesk.model.data.User
import io.helpdesk.view.bottomsheet.*
import io.helpdesk.viewmodel.TicketsViewModel
import io.helpdesk.viewmodel.UsersViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest


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
        println("ticket found => $argTicket")

        binding?.run {
            deleteTicket.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setTitle("Confirm deletion")
                    setMessage("Do you wish to delete this ticket?\nThis action cannot be undone")
                    setPositiveButton("Cancel") { dialog, _ -> dialog.cancel() }
                    setPositiveButton("Yes, delete") { dialog, _ ->
                        ticketsViewModel.deleteTicket(argTicket)
                        dialog.dismiss()
                    }
                    show()
                }
            }

            backButton.setOnClickListener { findNavController().popBackStack() }

            lifecycleScope.launchWhenCreated {
                with(usersViewModel) {
                    // get technician
                    getTechnician(argTicket.technician).collectLatest { technician ->
                        ticket = argTicket
                        if (technician != null) user = technician
                        executePendingBindings()
                    }

                    // get current user
                    currentUser().collectLatest { currentUser ->
                        if (currentUser?.id == argTicket.technician) {
                            requireActivity().window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                            requireActivity().window?.navigationBarColor =
                                ContextCompat.getColor(requireActivity(), R.color.blue_200)
                            updateTicketStatus.isVisible = currentUser.id == argTicket.technician
                        }
                    }
                }

                updateTicketStatus.setOnClickListener {
                    TicketOptionsBottomSheet.newInstance(argTicket, this@TicketInfoFragment).show(
                        childFragmentManager,
                        TicketOptionsBottomSheet::class.java.canonicalName
                    )
                }
            }
        }
    }

    // reset system bar color
    override fun onDestroyView() {
        requireActivity().window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        requireActivity().window?.navigationBarColor =
            ContextCompat.getColor(requireActivity(), R.color.white)
        super.onDestroyView()
    }

    override fun onItemSelected(item: TicketOptionsItem) {
        with(childFragmentManager) {
            when (item) {
                TicketOptionsItem.SendFeedback -> {
                    TicketFeedbackBottomSheet.newInstance(this@TicketInfoFragment)
                        .show(this, TicketFeedbackBottomSheet.TAG)
                }
                TicketOptionsItem.UpdatePriority -> {
                    var priority: TicketPriority = TicketPriority.Medium
                    MaterialAlertDialogBuilder(requireContext()).apply {
                        setTitle("Ticket priority")
                        val options = arrayOf("Low", "Medium", "High")
                        setItems(options) { dialog, index ->
                            priority = when (index) {
                                0 -> TicketPriority.Low
                                1 -> TicketPriority.Medium
                                else -> TicketPriority.High
                            }
                            dialog.dismiss()
                        }
                        setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                        show()
                    }

                    // update ticket
                    ticketsViewModel.updateTicket(args.ticket.copy(priority = priority))
                }
                TicketOptionsItem.Reassign -> {
                    TechniciansBottomSheet.newInstance(this@TicketInfoFragment)
                        .show(this, TechniciansBottomSheet.TAG)
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

}