package io.helpdesk.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.helpdesk.databinding.FaqsFragmentBinding
import io.helpdesk.databinding.ProgressIndicatorBinding
import io.helpdesk.view.recyclerview.QuestionsListAdapter
import io.helpdesk.viewmodel.FaqsViewModel
import io.helpdesk.viewmodel.QuestionsUIState
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class FaqsFragment : Fragment() {
    private var binding: FaqsFragmentBinding? = null
    private var progressBinding: ProgressIndicatorBinding? = null

    private val viewModel by viewModels<FaqsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FaqsFragmentBinding.inflate(inflater, container, false)
        if (binding != null) progressBinding =
            ProgressIndicatorBinding.bind(binding?.progressIndicator!!.root)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setup recyclerview
        val adapter = QuestionsListAdapter()
        binding?.faqsList?.adapter = adapter
        binding?.faqsList?.setHasFixedSize(true)

        lifecycleScope.launchWhenCreated {
            viewModel.uiState.collectLatest { state ->
                binding?.run {
                    when (state) {
                        is QuestionsUIState.Loading -> {
                            // show loading view
                            progressBinding?.root?.isVisible = true
                        }

                        is QuestionsUIState.Error -> {
                            progressBinding?.root?.isVisible = false
                            faqsList.isVisible = true
                            Snackbar.make(root, state.reason, Snackbar.LENGTH_LONG).show()
                        }

                        is QuestionsUIState.Success -> {
                            adapter.submitData(state.faqs)
                            faqsList.isVisible = adapter.itemCount > 0
                            progressBinding?.root?.isVisible = false
                        }
                    }
                }
            }
        }
    }
}

