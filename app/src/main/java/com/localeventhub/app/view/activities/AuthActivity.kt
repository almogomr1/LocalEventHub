package com.localeventhub.app.view.activities

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.localeventhub.app.R
import com.localeventhub.app.databinding.ActivityAuthBinding
import com.localeventhub.app.interfaces.OnFragmentChangeListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppCompatActivity(), OnFragmentChangeListener {

    private lateinit var binding:ActivityAuthBinding
    private lateinit var context: Context
    private var navController: NavController?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this


        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_auth) as NavHostFragment

        navController = navHostFragment.navController

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController!!.navigateUp() || super.onSupportNavigateUp()
    }

    override fun navigateToFragment(fragmentId: Int, popUpId: Int) {
            navController!!.navigate(fragmentId)
    }
}