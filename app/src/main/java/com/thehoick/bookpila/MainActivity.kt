package com.thehoick.bookpila

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.folioreader.model.HighLight
import com.folioreader.util.FolioReader
import com.thehoick.bookpila.models.BookPilaDataSource
import org.json.JSONObject


class MainActivity : AppCompatActivity(), MainActivityView {
    private val TAG = MainActivity::class.java.simpleName
    private var folioReader: FolioReader? = null
    lateinit var prefs: SharedPreferences
    lateinit var defaultTextView: TextView
    lateinit var token: String
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

        dataSource = BookPilaDataSource(this)
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

}
