package com.mariofronza.face_collection_app.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.mariofronza.face_collection_app.R
import com.mariofronza.face_collection_app.databinding.ActivityMainBinding
import com.mariofronza.face_collection_app.ui.user.SignInActivity
import com.mariofronza.face_collection_app.vision

class MainActivity : AppCompatActivity() {

    private val isTeacher get() = vision.session.current?.user?.role == "teacher"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (vision.session.current == null) {
            startActivity(Intent(this, if (vision.consented) SignInActivity::class.java else IntroActivity::class.java))
            finish()
            return
        }

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        val navHost = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        if (savedInstanceState == null) {
            navHost.navController.graph = navHost.navController.navInflater.inflate(R.navigation.nav_graph).apply {
                setStartDestination(if (isTeacher) R.id.classesFragment else R.id.photosFragment)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (vision.session.current == null) signOut()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.manu_main, menu)
        menu.findItem(R.id.profile).isVisible = !isTeacher
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.profile -> {
            (supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment)
                .navController.navigate(R.id.profileFragment)
            true
        }
        R.id.exitApp -> {
            signOut()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
