package com.localeventhub.app.view.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.localeventhub.app.adapters.PostAdapter
import com.localeventhub.app.databinding.FragmentHomeBinding
import com.localeventhub.app.interfaces.OnFragmentChangeListener
import com.localeventhub.app.viewmodel.PostViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private var listener: OnFragmentChangeListener? = null

    private val postViewModel: PostViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter // Assume you have a RecyclerView adapter

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
                postAdapter.updatePosts(postList)
                binding.emptyPostView.visibility = if (postList.isEmpty()) View.VISIBLE else View.GONE
                binding.homePostsRecyclerview.visibility = if (postList.isNotEmpty()) View.VISIBLE else View.GONE
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
    }

}