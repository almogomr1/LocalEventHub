package com.localeventhub.app.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.localeventhub.app.R
import com.localeventhub.app.databinding.ItemPostBinding
import com.localeventhub.app.model.Post
import com.localeventhub.app.utils.Constants
import com.localeventhub.app.utils.ExifTransformation
import com.squareup.picasso.Picasso

class PostAdapter(private val posts: MutableList<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
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

    class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.apply {
                Picasso.get().load(post.user?.profileImageUrl).transform(ExifTransformation(post.user?.profileImageUrl!!)).placeholder(R.drawable.placeholder).into(userImage)
                userName.text = post.user.name
                postDate.text = Constants.getTimeAgo(post.timestamp)
                if (post.imageUrl.isNullOrEmpty()){
                    postImage.visibility = View.GONE
                }
                else{
                    postImage.visibility = View.VISIBLE
                    Picasso.get().load(post.imageUrl).transform(ExifTransformation(post.imageUrl)).placeholder(R.drawable.placeholder).into(postImage)
                }
                postDescription.text = post.description
            }
        }
    }
}
