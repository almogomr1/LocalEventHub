package com.localeventhub.app.view.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import com.localeventhub.app.R
import com.localeventhub.app.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            if (authViewModel.checkUserAuth()){
                startActivity(Intent(this,MainActivity::class.java)).apply {
                    finish()
                }
            }
            else{
                startActivity(Intent(this,AuthActivity::class.java)).apply {
                    finish()
                }
            }
        },1000)
    }
}