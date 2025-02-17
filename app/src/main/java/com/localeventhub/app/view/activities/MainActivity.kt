package com.localeventhub.app.view.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.localeventhub.app.R
import com.localeventhub.app.databinding.ActivityMainBinding
import com.localeventhub.app.interfaces.OnFragmentChangeListener
import com.localeventhub.app.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity(),OnFragmentChangeListener {


    private lateinit var context: Context
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var navController: NavController


    companion object{
         lateinit var binding:ActivityMainBinding
         var mainMenu:Menu?=null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this
        authViewModel.fetUserDetails(authViewModel.getLoggedUserId())

        setUpToolbar()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_home) as NavHostFragment

        navController = navHostFragment.navController
        setupActionBarWithNavController(navController, AppBarConfiguration(navController.graph))
        binding.bottomNavigationView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            mainMenu?.findItem(R.id.action_search)?.isVisible = destination.id == R.id.homeFragment
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
        }
        binding.fab.setOnClickListener {
           startActivity(Intent(context,CreatePostActivity::class.java))
        }

    }

    private fun setUpToolbar(){
        setSupportActionBar(binding.toolbar)
        binding.toolbar.apply {
            setTitleTextColor(ContextCompat.getColor(context,R.color.white))
        }
    }

    override fun navigateToFragment(fragmentId: Int, popUpId: Int) {
        navController.navigate(fragmentId)
    }
}