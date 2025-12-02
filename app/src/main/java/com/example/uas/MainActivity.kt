package com.example.uas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Temporary: Auto-launch Register for testing if needed, or add a button.
        // For now, let's add a button in the layout.

        // Initialize with HomeFragment or requested fragment
        if (savedInstanceState == null) {
            val navigateTo = intent.getStringExtra("NAVIGATE_TO")
            if (navigateTo == "ORDERS") {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, OrderFragment())
                    .commit()
                findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.nav_view).selectedItemId = R.id.navigation_orders
            } else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()
            }
        }

        val navView = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.nav_view)
        navView.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.navigation_home -> HomeFragment()
                R.id.navigation_cart -> CartFragment()
                R.id.navigation_orders -> OrderFragment()
                R.id.navigation_account -> ProfileFragment()
                else -> null
            }

            if (fragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit()
                true
            } else {
                false
            }
        }
    }
}