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
import io.helpdesk.model.data.Question
import io.helpdesk.model.db.QuestionDao
import io.helpdesk.viewmodel.FaqsViewModel
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class FaqsFragment : Fragment() {
    private var binding: FaqsFragmentBinding? = null
    private var faqs: List<Question> = emptyList()

    @Inject
    lateinit var dao: QuestionDao

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

        lifecycleScope.launchWhenCreated {
            dao.faqs().collectLatest {
                faqs = it
            }
        }
    }

}
