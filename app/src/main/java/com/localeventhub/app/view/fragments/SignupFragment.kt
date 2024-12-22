package com.localeventhub.app.view.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.localeventhub.app.R
import com.localeventhub.app.databinding.FragmentSignupBinding
import com.localeventhub.app.interfaces.OnFragmentChangeListener
import com.localeventhub.app.model.User
import com.localeventhub.app.utils.Constants
import com.localeventhub.app.view.activities.MainActivity
import com.localeventhub.app.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignupFragment : Fragment() {

    private lateinit var binding: FragmentSignupBinding
    private val authViewModel: AuthViewModel by viewModels()
    private var listener: OnFragmentChangeListener? = null
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var selectedImageUri: Uri? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentChangeListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentChangeListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                binding.profileImage.setImageURI(uri)
            } else {
                Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSignupBinding.inflate(inflater, container, false)

        binding.signupLoginText.setOnClickListener {
            listener?.navigateToFragment(R.id.action_signup_to_login, R.id.signupFragment)
        }

        binding.signupBtn.setOnClickListener {
            val name = binding.name.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()
            val confirmPassword = binding.confirmPassword.text.toString().trim()

            Constants.startLoading(requireActivity())
            authViewModel.validateAndSignUp(
                selectedImageUri,
                name,
                email,
                password, confirmPassword
            ) { status, message ->
                Constants.dismiss()
                if (status) {
                    val user = User(name = name,email=email)
                    authViewModel.saveUserData(user,selectedImageUri)
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    Constants.showAlert(requireActivity(), message!!)
                }
            }
        }

        binding.uploadImageBtn.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        return binding.root
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

}