package io.helpdesk.view.bottomsheet

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.helpdesk.R
import io.helpdesk.databinding.TicketFeedbackBottomSheetBinding
import io.helpdesk.model.data.Ticket

/**
 * [Ticket] bottom sheet for showing actions to be performed on that item
 */
class TicketFeedbackBottomSheet private constructor(private val listener: OnFeedbackCompleteListener) :
    BottomSheetDialogFragment() {
    private var binding: TicketFeedbackBottomSheetBinding? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TicketFeedbackBottomSheetBinding.inflate(inflater, container, false)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.run {
            feedbackField.addTextChangedListener { text ->
                with(submitButton) {
                    setOnClickListener {
                        if (text.isNullOrEmpty())
                            Toast.makeText(
                                requireContext(),
                                "Feedback is required",
                                Toast.LENGTH_SHORT
                            ).show()
                        else {
                            dismissAllowingStateLoss()
                            listener.onComplete(text.toString())
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "TicketFeedbackBottomS"

        @JvmStatic
        fun newInstance(
            listener: OnFeedbackCompleteListener
        ): TicketFeedbackBottomSheet = TicketFeedbackBottomSheet(listener)
    }

}

// region interface callback
interface OnFeedbackCompleteListener {
    fun onComplete(feedback: String)
}

// endregion