package com.localeventhub.app.adapters


import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.localeventhub.app.R
import com.localeventhub.app.databinding.ItemPostBinding
import com.localeventhub.app.model.Post
import com.localeventhub.app.utils.Constants
import com.localeventhub.app.utils.ExifTransformation
import com.squareup.picasso.Picasso

class PostAdapter(private val posts: MutableList<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int, post: Post)
        fun onItemEditClick(position: Int, post: Post)
        fun onItemDeleteClick(position: Int, post: Post)
        fun onItemLikeClick(position: Int, post: Post)
        fun onItemCommentClick(position: Int, post: Post)
    }

    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, mListener!!)
    }

    override fun getItemCount(): Int = posts.size

    fun updatePosts(newPosts: List<Post>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
    }

    class PostViewHolder(
        private val binding: ItemPostBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.apply {
                Picasso.get().load(post.user?.profileImageUrl)
                    .transform(ExifTransformation(post.user?.profileImageUrl!!))
                    .placeholder(R.drawable.placeholder).into(userImage)
                userName.text = post.user.name
                postDate.text = Constants.getTimeAgo(post.timestamp)
                if (post.imageUrl.isNullOrEmpty()) {
                    postImage.visibility = View.GONE
                } else {
                    postImage.visibility = View.VISIBLE
                    Picasso.get().load(post.imageUrl).transform(ExifTransformation(post.imageUrl!!))
                        .placeholder(R.drawable.placeholder).into(postImage)
                }
                val likedByList = post.getLikedByList()
                val isLiked = likedByList.contains(Constants.loggedUserId)
                binding.postLikeView.text = if (isLiked) "Liked" else "Like"
                if (isLiked) {
                    binding.postLikeView.apply {
                        setTypeface(null, Typeface.BOLD)
                        setTextColor(ContextCompat.getColor(context, R.color.primary))

                    }
                } else {
                    binding.postLikeView.apply {
                        setTypeface(null, Typeface.NORMAL)
                        setTextColor(ContextCompat.getColor(context, R.color.black))

                    }
                }

                binding.postLikeView.setOnClickListener {
                    mListener.onItemLikeClick(layoutPosition,post)
                }

                binding.root.setOnClickListener {
                    mListener.onItemClick(layoutPosition, post)
                }

                binding.postLikeView.setOnClickListener {
                    mListener.onItemLikeClick(layoutPosition, post)
                }

                binding.postCommentView.setOnClickListener {
                    mListener.onItemCommentClick(layoutPosition, post)
                }

                binding.postMoreOption.setOnClickListener { view ->
                    showPopupMenu(view, post)
                }

                postDescription.text = post.description
            }
        }

        private fun showPopupMenu(view: View, item: Post) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.post_pop_up_menu, popupMenu.menu)

            if (item.user?.userId != Constants.loggedUserId) {
                popupMenu.menu.findItem(R.id.menu_edit).isVisible = false
                popupMenu.menu.findItem(R.id.menu_delete).isVisible = false
            } else {
                popupMenu.menu.findItem(R.id.menu_edit).isVisible = true
                popupMenu.menu.findItem(R.id.menu_delete).isVisible = true
            }

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_view -> {
                        mListener.onItemClick(layoutPosition, item)
                    }

                    R.id.menu_edit -> {
                        mListener.onItemEditClick(layoutPosition, item)
                    }

                    R.id.menu_delete -> {
                        mListener.onItemDeleteClick(layoutPosition, item)
                    }

                }
                true
            }

            popupMenu.show()
        }
    }


}
