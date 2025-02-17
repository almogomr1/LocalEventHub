package com.localeventhub.app.view.fragments

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.localeventhub.app.R
import com.localeventhub.app.adapters.PostAdapter.OnItemClickListener
import com.localeventhub.app.databinding.FragmentPostDetailsBinding
import com.localeventhub.app.model.Notification
import com.localeventhub.app.model.Post
import com.localeventhub.app.utils.Constants
import com.localeventhub.app.utils.ExifTransformation
import com.localeventhub.app.view.activities.UpdatePostActivity
import com.localeventhub.app.viewmodel.PostViewModel
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class PostDetailsFragment : Fragment() {

    private lateinit var binding:FragmentPostDetailsBinding
    private var post:Post?=null
    private val postViewModel: PostViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPostDetailsBinding.inflate(inflater, container, false)



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        post = arguments?.getSerializable("post") as? Post

        post?.let {
            Picasso.get().load(post?.user?.profileImageUrl).transform(ExifTransformation(post?.user?.profileImageUrl!!)).placeholder(R.drawable.placeholder).into(binding.userImage)
            binding.userName.text = post?.user?.name
            binding.postDate.text = Constants.getTimeAgo(post!!.timestamp)
            if (post!!.imageUrl.isNullOrEmpty()){
                binding.postImage.visibility = View.GONE
            }
            else{
                binding.postImage.visibility = View.VISIBLE
                Picasso.get().load(post!!.imageUrl).transform(ExifTransformation(post!!.imageUrl as String)).placeholder(R.drawable.placeholder).into(binding.postImage)
            }

            binding.postDescription.text = post!!.description

            binding.postMoreOption.setOnClickListener {v ->
                showPopupMenu(v, post!!)
            }

            val likedByList = post!!.getLikedByList()
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
                postViewModel.likePost(post!!, Constants.loggedUserId) { success, message ->
                    if (success) {
                        post.let {
                            val likedByList = it!!.getLikedByList().toMutableList()
                            if (likedByList.contains(Constants.loggedUserId)) {
                                likedByList.remove(Constants.loggedUserId)
                                binding.postLikeView.apply {
                                    text = "Like"
                                    setTypeface(null, Typeface.NORMAL)
                                    setTextColor(ContextCompat.getColor(context, R.color.black))
                                }

                            } else {
                                likedByList.add(Constants.loggedUserId)
                                binding.postLikeView.apply {
                                    text = "Liked"
                                    setTypeface(null, Typeface.BOLD)
                                    setTextColor(ContextCompat.getColor(context, R.color.primary))
                                }
                                saveNotification(post!!)
                            }
                            it.setLikedByList(likedByList)
                        }
                    } else {

                    }
                }
            }

            binding.postCommentView.setOnClickListener {
                val bottomSheet = CommentsBottomSheetFragment(post!!)
                bottomSheet.show(childFragmentManager, "CommentsBottomSheet")
            }
        }
    }

    private fun showPopupMenu(view: View, item: Post) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.post_pop_up_menu, popupMenu.menu)

        popupMenu.menu.findItem(R.id.menu_view).isVisible = false
        if (item.user?.userId != Constants.loggedUserId) {
            popupMenu.menu.findItem(R.id.menu_edit).isVisible = false
            popupMenu.menu.findItem(R.id.menu_delete).isVisible = false
        }
        else{
            popupMenu.menu.findItem(R.id.menu_edit).isVisible = true
            popupMenu.menu.findItem(R.id.menu_delete).isVisible = true
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_edit -> {
                    startActivity(Intent(requireActivity(), UpdatePostActivity::class.java).apply {
                        putExtra("POST",item)
                    })
                }
                R.id.menu_delete -> {
                    val builder = AlertDialog.Builder(requireActivity())
                    builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete?")
                        .setPositiveButton("Delete"){dialog,which->
                            dialog.dismiss()
                            postViewModel.deletePost(post!!){status,message->
                                if (status){
                                    Constants.showAlert(requireActivity(),message
                                    ) { p0, p1 -> findNavController().popBackStack()}
                                }
                                else{
                                    Constants.showAlert(requireActivity(),message)
                                }
                            }
                        }
                        .setNeutralButton("Cancel"){dialog,which->
                            dialog.dismiss()
                        }
                    val alert = builder.create()
                    alert.show()
                }

            }
            true
        }

        popupMenu.show()
    }

    private fun saveNotification(post: Post) {
        val notification = Notification(
            id = UUID.randomUUID().toString(),
            postId = post.postId,
            type = "like",
            senderId = Constants.loggedUserId,
            receiverId = post.user!!.userId,
            message = "${Constants.loggedUser?.name} liked your post.",
            timestamp = System.currentTimeMillis()
        )

        postViewModel.saveNotification(notification)
    }

}