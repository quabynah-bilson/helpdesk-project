package io.helpdesk.view.bottomsheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import io.helpdesk.R
import io.helpdesk.databinding.TicketOptionsBottomSheetBinding
import io.helpdesk.model.data.Ticket
import io.helpdesk.model.data.UserAndTicket

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

    override fun onDestroyView() {
        requireActivity().window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        requireActivity().window?.navigationBarColor =
            ContextCompat.getColor(requireActivity(), R.color.blue_200)
        super.onDestroyView()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        requireActivity().window?.navigationBarColor =
            ContextCompat.getColor(requireActivity(), R.color.white)
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
                ticketOptionSelectListener.onItemSelected(TicketOptionsItem.Reassign)
                dismissAllowingStateLoss()
            }

            updatePriority.setOnClickListener {
                ticketOptionSelectListener.onItemSelected(TicketOptionsItem.UpdatePriority)
                dismissAllowingStateLoss()
            }

            sendFeedback.setOnClickListener {
                ticketOptionSelectListener.onItemSelected(TicketOptionsItem.SendFeedback)
                dismissAllowingStateLoss()
            }
        }

    }

    companion object {
        const val ARG_USER_AND_TICKET = "user.ticket.args"

        @JvmStatic
        fun newInstance(ticket: Ticket, listener: OnTicketOptionSelectListener): TicketOptionsBottomSheet {
            val fragment = TicketOptionsBottomSheet(listener)
            with(fragment) {
                arguments = bundleOf(ARG_USER_AND_TICKET to ticket)
            }
            return fragment
        }
    }

}

// region interface callback
enum class TicketOptionsItem { Reassign, UpdatePriority, SendFeedback }

interface OnTicketOptionSelectListener {
    fun onItemSelected(item: TicketOptionsItem)
}

// endregion