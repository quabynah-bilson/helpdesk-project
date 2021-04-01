package io.helpdesk.view.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.helpdesk.R
import io.helpdesk.core.util.loadImage
import io.helpdesk.databinding.ItemTechnicianAvatarBinding
import io.helpdesk.model.data.User

/**
 * recyclerview implementation for list of available technicians
 */
class TechnicianAvatarListAdapter constructor(private val onUserSelect: (User) -> Unit) :
    PagingDataAdapter<User, TechnicianAvatarListAdapter.TechnicianAvatarListViewHolder>(
        USER_DIFF_UTIL
    ) {

    companion object {
        private val USER_DIFF_UTIL: DiffUtil.ItemCallback<User> =
            object : DiffUtil.ItemCallback<User>() {
                override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
                    oldItem.id == newItem.id

                override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
                    oldItem == newItem
            }
    }

    inner class TechnicianAvatarListViewHolder(private val binding: ItemTechnicianAvatarBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User, onUserSelect: (User) -> Unit) = binding.run {
            this.user = user
            userAvatar.loadImage(
                user.avatar,
                R.drawable.avatar_circular_clip,
                R.drawable.avatar_circular_clip
            )
            root.setOnClickListener {
                onUserSelect(user)
            }
            executePendingBindings()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TechnicianAvatarListViewHolder = TechnicianAvatarListViewHolder(
        ItemTechnicianAvatarBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: TechnicianAvatarListViewHolder, position: Int) {
        val user = getItem(position)
        user?.let { holder.bind(it, onUserSelect) }
    }
}