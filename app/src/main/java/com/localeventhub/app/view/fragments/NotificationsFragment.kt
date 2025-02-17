package com.localeventhub.app.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.localeventhub.app.adapters.NotificationAdapter
import com.localeventhub.app.databinding.FragmentNotificationsBinding
import com.localeventhub.app.model.Notification
import com.localeventhub.app.utils.Constants
import com.localeventhub.app.viewmodel.PostViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationsFragment : Fragment() {

    private lateinit var binding:FragmentNotificationsBinding
    private val postViewModel: PostViewModel by viewModels()
    private lateinit var notificationAdapter: NotificationAdapter
    private var notifications = mutableListOf<Notification>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        lifecycleScope.launch {
            delay(1000)
            postViewModel.notifications.collect { notificationList ->
                if (notificationList.isNotEmpty()) {
                    binding.loadingSpinner.visibility = View.GONE
                    notifications.clear()
                    notifications.addAll(notificationList)
                    binding.notificationsRecyclerview.apply {
                        visibility = View.VISIBLE
                    }
                    binding.emptyNotificationView.apply {
                        visibility = View.GONE
                    }
                } else {
                    binding.loadingSpinner.visibility = View.GONE
                    binding.notificationsRecyclerview.apply {
                        visibility = View.GONE
                    }
                    binding.emptyNotificationView.apply {
                        visibility = View.VISIBLE
                    }
                }
                notificationAdapter.updateNotifications(notifications)
            }
        }

        postViewModel.loadNotifications(isOnline = true,Constants.loggedUserId)
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(mutableListOf())
        binding.notificationsRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
        }


    }

}