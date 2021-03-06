package io.helpdesk.view.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.helpdesk.R
import io.helpdesk.databinding.ItemTicketBinding
import io.helpdesk.model.data.TicketPriority
import io.helpdesk.model.data.UserAndTicket

/**
 * recyclerview adapter implementation for [PagingDataAdapter]
 */
class TicketsListAdapter :
    PagingDataAdapter<UserAndTicket, TicketsListAdapter.TicketsListViewHolder>(TICKET_DIFF_UTIL) {

    inner class TicketsListViewHolder(
        private val binding: ItemTicketBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserAndTicket) = with(binding) {
            data = item
            priorityColor = when (item.ticket.priority) {
                TicketPriority.High -> binding.root.context.getColor(R.color.priority_high)
                TicketPriority.Medium -> binding.root.context.getColor(R.color.priority_mid)
                TicketPriority.Low -> binding.root.context.getColor(R.color.priority_low)
            }
            root.setOnClickListener {
                root.findNavController()
                    .navigate(R.id.nav_ticket_info, bundleOf("ticket" to item.ticket))
            }
            executePendingBindings()
        }
    }

    override fun onBindViewHolder(holder: TicketsListViewHolder, position: Int) {
        val item = getItem(position)
        item?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketsListViewHolder =
        TicketsListViewHolder(
            ItemTicketBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    companion object {
        val TICKET_DIFF_UTIL: DiffUtil.ItemCallback<UserAndTicket> =
            object : DiffUtil.ItemCallback<UserAndTicket>() {
                override fun areItemsTheSame(
                    oldItem: UserAndTicket,
                    newItem: UserAndTicket
                ): Boolean =
                    oldItem.ticket.id == newItem.ticket.id

                override fun areContentsTheSame(
                    oldItem: UserAndTicket,
                    newItem: UserAndTicket
                ): Boolean =
                    oldItem == newItem
            }
    }
}