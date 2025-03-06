package com.localeventhub.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.localeventhub.app.R
import com.localeventhub.app.databinding.ItemCommentBinding
import com.localeventhub.app.model.Comment
import com.localeventhub.app.utils.ImageTransform
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentsAdapter : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    private val comments = mutableListOf<Comment>()

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onItemDeleteClick(position: Int, comment: Comment)
    }

    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(view,mListener!!)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    override fun getItemCount(): Int = comments.size

    fun submitList(newComments: List<Comment>) {
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }

    class CommentViewHolder(private val binding: ItemCommentBinding,private val mListener: OnItemClickListener) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            Picasso.get().load(comment.user?.profileImageUrl)
//                .transform(ImageTransform(comment.user?.profileImageUrl!!))
                .placeholder(R.drawable.placeholder)
                .resize(200,200)
                .centerCrop()
                .into(binding.avatarImageView)
            binding.commentTextView.text = comment.content
            binding.dateTimeTextView.text =
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(
                    Date(comment.timestamp)
                )
            binding.deleteButton.setOnClickListener {
                mListener.onItemDeleteClick(layoutPosition,comment)
            }
        }
    }
}