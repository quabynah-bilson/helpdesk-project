package io.helpdesk.view.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.helpdesk.R
import io.helpdesk.databinding.ItemFaqBinding
import io.helpdesk.model.data.Question
import io.helpdesk.view.home.HomeFragmentDirections

/**
 * recyclerview adapter implementation for [PagingDataAdapter]
 */
class QuestionsListAdapter :
    PagingDataAdapter<Question, QuestionsListAdapter.QuestionsListViewHolder>(FAQ_DIFF_UTIL) {

    inner class QuestionsListViewHolder(
        private val binding: ItemFaqBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Question) = with(binding) {
            expanded = false
            faq = item
            faqAnswer.isVisible = expanded ?: false

            faqContainer.setOnClickListener {
//                binding.root.findNavController()
//                    .navigate(R.id.nav_post_ticket, bundleOf("question" to item))
                expanded = !(expanded ?: true)
            }
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