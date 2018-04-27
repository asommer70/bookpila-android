package com.thehoick.bookpila

import android.app.Fragment
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.FuelManager
import com.google.gson.JsonArray
import nl.siegmann.epublib.epub.Main
import org.json.JSONArray

class ServerBooksFragment: Fragment() {
    val TAG = ServerBooksFragment::class.java.simpleName
    lateinit var booksAdapter: BooksAdapter
//    lateinit var booksList: RecyclerView
    lateinit var books: JSONArray

    fun getBooks() {
        val prefs = activity.getSharedPreferences(activity.packageName + "_preferences", 0)
        val url = prefs.getString("url", "")
        val token = prefs.getString("token", "")
        Log.d(TAG, "getBooks token: $token")

        FuelManager.instance.baseHeaders = mapOf("Authorization" to "Token " + token)

        // HTTP GET /api/books
        Fuel.get(url + "/api/books").responseJson { request, response, result ->
            Log.d(TAG, "result.get().obj().get(results): ${result.get().obj().get("results")}")
//            Log.d(TAG, "result.get().obj().get(results).class: ${result.get().obj().get("results").javaClass}")
            books = result.get().obj().get("results") as JSONArray
            booksAdapter.bookList = books
//            return result.get().obj().get("resutls")
        }
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
        getBooks()

        val booksList = view.findViewById<RecyclerView>(R.id.booksList)
        booksAdapter = BooksAdapter()
        booksList.adapter = booksAdapter
        Log.d(TAG, "booksAdapter: $booksAdapter")

        return view
    }
}