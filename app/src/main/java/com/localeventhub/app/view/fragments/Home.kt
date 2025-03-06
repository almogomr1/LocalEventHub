package com.localeventhub.app.view.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.localeventhub.app.R
import com.localeventhub.app.adapters.PostAdapter
import com.localeventhub.app.databinding.HomeBinding
import com.localeventhub.app.interfaces.OnFragmentChangeListener
import com.localeventhub.app.model.Notification
import com.localeventhub.app.model.Post
import com.localeventhub.app.utils.Constants
import com.localeventhub.app.view.activities.MainActivity
import com.localeventhub.app.view.activities.UpdatePost
import com.localeventhub.app.viewmodel.PostViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@AndroidEntryPoint
class Home : Fragment() {

    private lateinit var binding: HomeBinding
    private var listener: OnFragmentChangeListener? = null

    private val postViewModel: PostViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter
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

        binding = HomeBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        binding.loadingSpinner.visibility = View.VISIBLE
        lifecycleScope.launch {
            delay(1000)
            postViewModel.posts.collect { postList ->
                if (postList.isNotEmpty()) {
                    binding.loadingSpinner.visibility = View.GONE
                    posts.clear()
                    posts.addAll(postList)
                    binding.homePostsRecyclerview.apply {
                        visibility = View.VISIBLE
                    }
                    binding.emptyPostView.apply {
                        visibility = View.GONE
                    }
                } else {
                    binding.loadingSpinner.visibility = View.GONE
                    binding.homePostsRecyclerview.apply {
                        visibility = View.GONE
                    }
                    binding.emptyPostView.apply {
                        visibility = View.VISIBLE
                    }
                }
                postAdapter.updatePosts(posts)
            }
        }

        postViewModel.loadPosts(isOnline = Constants.isOnline(requireActivity()))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        MainActivity.mainMenu = menu
        inflater.inflate(R.menu.main_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(arg0: View) {
                MainActivity.binding.appBarLayout.setBackgroundColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.primary
                    )
                )
            }

            override fun onViewAttachedToWindow(arg0: View) {
                MainActivity.binding.appBarLayout.setBackgroundColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.white
                    )
                )
            }
        })

        searchView.queryHint = "Search by keywords"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { postViewModel.searchPostsByTag(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                postViewModel.searchPostsByTag(newText ?: "")
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(mutableListOf())
        binding.homePostsRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }

        postAdapter.setOnItemClickListener(object : PostAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, post: Post) {
                val bundle = Bundle().apply {
                    putSerializable("post", post)
                }
                findNavController().navigate(R.id.action_home_to_postDetails, bundle)
            }

            override fun onItemEditClick(position: Int, post: Post) {
                startActivity(Intent(requireActivity(), UpdatePost::class.java).apply {
                    putExtra("POST", post)
                })
            }

            override fun onItemDeleteClick(position: Int, post: Post) {
                val builder = AlertDialog.Builder(requireActivity())
                builder.setTitle("Delete")
                    .setMessage("Are you sure you want to delete?")
                    .setPositiveButton("Delete") { dialog, which ->
                        dialog.dismiss()
                        postViewModel.deletePost(post) { status, message ->
                            if (status) {
                                posts.removeAt(position)
                                postAdapter.notifyItemChanged(position)
                                Constants.showAlert(requireActivity(), message)
                            } else {
                                Constants.showAlert(requireActivity(), message)
                            }
                        }
                    }
                    .setNeutralButton("Cancel") { dialog, which ->
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()
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
                                saveNotification(post)
                            }
                            it.setLikedByList(likedByList)
                        }
                        posts.removeAt(position)
                        posts.add(position, post)
                        postAdapter.notifyItemChanged(position)
                    } else {

                    }
                }
            }

            override fun onItemCommentClick(position: Int, post: Post) {
                val bottomSheet = CommentsBottomSheet(post)
                bottomSheet.show(childFragmentManager, "CommentsBottomSheet")
            }

        })
    }

    private fun saveNotification(post: Post) {
        val notification = Notification(
            id = UUID.randomUUID().toString(),
            postId = post.postId,
            type = "like",
            senderId = Constants.loggedUserId,
            receiverId = post.userId,
            message = "${Constants.loggedUser?.name} liked your post.",
            timestamp = System.currentTimeMillis()
        )

        postViewModel.saveNotification(notification)
    }


}