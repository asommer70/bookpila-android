package com.thehoick.bookpila

import android.app.Fragment
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.result.Result
import com.google.gson.JsonArray
import kotlinx.android.synthetic.main.activity_main.*
import nl.siegmann.epublib.epub.Main
import org.json.JSONArray
import java.net.ConnectException

class ServerBooksFragment: Fragment() {
    val TAG = ServerBooksFragment::class.java.simpleName
    var booksAdapter: BooksAdapter? = null
//    lateinit var booksList: RecyclerView
    lateinit var message: TextView
    lateinit var books: JSONArray

    fun getBooks() {

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater!!.inflate(R.layout.activity_main, container, false)

//        if (booksAdapter == null) {
//            getBooks(view)
//        } else {
//            booksList = view.findViewById<RecyclerView>(R.id.booksList)
//            booksList.adapter = booksAdapter
//        }

//        booksAdapter = BooksAdapter(getBooks(view))
//        getBooks()

        message = view.findViewById<TextView>(R.id.defaultTextView)
        message.setText(getString(R.string.getting_books))
        message.visibility = VISIBLE

        val prefs = activity.getSharedPreferences(activity.packageName + "_preferences", 0)
        val url = prefs.getString("url", "")
        val token = prefs.getString("token", "")
        Log.d(TAG, "getBooks token: $token url: $url")

        FuelManager.instance.baseHeaders = mapOf("Authorization" to "Token " + token)

        // HTTP GET /api/books
        Fuel.get(url + "/api/books").responseJson { request, response, result ->
            Log.d(TAG, "result.get().obj().get(results): ${result.get().obj().get("results")}")
//            Log.d(TAG, "result.get().obj().get(results).class: ${result.get().obj().get("results").javaClass}")

            when (result) {
                is Result.Failure -> {
//                        val ex = result.getException()
//                        Log.d(TAG, "GET /api/books ex: ${ex.message}")
//                        message.text = ex.message
                    message.text = "Network problem, or problem with the server..."
                    message.textSize = 40f
                    message.setTextColor(Color.RED)
                }
                is Result.Success -> {
                    message.visibility = INVISIBLE
                    books = result.get().obj().get("results") as JSONArray
                    val booksList = view.findViewById<RecyclerView>(R.id.booksList)
                    booksAdapter = BooksAdapter(books)
                    booksList.setLayoutManager(LinearLayoutManager(context))
                    booksList.adapter = booksAdapter
                }
            }
        }

        val localBooksButton = view.findViewById<Button>(R.id.localBooksButton)
        localBooksButton.setOnClickListener {
            Log.d(TAG, "localBooksButton onClick...")

            this.fragmentManager.popBackStackImmediate()
        }



        return view
    }
}