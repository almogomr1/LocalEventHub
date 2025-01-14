package com.localeventhub.app.view.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.localeventhub.app.R
import com.localeventhub.app.adapters.PostAdapter
import com.localeventhub.app.databinding.FragmentHomeBinding
import com.localeventhub.app.interfaces.OnFragmentChangeListener
import com.localeventhub.app.model.Post
import com.localeventhub.app.utils.Constants
import com.localeventhub.app.view.activities.UpdatePostActivity
import com.localeventhub.app.viewmodel.PostViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private var listener: OnFragmentChangeListener? = null

    private val postViewModel: PostViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter // Assume you have a RecyclerView adapter
    private var posts = mutableListOf<Post>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentChangeListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentChangeListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupRecyclerView()

        lifecycleScope.launch {
            postViewModel.posts.collect { postList ->
                if (postList.isNotEmpty()){
                    posts.clear()
                }
                posts.addAll(postList)
                postAdapter.updatePosts(posts)
                binding.emptyPostView.visibility = if (posts.isEmpty()) View.VISIBLE else View.GONE
                binding.homePostsRecyclerview.visibility = if (posts.isNotEmpty()) View.VISIBLE else View.GONE
            }
        }

        postViewModel.loadPosts(isOnline = true)

        return binding.root
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(mutableListOf())
        binding.homePostsRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }

       postAdapter.setOnItemClickListener(object :PostAdapter.OnItemClickListener{
           override fun onItemClick(position: Int,post: Post) {
               val bundle = Bundle().apply {
                   putSerializable("post", post)
               }
               findNavController().navigate(R.id.action_home_to_postDetails, bundle)
           }

           override fun onItemEditClick(position: Int,post: Post) {
              startActivity(Intent(requireActivity(),UpdatePostActivity::class.java).apply {
                  putExtra("POST",post)
              })
           }

           override fun onItemDeleteClick(position: Int,post: Post) {

           }

           override fun onItemLikeClick(position: Int, post: Post) {
               postViewModel.likePost(post, Constants.loggedUserId) { success, message ->
                   if (success) {
                       post.let {
                           val likedByList = it.getLikedByList().toMutableList()
                           if (likedByList.contains(Constants.loggedUserId)) {
                               likedByList.remove(Constants.loggedUserId)
                           } else {
                               likedByList.add(Constants.loggedUserId)
                           }
                          it.setLikedByList(likedByList)
                       }
                       posts.removeAt(position)
                       posts.add(position,post)
                       postAdapter.notifyItemChanged(position)
                   } else {
                       // Show an error message
                   }
               }
           }

           override fun onItemCommentClick(position: Int, post: Post) {

           }

       })
    }


}