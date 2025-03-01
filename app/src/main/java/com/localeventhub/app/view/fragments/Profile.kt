package com.localeventhub.app.view.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.localeventhub.app.R
import com.localeventhub.app.databinding.ProfileBinding
import com.localeventhub.app.interfaces.OnFragmentChangeListener
import com.localeventhub.app.utils.Constants
import com.localeventhub.app.utils.ImageTransform
import com.localeventhub.app.view.activities.Authentication
import com.localeventhub.app.viewmodel.AuthViewModel
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Profile : Fragment() {

    private lateinit var binding:ProfileBinding
    private val authViewModel: AuthViewModel by viewModels()
    private var listener: OnFragmentChangeListener? = null

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
        binding =  ProfileBinding.inflate(inflater, container, false)

        val user = Constants.loggedUser
        if (user != null){
            Picasso.get().load(user.profileImageUrl)
                .transform(ImageTransform(user.profileImageUrl!!))
                .placeholder(R.drawable.placeholder)
                .into(binding.profileImage)
            binding.name.setText(user.name)
            binding.email.setText(user.email)
        }

        binding.editBtn.setOnClickListener {
            listener?.navigateToFragment(R.id.action_profile_to_editProfile)
        }

        binding.logoutBtn.setOnClickListener {
            authViewModel.logout()
            val intent = Intent(context, Authentication::class.java)
            requireActivity().startActivity(intent)
            requireActivity().finish()
        }

        return binding.root
    }

}