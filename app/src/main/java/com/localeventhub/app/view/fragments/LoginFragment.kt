package com.localeventhub.app.view.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.localeventhub.app.R
import com.localeventhub.app.databinding.FragmentLoginBinding
import com.localeventhub.app.interfaces.OnFragmentChangeListener
import com.localeventhub.app.utils.Constants
import com.localeventhub.app.view.activities.MainActivity
import com.localeventhub.app.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
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
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(layoutInflater, container, false)

        binding.loginSignupText.setOnClickListener {
            listener?.navigateToFragment(R.id.action_login_to_signup,R.id.loginFragment)
        }

        binding.loginBtn.setOnClickListener {

            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()

            Constants.startLoading(requireActivity())
            authViewModel.validateAndLogin(email,password){ status,message->
                Constants.dismiss()
                if (status){
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                else{
                 Constants.showAlert(requireActivity(),message!!)
                }
            }
        }

        return binding.root
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

}