package io.helpdesk.view.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.helpdesk.databinding.FragmentUserTypeBinding
import io.helpdesk.databinding.ProgressIndicatorBinding
import io.helpdesk.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class UserTypeFragment : Fragment() {
    private var binding: FragmentUserTypeBinding? = null
    private var progressBinding: ProgressIndicatorBinding? = null

    private val authViewModel by activityViewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserTypeBinding.inflate(inflater, container, false)
        progressBinding = ProgressIndicatorBinding.bind(binding?.progressIndicator?.root!!)
        // Inflate the layout for this fragment
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        lifecycleScope.launchWhenCreated {

            authViewModel.userTypeState.collectLatest { state -> }
        }
    }

}