package com.localeventhub.app.view.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.localeventhub.app.R
import com.localeventhub.app.databinding.ActivityMainBinding
import com.localeventhub.app.interfaces.OnFragmentChangeListener
import com.localeventhub.app.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(),OnFragmentChangeListener {

    private lateinit var binding:ActivityMainBinding
    private lateinit var context: Context
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this
        authViewModel.fetUserDetails(authViewModel.getLoggedUserId())

        setUpToolbar()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_home) as NavHostFragment

        navController = navHostFragment.navController

        binding.bottomNavigationView.setupWithNavController(navController)

        binding.fab.setOnClickListener {
           startActivity(Intent(context,CreatePostActivity::class.java))
        }

    }

    private fun setUpToolbar(){
        setSupportActionBar(binding.toolbar)
       binding.toolbar.setTitleTextColor(ContextCompat.getColor(context,R.color.white))
    }

    override fun navigateToFragment(fragmentId: Int, popUpId: Int) {
        navController.navigate(fragmentId)
    }
}