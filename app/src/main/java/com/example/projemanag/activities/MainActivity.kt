package com.example.projemanag.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.databinding.ActivityMainBinding
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.User
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()

        binding.navView.setNavigationItemSelectedListener(this)

        FirestoreClass().loadUserData(this)
    }

    private fun setupActionBar() {
        setSupportActionBar(findViewById(R.id.toolbar_main_activity))
        findViewById<Toolbar>(R.id.toolbar_main_activity).setNavigationIcon(R.drawable.ic_action_navigation_menu)
        findViewById<Toolbar>(R.id.toolbar_main_activity).setNavigationOnClickListener {
            toggleDrawer()
        }
    }
    private fun toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
              binding.drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            doubleBackToExit()
        }
    }

    fun updateNavigationUserDetails(user: User) {
        Glide
            .with(this@MainActivity)
            .load(user.image)
            .centerCrop() //fitCenter()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(findViewById(R.id.iv_user_image))

        findViewById<TextView>(R.id.tv_username).text = user.name

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile ->{
                startActivity(Intent(this, MyProfileActivity::class.java))
            }
            R.id.nav_sign_out ->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}