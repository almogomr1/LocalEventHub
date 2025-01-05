package com.localeventhub.app.view.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import com.localeventhub.app.R
import com.localeventhub.app.adapters.PostAdapter.OnItemClickListener
import com.localeventhub.app.databinding.FragmentPostDetailsBinding
import com.localeventhub.app.model.Post
import com.localeventhub.app.utils.Constants
import com.localeventhub.app.utils.ExifTransformation
import com.localeventhub.app.view.activities.UpdatePostActivity
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostDetailsFragment : Fragment() {

    private lateinit var binding:FragmentPostDetailsBinding
    private var post:Post?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
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

            binding.postLikeView.setOnClickListener {

            }

            binding.postCommentView.setOnClickListener {

            }
        }
    }

    private fun showPopupMenu(view: View, item: Post) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.post_pop_up_menu, popupMenu.menu)

        // Hide "Edit" and "Delete" options based on the condition
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

                }

            }
            true
        }

        popupMenu.show()
    }

}