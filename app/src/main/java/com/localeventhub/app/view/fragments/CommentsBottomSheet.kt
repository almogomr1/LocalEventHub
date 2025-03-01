package com.localeventhub.app.view.fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.localeventhub.app.adapters.CommentsAdapter
import com.localeventhub.app.databinding.CommentsBottomSheetBinding
import com.localeventhub.app.model.Comment
import com.localeventhub.app.model.Notification
import com.localeventhub.app.model.Post
import com.localeventhub.app.utils.Constants
import com.localeventhub.app.viewmodel.PostViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID


@AndroidEntryPoint
class CommentsBottomSheet(
    private val post: Post
) : BottomSheetDialogFragment() {

    private lateinit var binding:CommentsBottomSheetBinding
    private val commentsAdapter = CommentsAdapter()
    private val postViewModel: PostViewModel by viewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        postViewModel.loadCommentsForPost(post.postId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = CommentsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.rvComments.adapter = commentsAdapter
        binding.rvComments.layoutManager = LinearLayoutManager(requireContext())


        postViewModel.getCommentsForPost(post.postId).observe(viewLifecycleOwner) { comments ->

            commentsAdapter.submitList(comments)
        }

        commentsAdapter.setOnItemClickListener(object :CommentsAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {

            }

            override fun onItemDeleteClick(position: Int, comment: Comment) {
                postViewModel.deleteComment(comment.commentId) { success, message ->
                    if (success) {

                    } else {
                    }
                }
            }

        })


        binding.btnSend.setOnClickListener {
            val commentText = binding.commentInputBox.text.toString().trim()
            if (commentText.isNotEmpty()) {
                addComment(commentText)
                binding.commentInputBox.text?.clear()
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

            bottomSheet?.let { sheet ->

                sheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                sheet.requestLayout()

                val behavior = BottomSheetBehavior.from(sheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED // Ensure it's expanded
            }
        }

        return dialog
    }

    private fun addComment(commentText: String) {
        val newComment = Comment(
            commentId = UUID.randomUUID().toString(),
            postId = post.postId,
            userId = Constants.loggedUserId,
            content = commentText,
            timestamp = System.currentTimeMillis()
        )
        postViewModel.addComment(newComment) { success, message ->
            if (success) {
                saveNotification(post)
            } else {
            }
        }
    }

    private fun saveNotification(post: Post) {
        val notification = Notification(
            id = UUID.randomUUID().toString(),
            postId = post.postId,
            type = "comment",
            senderId = Constants.loggedUserId,
            receiverId = post.user!!.userId,
            message = "${Constants.loggedUser?.name} commented on your post.",
            timestamp = System.currentTimeMillis()
        )

        postViewModel.saveNotification(notification)
    }

}