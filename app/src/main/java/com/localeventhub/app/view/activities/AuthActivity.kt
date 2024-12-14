package com.localeventhub.app.view.activities

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.localeventhub.app.R
import com.localeventhub.app.databinding.ActivityAuthBinding
import com.localeventhub.app.view.fragments.LoginFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private lateinit var binding:ActivityAuthBinding
    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this

        val navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_auth)
            ?.let { it as androidx.navigation.fragment.NavHostFragment }
            ?.navController

    }
}