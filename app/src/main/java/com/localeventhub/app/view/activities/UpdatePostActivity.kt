package com.localeventhub.app.view.activities

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.localeventhub.app.R
import com.localeventhub.app.databinding.ActivityUpdatePostBinding

class UpdatePostActivity : AppCompatActivity() {

    private lateinit var binding:ActivityUpdatePostBinding
    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding = ActivityUpdatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this
    }
}