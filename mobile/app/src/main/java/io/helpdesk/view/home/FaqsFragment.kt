package io.helpdesk.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import io.helpdesk.databinding.FaqsFragmentBinding
import io.helpdesk.databinding.ItemFaqBinding
import io.helpdesk.model.data.Question
import io.helpdesk.model.db.QuestionDao
import io.helpdesk.viewmodel.FaqsViewModel
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

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

/**
 * recyclerview adapter implementation for [PagingDataAdapter]
 */
class QuestionsListAdapter :
    PagingDataAdapter<Question, QuestionsListAdapter.QuestionsListViewHolder>(FAQ_DIFF_UTIL) {

    inner class QuestionsListViewHolder(
        private val binding: ItemFaqBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Question) = with(binding) {
            faqAnswer.text = item.answer
            faqTitle.text = item.title

            val direction = HomeFragmentDirections.actionNavHomeToNavPostTicket(item)
            faqContainer.setOnClickListener { binding.root.findNavController().navigate(direction) }
        }

    }

    override fun onBindViewHolder(holder: QuestionsListViewHolder, position: Int) {
        val item = getItem(position)
        item?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionsListViewHolder =
        QuestionsListViewHolder(
            ItemFaqBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    companion object {
        val FAQ_DIFF_UTIL: DiffUtil.ItemCallback<Question> =
            object : DiffUtil.ItemCallback<Question>() {
                override fun areItemsTheSame(oldItem: Question, newItem: Question): Boolean =
                    oldItem.id == newItem.id

                override fun areContentsTheSame(oldItem: Question, newItem: Question): Boolean =
                    oldItem == newItem
            }
    }
}