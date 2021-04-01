package io.helpdesk.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.helpdesk.databinding.TicketsFragmentBinding
import io.helpdesk.viewmodel.LatestTicketUIState
import io.helpdesk.viewmodel.TicketsViewModel
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class TicketsFragment : Fragment() {
    private var binding: TicketsFragmentBinding? = null

    private val ticketsViewModel by viewModels<TicketsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TicketsFragmentBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // observe UI state
        lifecycleScope.launchWhenCreated {
            ticketsViewModel.uiState.collectLatest { state ->
                binding?.progressIndicator?.isVisible = state is LatestTicketUIState.Loading
                binding?.ticketsList?.isVisible =
                    state is LatestTicketUIState.Success && state.tickets.isNotEmpty()
                binding?.container?.isVisible =
                    state is LatestTicketUIState.Success && state.tickets.isEmpty() || state is LatestTicketUIState.Error
            }
        }

        // perform binding
        binding?.run {
            viewModel = ticketsViewModel
            executePendingBindings()
        }

    }

}