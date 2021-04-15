package io.helpdesk.view.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.helpdesk.databinding.UserProfileBottomSheetBinding
import io.helpdesk.model.data.User
import io.helpdesk.viewmodel.UsersViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserProfileBottomSheet private constructor(private val user: User) :
    BottomSheetDialogFragment() {
    private var binding: UserProfileBottomSheetBinding? = null
    private val usersViewModel by activityViewModels<UsersViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = UserProfileBottomSheetBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.run {
            currentUserViewModel = usersViewModel
            currentUser = user
            lifecycleScope.launchWhenCreated {
                usersViewModel.getUserById(user.id).collectLatest { updatedUser ->
                    currentUser = updatedUser
                    launch(Dispatchers.Main) { executePendingBindings() }
                }
            }
            fabCall.setOnClickListener {
                Toast.makeText(
                    requireContext(),
                    "Feature not available",
                    Toast.LENGTH_SHORT
                ).show()
            }

            fabReassign.setOnClickListener {
                Toast.makeText(
                    requireContext(),
                    "Feature not available",
                    Toast.LENGTH_SHORT
                ).show()
            }

            executePendingBindings()
        }
    }

    companion object {
        const val TAG = "UserProfileBottomSheet"

        @JvmStatic
        fun newInstance(user: User): UserProfileBottomSheet = UserProfileBottomSheet(user)
    }
}