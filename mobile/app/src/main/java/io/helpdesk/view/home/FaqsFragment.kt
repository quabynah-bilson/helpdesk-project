package io.helpdesk.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.helpdesk.databinding.FaqsFragmentBinding
import io.helpdesk.view.recyclerview.QuestionsListAdapter
import io.helpdesk.viewmodel.FaqsViewModel
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class FaqsFragment : Fragment() {
    private var binding: FaqsFragmentBinding? = null

    private val viewModel by viewModels<FaqsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FaqsFragmentBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setup recyclerview
        val adapter = QuestionsListAdapter()
        binding?.faqsList?.adapter = adapter
        binding?.faqsList?.setHasFixedSize(true)

        lifecycleScope.launchWhenCreated {
            viewModel.questions.collectLatest {
                adapter.submitData(it)
            }
        }
    }
}

