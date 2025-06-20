package com.ai.app.audio_ai.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.ai.app.audio_ai.R
import com.ai.app.audio_ai.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        // Check if user is authenticated
        val isAuthenticated = false // TODO: Replace with actual auth check
        
        if (!isAuthenticated) {
            // Navigate to register screen if not authenticated
            navController.navigate(R.id.registerFragment)
        } else {
            // Only setup bottom navigation after authentication
            binding.bottomNavigation.setupWithNavController(navController)
        }
    }
}
