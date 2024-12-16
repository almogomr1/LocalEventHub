package com.localeventhub.app.view.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.localeventhub.app.R
import com.localeventhub.app.databinding.ActivityMainBinding
import com.localeventhub.app.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    private lateinit var context: Context
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this
        authViewModel.fetUserDetails(authViewModel.getLoggedUserId())

        binding.logoutBtn.setOnClickListener {
            authViewModel.logout()
            val intent = Intent(context, AuthActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}