package io.helpdesk.view.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.helpdesk.databinding.TechniciansBottomSheetBinding
import io.helpdesk.model.data.Ticket
import io.helpdesk.model.data.User
import io.helpdesk.view.recyclerview.UsersListAdapter
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.run {

            lifecycleScope.launchWhenCreated {
                // adapter
                val technicianAdapter = UsersListAdapter { user ->
                    dismissAllowingStateLoss()
                    listener.onItemSelected(user)
                }


                // get technicians
                with(usersViewModel) {
                    loadUsers()
                    loadTechniciansState.collectLatest { state ->
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