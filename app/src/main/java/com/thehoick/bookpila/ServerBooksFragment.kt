package com.thehoick.bookpila

import android.app.Fragment
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Color.*
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
import org.json.JSONObject
import java.net.ConnectException

class ServerBooksFragment: Fragment() {
    val TAG = ServerBooksFragment::class.java.simpleName
    var booksAdapter: BooksAdapter? = null
    lateinit var message: TextView
    lateinit var books: JSONArray

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater!!.inflate(R.layout.activity_main, container, false)
        view.setBackgroundColor(WHITE)

        val switch = view.findViewById<Switch>(R.id.localOrServer)
        switch.text = getResources().getString(R.string.server_books)
        switch.isChecked = false


        message = view.findViewById<TextView>(R.id.defaultTextView)
        message.setText(getString(R.string.getting_books))
        message.visibility = VISIBLE

        val prefs = activity.getSharedPreferences(activity.packageName + "_preferences", 0)
        val url = prefs.getString("url", "")
        val token = prefs.getString("token", "")

        FuelManager.instance.baseHeaders = mapOf("Authorization" to "Token " + token)

        // HTTP GET /api/books
        Fuel.get(url + "/api/books").responseJson { request, response, result ->
            when (result) {
                is Result.Failure -> {
                    message.text = getString(R.string.server_network_problem)
                    message.textSize = 40f
                    message.setTextColor(RED)
                }
                is Result.Success -> {
                    message.visibility = INVISIBLE
                    books = result.get().obj().get("results") as JSONArray
                    val booksList = view.findViewById<RecyclerView>(R.id.booksList)
                    val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    booksList.layoutManager = linearLayoutManager
                    booksAdapter = BooksAdapter(books)

                    val nextUrl = result.get().obj().get("next").toString()

                    booksList.addOnScrollListener(
                            BookPagination(
                                linearLayoutManager,
                                books.length(),
                                nextUrl,
                                booksAdapter!!
                        )
                    )
                    booksList.adapter = booksAdapter
                }
            }
        }

        switch.setOnClickListener {
            switch.text = getResources().getString(R.string.local_books)
            switch.isChecked = false
            this.fragmentManager.popBackStackImmediate()
        }

        return view
    }
}