package io.helpdesk.view.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import io.helpdesk.R
import io.helpdesk.databinding.FragmentRegisterUserBinding
import io.helpdesk.databinding.FragmentUserTypeBinding
import io.helpdesk.databinding.ProgressIndicatorBinding
import io.helpdesk.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.collectLatest

class RegisterUserFragment : Fragment() {
    private var binding: FragmentRegisterUserBinding? = null

    private val authViewModel by activityViewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterUserBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenCreated {

            authViewModel.authState.collectLatest { state -> }

            authViewModel.userTypeState.collectLatest { state -> }
        }

    }
}