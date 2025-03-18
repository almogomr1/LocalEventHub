package com.localeventhub.app.view.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editBtn.setOnClickListener {
            listener?.navigateToFragment(R.id.action_profile_to_editProfile)
        }

        binding.logoutBtn.setOnClickListener {
            authViewModel.logout()
            Constants.loggedUserId = ""
            Constants.loggedUser = null
            val intent = Intent(context, Authentication::class.java)
            requireActivity().startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onResume() {
        super.onResume()

        val user = Constants.loggedUser
        if (user != null){
            binding.name.setText(user.name)
            binding.email.setText(user.email)
            Picasso.get().load(user.profileImageUrl)
//                .transform(ImageTransform(user.profileImageUrl!!))
                .placeholder(R.drawable.placeholder)
                .resize(200,200)
                .centerCrop()
                .into(binding.profileImage)

        }
        else {
            binding.loadingSpinner.visibility = View.VISIBLE
            authViewModel.fetUserDetailsFromFirestore(){status,newUser ->
                binding.loadingSpinner.visibility = View.GONE
                Constants.loggedUser = newUser
                if (newUser != null){
                    binding.name.setText(newUser.name)
                    binding.email.setText(newUser.email)
                    Picasso.get().load(newUser.profileImageUrl)
//                .transform(ImageTransform(user.profileImageUrl!!))
                        .placeholder(R.drawable.placeholder)
                        .resize(200,200)
                        .centerCrop()
                        .into(binding.profileImage)

                }
            }
        }
    }
}