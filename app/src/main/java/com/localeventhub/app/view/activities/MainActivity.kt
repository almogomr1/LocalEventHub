package com.localeventhub.app.view.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.localeventhub.app.R
import com.localeventhub.app.databinding.ActivityMainBinding
import com.localeventhub.app.interfaces.OnFragmentChangeListener
import com.localeventhub.app.view.fragments.Home
import com.localeventhub.app.viewmodel.AuthViewModel
import com.localeventhub.app.viewmodel.PostViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity(),OnFragmentChangeListener {


    private lateinit var context: Context
    private val authViewModel: AuthViewModel by viewModels()
    private val postViewModel: PostViewModel by viewModels()
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

        setUpToolbar()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_home) as NavHostFragment

        navController = navHostFragment.navController
        setupActionBarWithNavController(navController, AppBarConfiguration(navController.graph))
        binding.bottomNavigationView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            mainMenu?.findItem(R.id.action_search)?.isVisible = destination.id == R.id.homeFragment
            binding.tagsFilterWrapper.isVisible = destination.id == R.id.homeFragment
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
        }
        binding.fab.setOnClickListener {
           startActivity(Intent(context,CreatePost::class.java))
        }


    }

    private suspend fun setupTagSpinner() {
        val tagList = mutableListOf<String>()
        tagList.add("All")
        postViewModel.tags.observe(this@MainActivity) { tags ->
            // Convert to list
            val list = tags.toList()

            // Flatten and split any comma-separated tags, then trim and filter out empty ones
            val allTags = list.flatMap { it.split(",") }
                .map { it.replace("[","")
                    .replace("]","")
                    .replace("\"", "")
                    .trim() }
                .filter { it.isNotEmpty() }
                .distinct()  // Make sure all tags are distinct

            Log.d("Tags", "All unique tags: $allTags")
            tagList.addAll(allTags)
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tagList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            binding.tagSpinner.apply {
                this.adapter = adapter
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val selectedTag = tagList[position]
                        // Send to Home Fragment
                    sendSelectedTagToFragment(selectedTag)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }
        }

    }

    private fun sendSelectedTagToFragment(selectedTag: String) {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_home) as NavHostFragment
        val homeFragment = navHostFragment.childFragmentManager.fragments.firstOrNull { it is Home } as? Home
        homeFragment?.apply {
            updateSelectedTag(selectedTag)
        }
    }

    override fun onResume() {
        super.onResume()
        authViewModel.fetUserDetails()
        lifecycleScope.launch {
            delay(5000)
            setupTagSpinner()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
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