package io.helpdesk.view.bottomsheet

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.helpdesk.R
import io.helpdesk.databinding.TechniciansBottomSheetBinding
import io.helpdesk.model.data.Ticket
import io.helpdesk.model.data.User
import io.helpdesk.view.recyclerview.TechnicianAvatarListAdapter
import io.helpdesk.viewmodel.UserUIState
import io.helpdesk.viewmodel.UsersViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * [Ticket] bottom sheet for showing actions to be performed on that item
 */
class TechniciansBottomSheet private constructor(private val listener: OnTechnicianSelectListener) :
    BottomSheetDialogFragment() {
    private var binding: TechniciansBottomSheetBinding? = null
    private val usersViewModel by activityViewModels<UsersViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TechniciansBottomSheetBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onDestroyView() {
        requireActivity().window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        requireActivity().window?.navigationBarColor =
            ContextCompat.getColor(requireActivity(), TypedValue().apply {
                requireContext().theme.resolveAttribute(R.attr.colorPrimary, this, true)
            }.data)
        super.onDestroyView()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        requireActivity().window?.navigationBarColor =
            ContextCompat.getColor(requireActivity(), R.color.white)
        super.onViewCreated(view, savedInstanceState)

        binding?.run {

            lifecycleScope.launchWhenCreated {
                // adapter
                val technicianAdapter = TechnicianAvatarListAdapter { user ->
                    dismissAllowingStateLoss()
                    listener.onItemSelected(user)
                }

                // get technicians
                usersViewModel.loadTechniciansState.collectLatest { state ->
                    if (state is UserUIState.Success) {
                        technicianAdapter.submitData(PagingData.from(state.users))

                        // setup recyclerview
                        techniciansList.run {
                            setHasFixedSize(true)
                            adapter = technicianAdapter
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "TechniciansBottomSheet"

        @JvmStatic
        fun newInstance(
            listener: OnTechnicianSelectListener
        ): TechniciansBottomSheet = TechniciansBottomSheet(listener)
    }

}

// region interface callback
interface OnTechnicianSelectListener {
    fun onItemSelected(technician: User)
}

// endregion