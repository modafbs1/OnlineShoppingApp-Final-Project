package com.moda.ShoppingApp.activities.dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.moda.ShoppingApp.R
import com.moda.ShoppingApp.fragments.user.CartFragment
import com.moda.ShoppingApp.fragments.user.HomeFragment
import com.moda.ShoppingApp.fragments.user.FavoritesFragment
import com.moda.ShoppingApp.fragments.ProfileFragment

class UserMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.userBottomNav)

        replaceFragment(HomeFragment()) // البداية بالمنتجات

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_fav -> replaceFragment(FavoritesFragment())
                R.id.nav_cart -> replaceFragment(CartFragment())
                R.id.nav_profile -> replaceFragment(ProfileFragment())
            }

            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.userFragmentContainer, fragment)
            .commit()
    }
}
