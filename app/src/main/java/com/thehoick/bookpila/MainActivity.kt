package com.thehoick.bookpila

import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.folioreader.util.FolioReader
import java.io.IOException
import android.widget.Toast
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.folioreader.model.HighLight
import com.folioreader.ui.base.OnSaveHighlight
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.BufferedReader
import java.io.InputStreamReader
//import com.folioreader.util.
//import com.folioreader.util.FolioReader
import com.folioreader.util.LastReadStateCallback
import com.folioreader.util.OnHighlightListener
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.FuelManager
import org.json.JSONArray
import org.json.JSONObject


class MainActivity : AppCompatActivity(), LastReadStateCallback, MainActivityView {
    private val TAG = MainActivity::class.java.simpleName
    private var folioReader: FolioReader? = null
    lateinit var prefs: SharedPreferences
    lateinit var defaultTextView: TextView
    lateinit var token: String
    lateinit var username: String
    lateinit var presenter: MainActivityPresenter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        presenter = MainActivityPresenter(this)

        prefs = this.getSharedPreferences(this.packageName + "_preferences", 0)
        defaultTextView = this.findViewById<TextView>(R.id.defaultTextView)

        token = prefs.getString("token", "")
        if (token.isEmpty()) {
            // Open the LoginActivity.
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("loginType", "books");
            startActivityForResult(intent, 200)

            val needToLoginFragment = NeedToLoginFragment()
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.container, needToLoginFragment, "needtologin_fragment")
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        } else {
            val booksFragment = ServerBooksFragment()
            val fragmentTransaction = fragmentManager.beginTransaction()

            fragmentTransaction.replace(R.id.container, booksFragment, "books_fragment")
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

//        getBooks()

        // HTTP GET /api/books
//        val url = prefs.getString("url", "")
//        Log.d(TAG, "url: $url")
//        Fuel.get(url + "/api/books").responseString { request, response, result ->
////            Log.d(TAG, "result.get().obj().get(results): ${result.get().obj().get("results")}")
////            Log.d(TAG, "result.get().obj().get(results).class: ${result.get().obj().get("results").javaClass}")
//            Log.d(TAG, "result: ${result}")
////            return result.get().obj().get("resutls")
//        }

//        folioReader = FolioReader(this)
////        folioReader?.registerHighlightListener(this);
//        folioReader?.setLastReadStateCallback(this);
//
//        folioReader?.openBook("file:///android_asset/books/TheSilverChair.epub");
//
//        getHighlightsAndSave();
//        getLastReadPositionAndSave();
    }

    fun getBooks() {
//        val prefs = activity.getSharedPreferences(activity.packageName + "_preferences", 0)
        val url = prefs.getString("url", "")
//        val token = prefs.getString("token", "")
        Log.d(TAG, "getBooks token: $token")
        Log.d(TAG, "getBooks url: $url")

        FuelManager.instance.baseHeaders = mapOf("Authorization" to "Token " + token)

        // HTTP GET /api/books
        Fuel.get(url + "/api/books").responseJson { request, response, result ->
            Log.d(TAG, "result.get().obj().get(results): ${result.get().obj().get("results")}")
            Log.d(TAG, "result.get().obj().get(results).class: ${result.get().obj().get("results").javaClass}")
            val books = result.get().obj().get("results") as JSONArray
            val book = books[0] as JSONObject

            Log.d(TAG, "book: ${book.get("title")}")
//            for (i in 0..(books.length() - 1)) {
//                val book = books.getJSONObject(i)
//                // Your code here
//            }

//            return result.get().obj().get("resutls")
        }
    }

    private fun getLastReadPositionAndSave() {

        Thread(Runnable {
            val lastReadChapterIndex = 12
            val lastReadSpanIndex = "{\"usingId\":false,\"value\":21}"
            Log.d(TAG, "getLastReadPositionAndSave thread lastReadChapterIndex: $lastReadChapterIndex")
            folioReader?.setLastReadState(lastReadChapterIndex, lastReadSpanIndex)
        }).start()
    }

    override fun saveLastReadState(lastReadChapterIndex: Int, lastReadSpanIndex: String) {

        Toast.makeText(this, "lastReadChapterIndex = " + lastReadChapterIndex +
                ", lastReadSpanIndex = " + lastReadSpanIndex, Toast.LENGTH_SHORT).show()
        Log.d(TAG, "-> saveLastReadState -> lastReadChapterIndex = "
                + lastReadChapterIndex + ", lastReadSpanIndex = " + lastReadSpanIndex)
    }

    /*
     * For testing purpose, we are getting dummy highlights from asset. But you can get highlights from your server
     * On success, you can save highlights to FolioReader DB.
     */
    private fun getHighlightsAndSave() {
        Log.d(TAG, "highlighting...")
//        Thread(Runnable {
//            var highlightList: ArrayList<HighLight>? = null
//            val objectMapper = ObjectMapper()
//            try {
//                highlightList = objectMapper.readValue(
//                        loadAssetTextAsString("highlights/highlights_data.json"),
//                        object : TypeReference<List<HighlightData>>() {
//
//                        })
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//
//            if (highlightList == null) {
//                folioReader.saveReceivedHighLights(highlightList, OnSaveHighlight {
//                    //You can do anything on successful saving highlight list
//                })
//            }
//        }).start()
    }

    private fun loadAssetTextAsString(name: String): String? {
        var `in`: BufferedReader? = null
        try {
            val buf = StringBuilder()
            val `is` = assets.open(name)
            `in` = BufferedReader(InputStreamReader(`is`))

            var str: String
            var isFirst = true
//            while ((str = `in`!!.readLine()) != null) {
//                if (isFirst)
//                    isFirst = false
//                else
//                    buf.append('\n')
//                buf.append(str)
//            }
            return buf.toString()
        } catch (e: IOException) {
            Log.e("HomeActivity", "Error opening asset $name")
        } finally {
            if (`in` != null) {
                try {
                    `in`!!.close()
                } catch (e: IOException) {
                    Log.e("HomeActivity", "Error closing asset $name")
                }

            }
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        folioReader?.unregisterHighlightListener()
        folioReader?.removeLastReadStateCallback()
    }

    fun onHighlight(highlight: HighLight, type: HighLight.HighLightAction) {
        Toast.makeText(this,
                "highlight id = " + highlight.uuid + " type = " + type,
                Toast.LENGTH_SHORT).show()
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
