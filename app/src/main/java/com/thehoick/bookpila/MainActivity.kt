package com.thehoick.bookpila

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.folioreader.model.HighLight
import com.folioreader.util.FolioReader
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.result.Result
import com.thehoick.bookpila.models.BookPilaDataSource
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*


class MainActivity : AppCompatActivity(), MainActivityView {
    private val TAG = MainActivity::class.java.simpleName
    private var folioReader: FolioReader? = null
    lateinit var prefs: SharedPreferences
    lateinit var defaultTextView: TextView
    lateinit var token: String
    lateinit var url: String
    lateinit var username: String
    lateinit var presenter: MainActivityPresenter
    lateinit var booksList: RecyclerView
    lateinit var dataSource: BookPilaDataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        presenter = MainActivityPresenter(this)

        prefs = this.getSharedPreferences(this.packageName + "_preferences", 0)
        defaultTextView = this.findViewById<TextView>(R.id.defaultTextView)
        token = prefs.getString("token", "")
        url = prefs.getString("url", "")
        dataSource = BookPilaDataSource(this)

        // Open the last Book if it's in SharedPrefs.
        val lastTitle = prefs.getString("last_book", "")
        if (!lastTitle.isEmpty()) {
            val localBook = dataSource.getBook(lastTitle)
            localBook?.read(this)
        }

        val books = dataSource.getBooks()

        // Return savedFragment, or create a new localBooksFragment.
        val savedFragment = fragmentManager.findFragmentById(R.id.container)
        if (savedFragment == null) {
            val localBooksFragment = LocalBooksFragment()
            val fragmentTransaction = fragmentManager.beginTransaction()

            fragmentTransaction.replace(R.id.container, localBooksFragment, "local_books_fragment")
            fragmentTransaction.commit()
        } else {

        }

        // Uncomment for testing.
//        addFirst()

        // Add The Sign of Four to the localBooks.
        val isFirstRun = prefs.getBoolean("FIRSTRUN", true)
        if (isFirstRun) {
            // Code to run once
            val editor = prefs.edit()
            editor.putBoolean("FIRSTRUN", false)
            editor.apply()

            val defaultBook = JSONObject(mutableMapOf(
                    "id" to 0,
                    "title" to "The Sign of Four",
                    "author" to "Sir Arthur Conan Doyle",
                    "about" to "The story is set in 1888. The Sign of the Four has a complex plot involving service in India, the Indian Rebellion of 1857, a stolen treasure, and a secret pact among four convicts ('the Four' of the title) and two corrupt prison guards. It presents the detective's drug habit and humanizes him in a way that had not been done in the preceding novel, A Study in Scarlet (1887). It also introduces Doctor Watson's future wife, Mary Morstan.",
                    "isbn" to "",
                    "upload" to "the_sign_of_four.epub",
                    "current_loc" to "",
                    "current_loc_folio" to "",
                    "cover" to "",
                    "cover_image" to "",
                    "cover_url" to "/android_asset/books/The_Sign_of_Four_cover_1892.jpg",
                    "local_filename" to "the_sign_of_four.epub",
                    "local_path" to "/android_asset/books/the_sign_of_four.epub",
                    "local_cover" to "/android_asset/books/The_Sign_of_Four_cover_1892.jpg",
                    "created_at" to "",
                    "updated_at" to ""

            ))
            dataSource.createBook(defaultBook)
        }

    }

    fun addFirst() {
        val ed = prefs.edit()
        ed.putBoolean("FIRSTRUN", true)
        ed.commit()
        dataSource.deleteBook("The Sign of Four")
    }

    override fun onDestroy() {
        super.onDestroy()
        folioReader?.unregisterHighlightListener()
        folioReader?.removeLastReadStateCallback()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.settings -> {
            // Open the Settings fragment.
//            fragmentManager.beginTransaction()
//                    .addToBackStack("Settings")
//                    .replace(android.R.id.content, SettingsFragment())
//                    .commit()
            presenter.openSettingsFragment()
            true
        }
        R.id.login -> {
            // Open the LoginActivity.
//            val intent = Intent(this, LoginActivity::class.java)
//            intent.putExtra("loginType", "photos");
//            startActivityForResult(intent, 100)
            presenter.launchLoginActivity(LoginActivity::class.java)
            true
        }

        R.id.logout -> {
            // Clear token, user_id, and username from SharedPrefs, and finish the Activity.
            val editor = prefs.edit()
            editor.putString("username", null)
            editor.putString("token", null)
            editor.putInt("user_id", 0)
            editor.apply()
            finish()
            true
        }

        R.id.sync -> {
            // Get local books.
            val localBooks = dataSource.getBooks()

            // Get Server Books.
            if (!url.isEmpty()) {
                FuelManager.instance.baseHeaders = mapOf("Authorization" to "Token " + token)
                Fuel.get(url + "/api/books").responseJson { request, response, result ->
                    when (result) {
                        is Result.Failure -> {
                            val ex = result.getException()
                            Log.d(TAG, "Sync failed exception message: ${ex.message}")
                        }
                        is Result.Success -> {
                            val serverBooks = result.get().obj().get("results") as JSONArray

                            // Loop through the localBooks.
                            for (localBook in localBooks) {
                                // Check if the localBook is in the serverBooks.
                                for (idx in 0..(serverBooks.length() - 1)) {
                                    val serverBook = serverBooks[idx] as JSONObject
                                    if (serverBook.get("title").toString().equals(localBook.title)) {
                                        // If the localBook is in serverBooks check updated_at.
                                        if (!localBook.current_loc_folio.equals(serverBook.get("current_loc_folio").toString())) {
                                            val localUpdatedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(localBook.updated_at)
                                            val serverUpdatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(serverBook.get("updated_at").toString())
                                            val diff = Math.abs(localUpdatedAt.getTime()/60000 - serverUpdatedAt.getTime()/60000)

                                            // If updated_at is newer than the localBook update the localBook.updated_at and localBook.current_loc_folio
                                            if (diff > 1) {
                                                localBook.updated_at = serverBook.get("updated_at").toString()
                                                localBook.current_loc_folio = serverBook.get("current_loc_folio").toString()
                                                dataSource.updateBook(localBook)
                                            } else {
                                                // Update Book on Server.
                                                Fuel.put(
                                                        "$url/api/books/${localBook.id}",
                                                        localBook.toList()
                                                ).response { request, response, result ->
                                                    when (result) {
                                                        is Result.Failure -> {
                                                            val ex = result.getException()
                                                            Log.d(TAG, "Book: ${localBook.title} NOT updated... ${ex.message}")
                                                        }
                                                        is Result.Success -> {
                                                            val data = result.get()
                                                            Log.d(TAG, "Book: ${localBook.title} updated... ${response.statusCode}")
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)

        token = prefs.getString("token", "")
        username = prefs.getString("username", "")

        // Add welcome message to the menu and logout item if token and username SharedPrefs are set.
        if (!token.equals("") && !username.equals("")) {
            menu.clear()
            menu.add(0, R.id.username, Menu.NONE, "Welcome, " + username)
            menu.add(0, R.id.sync, Menu.NONE, "Sync")
            menu.add(0, R.id.logout, Menu.NONE, "Logout")
            menu.add(0, R.id.settings, Menu.NONE, "Settings")
        } else {
            menu.removeItem(R.id.username)
            menu.removeItem(R.id.logout)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun openSettingsFragment() {
        fragmentManager.beginTransaction()
                .addToBackStack("Settings")
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }

    override fun launchLoginActivity(activity: Class<LoginActivity>) {
        val intent = Intent(this, activity)
        intent.putExtra("loginType", "photos");
        startActivityForResult(intent, 100)
    }

//    override fun onPause() {
//        super.onPause()
//
//        val editor = prefs.edit()
//        editor.putString("lastActivity", javaClass.name)
//        editor.apply()
//    }

}
