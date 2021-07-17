package io.helpdesk.view.shared

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.helpdesk.databinding.ProgressIndicatorBinding
import io.helpdesk.databinding.TicketsFragmentBinding
import io.helpdesk.model.data.UserType
import io.helpdesk.view.recyclerview.TicketsListAdapter
import io.helpdesk.viewmodel.LatestTicketUIState
import io.helpdesk.viewmodel.TicketsViewModel
import io.helpdesk.viewmodel.UsersViewModel
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

@AndroidEntryPoint
class TicketsFragment : Fragment() {
    private var binding: TicketsFragmentBinding? = null
    private var progressBinding: ProgressIndicatorBinding? = null

    private val ticketsViewModel by activityViewModels<TicketsViewModel>()
    private val usersViewModel by activityViewModels<UsersViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TicketsFragmentBinding.inflate(inflater, container, false)
        if (binding != null) progressBinding =
            ProgressIndicatorBinding.bind(binding?.progressIndicator!!.root)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get adapter
        val ticketsAdapter = TicketsListAdapter()

        // observe UI state
        lifecycleScope.launchWhenCreated {

            ticketsViewModel.ticketsUIState.collectLatest { state ->
                binding?.run {
                    // update UI based on current state
                    if (state is LatestTicketUIState.Success) {
                        ticketsAdapter.submitData(state.tickets)
                        Timber.tag("tickets").d("# of tickets -> ${ticketsAdapter.itemCount}")
                        ticketsList.isVisible = ticketsAdapter.itemCount > 0
                        container.isVisible = ticketsAdapter.itemCount == 0
                    } else if (state is LatestTicketUIState.Error) {
                        Snackbar.make(container, state.reason, Snackbar.LENGTH_LONG).show()
                        // show empty view
                        container.isVisible = true
                    }

                    // show loading view
                    progressBinding?.root?.isVisible = state is LatestTicketUIState.Loading
                }

            }
        }

        // perform binding
        binding?.run {
            viewModel = ticketsViewModel
            ticketsList.run {
                adapter = ticketsAdapter
            }
            isAdmin = false
            lifecycleScope.launchWhenStarted {
                usersViewModel.currentUser().collectLatest { currentUser ->
                    isAdmin = currentUser?.type == UserType.SuperAdmin
                }
            }
            executePendingBindings()
        }
    }


}