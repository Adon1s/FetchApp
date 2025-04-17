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
import org.json.JSONObject
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
            fetchJsonData()
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

        fetchJsonData()
    }

    private fun fetchJsonData() {
        lifecycleScope.launch {
            try {
                val jsonData = withContext(Dispatchers.IO) {
                    val url = URL("https://fetch-hiring.s3.amazonaws.com/hiring.json")
                    url.readText()
                }

                val jsonArray = JSONArray(jsonData)

                val groupedItems = mutableMapOf<Int, MutableList<JSONObject>>()

                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)

                    if (item.isNull("name") || item.getString("name").isEmpty()) {
                        continue
                    }

                    val listId = item.getInt("listId")

                    if (!groupedItems.containsKey(listId)) {
                        groupedItems[listId] = mutableListOf()
                    }

                    groupedItems[listId]?.add(item)
                }

                val formattedJson = StringBuilder()

                groupedItems.keys.sorted().forEach { listId ->
                    formattedJson.append("List $listId:\n")
                    formattedJson.append("-------------------\n")

                    val sortedItems = groupedItems[listId]?.sortedBy {
                        it.getInt("id")
                    } ?: emptyList()

                    sortedItems.forEach { item ->
                        formattedJson.append("ID: ${item.getInt("id")}\n")
                        formattedJson.append("Name: ${item.getString("name")}\n\n")
                    }
                }

                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
                val currentFragment = navHostFragment?.childFragmentManager?.fragments?.get(0)
                val textView = currentFragment?.view?.findViewById<TextView>(R.id.text_home)

                textView?.text = formattedJson.toString()

                // Update last update time
                val currentTime = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
                    .format(Date())
                val lastUpdateView = currentFragment?.view?.findViewById<TextView>(R.id.lastUpdateText)
                lastUpdateView?.text = "Last updated: $currentTime"

                Snackbar.make(binding.root, "Items loaded successfully", Snackbar.LENGTH_SHORT)
                    .setAnchorView(R.id.fab)
                    .show()

            } catch (e: Exception) {
                Log.e("MainActivity", "Error in fetchJsonData", e)

                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
                val currentFragment = navHostFragment?.childFragmentManager?.fragments?.get(0)
                val textView = currentFragment?.view?.findViewById<TextView>(R.id.text_home)
                textView?.text = "Error loading items: ${e.message}"

                val lastUpdateView = currentFragment?.view?.findViewById<TextView>(R.id.lastUpdateText)
                lastUpdateView?.text = "Last update failed"

                Snackbar.make(binding.root, "Error loading data: ${e.message}", Snackbar.LENGTH_LONG)
                    .setAnchorView(R.id.fab)
                    .show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}