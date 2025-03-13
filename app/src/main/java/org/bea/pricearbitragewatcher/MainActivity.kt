package org.bea.pricearbitragewatcher

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainActivityViewModel by viewModels()

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: MaterialToolbar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        val navController = navHostFragment?.navController
            ?: throw IllegalStateException("NavController not found!")

        this.title = getString(R.string.app_title)

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        toolbar = findViewById(R.id.toolbar)

        // Set up toolbar
        setSupportActionBar(toolbar)

        // Add navigation drawer toggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        toggle.drawerArrowDrawable.color = ContextCompat.getColor(this, R.color.colorOnPrimary)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Handle navigation menu item clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    //Toast.makeText(this, getString(R.string.home_selected), Toast.LENGTH_SHORT).show()
                    navController.navigate(R.id.exchangeArbitrageFragment)
                }
                R.id.nav_monitor -> {
                    //Toast.makeText(this, getString(R.string.home_selected), Toast.LENGTH_SHORT).show()
                    navController.navigate(R.id.priceMonitorFragment)
                }
                R.id.nav_settings -> {
                    //Toast.makeText(this, getString(R.string.settings_selected), Toast.LENGTH_SHORT).show()
                    navController.navigate(R.id.currencyPairSelectionFragment)
                }
                R.id.nav_about -> {
                    //Toast.makeText(this,getString(R.string.about_selected), Toast.LENGTH_SHORT).show()
                    navController.navigate(R.id.aboutFragment)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

//        val fab = findViewById<FloatingActionButton>(R.id.fab)
//        fab.setOnClickListener {
//            // Обработка нажатия на кнопку
//            Toast.makeText(
//                this,
//                getString(R.string.write_new_message),
//                Toast.LENGTH_SHORT
//            ).show()
//            // Здесь вы можете добавить логику для открытия нового экрана или действия
//        }

            //        viewModel.checkAndUpdateMarkets()

    }


}




