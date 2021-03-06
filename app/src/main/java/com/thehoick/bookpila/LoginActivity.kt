package com.thehoick.bookpila

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson


class LoginActivity : AppCompatActivity() {
    private val TAG = LoginActivity::class.java.simpleName
    lateinit var prefs: SharedPreferences
    private var usernameInput: EditText? = null
    private var passwordInput: EditText? = null
    private var loginButton: Button? = null
    private var statusText: TextView? = null
    private val USER_ID = "user_id"
    private val USERNAME = "username"
    private val TOKEN = "token"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val extras = intent.extras

        prefs = this.getSharedPreferences(this.packageName + "_preferences", 0)
        val url = prefs.getString("url", null)

        if (url.isNullOrEmpty()) {
            Toast.makeText(this@LoginActivity, "Please configure the URL in Settings!", Toast.LENGTH_LONG).show()
            fragmentManager.beginTransaction()
                    .addToBackStack("Settings")
                    .replace(android.R.id.content, SettingsFragment())
                    .commit()
        }

        val usernameInput = findViewById<EditText>(R.id.username)
        val passwordInput = findViewById<EditText>(R.id.password)

        val signInButton = findViewById<Button>(R.id.sign_in_button)
        signInButton.setOnClickListener {
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()

            // POST username and password to url + /api/login.
            Fuel.post(url + "/api/login", listOf("username" to username, "password" to password))
                    .responseJson { request, response, result ->
                        // Save token, id, and username to SharedPrefs and finish.
                        val editor = prefs.edit()
                        editor.putString(USERNAME, result.get().obj().get("username").toString())
                        editor.putString(TOKEN, result.get().obj().get("token").toString())
                        editor.putInt(USER_ID, result.get().obj().get("id") as Int)
                        editor.apply()

                        Toast.makeText(this@LoginActivity, result.get().obj().get("message").toString(), Toast.LENGTH_LONG).show()
                        setResult(700, intent)
                        finish()
                    }
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
        super.onBackPressed()
    }
}