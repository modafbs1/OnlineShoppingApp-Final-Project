package com.moda.ShoppingApp.activities.dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.moda.ShoppingApp.R
import com.moda.ShoppingApp.fragments.admin.AddProductFragment
import com.moda.ShoppingApp.fragments.admin.AdminProductsFragment
import com.moda.ShoppingApp.fragments.admin.AdminStatsFragment
import com.moda.ShoppingApp.fragments.admin.CategoryManagementFragment
import com.moda.ShoppingApp.fragments.ProfileFragment

class AdminHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        val bottomNav = findViewById<BottomNavigationView>(R.id.adminBottomNav)
        replaceFragment(AdminProductsFragment())

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_products -> replaceFragment(AdminProductsFragment())
                R.id.nav_add -> replaceFragment(AddProductFragment())
                R.id.nav_categories -> replaceFragment(CategoryManagementFragment())
                R.id.nav_stats-> replaceFragment(AdminStatsFragment())
                R.id.nav_admin_profile -> replaceFragment(ProfileFragment())
            }
            true
        }

    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.adminFragmentContainer, fragment)
            .commit()
    }
}
