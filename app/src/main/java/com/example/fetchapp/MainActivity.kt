package com.example.fetchapp

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.fetchapp.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            fetchJsonData()  // Changed to fetch JSON when FAB is clicked
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Initial data fetch
        fetchJsonData()
    }

    private fun fetchJsonData() {
        lifecycleScope.launch {
            try {
                val jsonData = withContext(Dispatchers.IO) {
                    val url = URL("https://adon1s.github.io/Tweet_Analyzer/forbes_articles.json")
                    url.readText()
                }

                // Parse as JSONArray instead of JSONObject
                val jsonArray = JSONArray(jsonData)
                val formattedJson = StringBuilder()

                // Format each article in the array
                for (i in 0 until jsonArray.length()) {
                    val article = jsonArray.getJSONObject(i)
                    formattedJson.append("Title: ${article.getString("title")}\n")
                    formattedJson.append("URL: ${article.getString("url")}\n")
                    formattedJson.append("Time: ${article.getString("timestamp")}\n\n")
                }

                // Find the TextView in nav_home fragment
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
                val currentFragment = navHostFragment?.childFragmentManager?.fragments?.get(0)
                val textView = currentFragment?.view?.findViewById<TextView>(R.id.text_home)

                // Update the TextView with formatted content
                textView?.text = formattedJson.toString()

                // Update last update time
                val currentTime = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
                    .format(Date())
                val lastUpdateView = currentFragment?.view?.findViewById<TextView>(R.id.lastUpdateText)
                lastUpdateView?.text = "Last updated: $currentTime"

                Snackbar.make(binding.root, "Articles loaded successfully", Snackbar.LENGTH_SHORT)
                    .setAnchorView(R.id.fab)
                    .show()

            } catch (e: Exception) {
                Log.e("MainActivity", "Error in fetchJsonData", e)

                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
                val currentFragment = navHostFragment?.childFragmentManager?.fragments?.get(0)
                val textView = currentFragment?.view?.findViewById<TextView>(R.id.text_home)
                textView?.text = "Error loading articles: ${e.message}"

                val lastUpdateView = currentFragment?.view?.findViewById<TextView>(R.id.lastUpdateText)
                lastUpdateView?.text = "Last update failed"

                Snackbar.make(binding.root, "Error loading data: ${e.message}", Snackbar.LENGTH_LONG)
                    .setAnchorView(R.id.fab)
                    .show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}