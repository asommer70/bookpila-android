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
        Log.d(TAG, "getBooks token: $token url: $url")

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
                    booksAdapter = BooksAdapter(books)


//                    val layoutManager: LinearLayoutManager = LinearLayoutManager(activity)
//                    val layoutManager = RecyclerView.LayoutManager()
//                    val layoutManager = view.getL
//                    booksList.setLayoutManager(layoutManager)

                    // TODO:as set totalItemCount and pos here.

                    booksList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                            // Check for down scroll.
//                            if (dy > 0) {
                            val layoutManager = booksList.layoutManager
                            var pastVisiblesItems: Int
                            var visibleItemCount: Int
                            var totalItemCount: Int
                            var loading = true
                            Log.d(TAG, "dx: $dx, dy: $dy")
                                visibleItemCount = layoutManager.getChildCount()
                                totalItemCount = layoutManager.getItemCount()
//                                pastVisiblesItems = layoutManager.findFirstVisibleItemPosition()
//                                val currentPosition = layoutManager.getPosition(layoutManager.focusedChild)
//                            recyclerView.getChildAt(0).getTop() == 0

                            val linearLayoutManager = layoutManager as LinearLayoutManager
                            val pos = linearLayoutManager.findLastVisibleItemPosition()
//                            val lastChildBottom = linearLayoutManager.findViewByPosition(pos).bottom
//                            Log.d(TAG, "pos: $pos, lastChildBottom: $lastChildBottom")
//                            if (lastChildBottom == booksList.getHeight() - booksList.getPaddingBottom() && pos == totalItemCount - 1) {
//                                Log.d(TAG, "At the bottom...")
//                            }

                            if (pos == totalItemCount - 1) {
                                val nextUrl = result.get().obj().get("next").toString()
                                Log.d(TAG, "At the bottom... nextUrl: $nextUrl")

                                if (nextUrl != "null") {
                                    // Get more books.
                                    Fuel.get(nextUrl).responseJson { request, response, result ->
                                        val newBooks = result.get().obj().get("results") as JSONArray
                                        for (i in 0..(newBooks.length() - 1)) {
                                            booksAdapter?.bookList?.put(newBooks[i])
                                        }
                                        booksAdapter?.notifyDataSetChanged()
                                    }
                                }
                            }

//                                val lastChild = booksList.getChildAt(totalItemCount)
//                                Log.d(TAG, "lastChild: $lastChild")
//                                if (lastChild != null && lastChild.isFocused) {
//                                    Log.d(TAG, "At the end...")
//                                }
//                            val pos = layoutManager.getPosition()

//                                if (loading) {
//                                    Log.d(TAG, "visibleItemCount: $visibleItemCount, pastVisiblesItems: $pastVisiblesItems, totalItemCount: $totalItemCount")
//                            Log.d(TAG, "visibleItemCount: $visibleItemCount, totalItemCount: $totalItemCount")
//                            if (visibleItemCount >= totalItemCount) {
//                                        loading = false
//                                        Log.d(TAG, "Final book...")
//
//                                    }
//                                }
//                            }
                        }
                    })

                    booksList.setLayoutManager(LinearLayoutManager(context))
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