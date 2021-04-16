package io.helpdesk.view.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import io.helpdesk.databinding.TicketOptionsBottomSheetBinding
import io.helpdesk.model.data.Ticket

/**
 * [Ticket] bottom sheet for showing actions to be performed on that item
 */
class TicketOptionsBottomSheet private constructor(private val ticketOptionSelectListener: OnTicketOptionSelectListener) :
    BottomSheetDialogFragment() {
    private var binding: TicketOptionsBottomSheetBinding? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TicketOptionsBottomSheetBinding.inflate(inflater, container, false)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get arguments
        val args = arguments?.getParcelable<Ticket>(ARG_USER_AND_TICKET)

        binding?.run {
            if (args == null) {
                Snackbar.make(root, "No ticket found", Snackbar.LENGTH_LONG).show()
                dismissAllowingStateLoss()
                return@run
            }

            reassign.setOnClickListener {
                ticketOptionSelectListener.onItemSelected(args, TicketOptionsItem.Reassign)
                dismissAllowingStateLoss()
            }

            updateState.setOnClickListener {
                ticketOptionSelectListener.onItemSelected(args, TicketOptionsItem.MarkAs)
                dismissAllowingStateLoss()
            }

            updatePriority.setOnClickListener {
                ticketOptionSelectListener.onItemSelected(args, TicketOptionsItem.UpdatePriority)
                dismissAllowingStateLoss()
            }

            sendFeedback.setOnClickListener {
                ticketOptionSelectListener.onItemSelected(args, TicketOptionsItem.SendFeedback)
                dismissAllowingStateLoss()
            }
        }

    }

    companion object {
        const val ARG_USER_AND_TICKET = "user.ticket.args"

        @JvmStatic
        fun newInstance(
            ticket: Ticket,
            listener: OnTicketOptionSelectListener
        ): TicketOptionsBottomSheet {
            val fragment = TicketOptionsBottomSheet(listener)
            with(fragment) {
                arguments = bundleOf(ARG_USER_AND_TICKET to ticket)
            }
            return fragment
        }
    }

}

// region interface callback
enum class TicketOptionsItem { Reassign, UpdatePriority, SendFeedback, MarkAs }

interface OnTicketOptionSelectListener {
    fun onItemSelected(ticket: Ticket, item: TicketOptionsItem)
}

// endregion