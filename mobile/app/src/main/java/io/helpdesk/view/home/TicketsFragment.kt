package io.helpdesk.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.helpdesk.databinding.TicketsFragmentBinding
import io.helpdesk.viewmodel.TicketsViewModel

@AndroidEntryPoint
class TicketsFragment : Fragment() {

    private var binding: TicketsFragmentBinding? = null

    private val viewModel by viewModels<TicketsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TicketsFragmentBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

}