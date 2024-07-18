package com.groupf.togolist

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.groupf.driverapp.Model.UserInfoModel
import com.groupf.togolist.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarHome.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        updateNavHeader(navView)  // Call to update the navigation header.
    }

    private fun updateNavHeader(navView: NavigationView) {
        val headerView = navView.getHeaderView(0)
        val tvHeaderTitle = headerView.findViewById<TextView>(R.id.tv_header_title)
        val tvHeaderSubtitle = headerView.findViewById<TextView>(R.id.tv_header_subtitle)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Reference to the user info in the database
            val userInfoRef = FirebaseDatabase.getInstance().getReference("UserInfo").child(currentUser.uid)
            userInfoRef.addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val userInfo = dataSnapshot.getValue(UserInfoModel::class.java)
                        tvHeaderTitle.text = "Welcome, ${userInfo?.firstName}"
                        tvHeaderSubtitle.text = "Logged in as ${
                            if (currentUser.email.isNullOrEmpty())
                                currentUser.phoneNumber.takeUnless { it.isNullOrEmpty() } ?: "No Email nor Phone"
                            else
                                currentUser.email
                        }"
                    } else {
                        tvHeaderTitle.text = "Welcome, User"
                        tvHeaderSubtitle.text = "No additional user info"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("Database Error", databaseError.message)
                }
            })
        } else {
            tvHeaderTitle.text = "Welcome, Guest"
            tvHeaderSubtitle.text = "Please log in"
        }
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // Handle settings action here
                true
            }
            R.id.action_logout -> {
                // Handle logout action here
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        // Perform Firebase logout
        FirebaseAuth.getInstance().signOut()
        // Perform logout operations (e.g., clearing user data, navigating to login screen)
        val intent = Intent(this, SplashScreenActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
