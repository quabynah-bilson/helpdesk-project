package io.helpdesk.view.bottomsheet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
            currentUser = user
            lifecycleScope.launchWhenCreated {
                usersViewModel.getUserById(user.id).collectLatest { updatedUser ->
                    currentUser = updatedUser
                    launch(Dispatchers.Main) { executePendingBindings() }
                }
            }
            fabCall.setOnClickListener {
                if (currentUser?.phone == null) {
                    Toast.makeText(requireContext(), "User has no phone number", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
                with(Intent(Intent.ACTION_CALL)) {
                    data = Uri.parse("tel:${currentUser?.phone}")
                    requireActivity().startActivity(this)
                }
            }

            fabDelete.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setMessage("Do you wish to remove this user?")
                    setPositiveButton("yes") { d, _ ->
                        d.dismiss()
                        usersViewModel.deleteUser(currentUser)
                        dismissAllowingStateLoss()
                    }
                    setNegativeButton("no") { d, _ ->
                        d.cancel()
                    }
                    show()
                }
            }

            nameField.run {
                addTextChangedListener { text ->
                    if (text.isNullOrEmpty()) return@addTextChangedListener
                    currentUser = currentUser?.copy(name = text.toString())
                    executePendingBindings()
                }
            }

            phoneField.run {
                addTextChangedListener { text ->
                    if (text.isNullOrEmpty()) return@addTextChangedListener
                    currentUser = currentUser?.copy(phone = text.toString())
                    executePendingBindings()
                }
            }

            saveButton.setOnClickListener {
                if (user == currentUser) return@setOnClickListener
                usersViewModel.saveUser(currentUser)
                Toast.makeText(requireContext(), "Saved successfully", Toast.LENGTH_SHORT)
                    .show()
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