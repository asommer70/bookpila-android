package com.thehoick.bookpila

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.thehoick.bookpila.R
import com.thehoick.bookpila.SettingsFragment

//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response


class LoginActivity : AppCompatActivity() {
    private val TAG = LoginActivity::class.java.simpleName
    private var prefs: SharedPreferences? = null
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
        val url = prefs!!.getString("url", null)
        if (url.isNullOrEmpty()) {
            Toast.makeText(this@LoginActivity, "Please configure the URL in Settings!", Toast.LENGTH_LONG).show()
            fragmentManager.beginTransaction()
                    .addToBackStack("Settings")
                    .replace(android.R.id.content, SettingsFragment())
                    .commit()
        }

//        usernameInput = findViewById(R.id.usernameInput)
//        passwordInput = findViewById(R.id.passwordInput)
//        loginButton = findViewById(R.id.loginButton)
//        statusText = findViewById(R.id.statusText)

//        loginButton!!.setOnClickListener {
//            val username = usernameInput!!.text.toString()
//            val password = passwordInput!!.text.toString()

//            val api = Api(it.context)
//            val callback = object: Callback<User> {
//                override fun onFailure(call: Call<User>?, t: Throwable?) {
//                    Log.i(TAG, "A problem occurred logging in...")
//                    // Display login error message.
//                    statusText!!.text = getString(R.string.bad_username_or_password)
//                    statusText!!.visibility = View.VISIBLE
//                }
//
//                override fun onResponse(call: Call<User>?, response: Response<User>?) {
//                    // Save token, id, and username to SharedPrefs and finish.
//                    val editor = prefs!!.edit()
//                    editor.putString(USERNAME, response?.body()?.username)
//                    editor.putString(TOKEN, response?.body()?.token)
//                    if (response?.body()?.id != null) {
//                        editor.putInt(USER_ID, response.body()?.id as Int)
//                    }
//                    editor.apply()
//                    Toast.makeText(this@LoginActivity, response?.body()?.message, Toast.LENGTH_LONG).show()
//
//                    if (extras.get("loginType").equals("albums")) {
//                        setResult(700, intent)
//                    } else {
//                        setResult(800, intent)
//                    }
//                    finish()
//                }
//
//            }
//            api.login(username, password, callback)

//        }
    }

//    override fun onBackPressed() {
//        setResult(Activity.RESULT_CANCELED, intent)
//        finish()
//        super.onBackPressed()
//    }
}