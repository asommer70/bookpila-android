package com.thehoick.bookpila

import android.app.Fragment
import android.content.Intent
import android.graphics.Color
import android.graphics.Color.*
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import com.thehoick.bookpila.models.Book
import com.thehoick.bookpila.models.BookPilaDataSource

class LocalBooksFragment(): Fragment() {
    val TAG = ServerBooksFragment::class.java.simpleName
    var booksAdapter: LocalBooksAdapter? = null
    lateinit var message: TextView
    lateinit var books: List<Book>

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater!!.inflate(R.layout.activity_main, container, false)
        view.setBackgroundColor(WHITE)

        val switch = view.findViewById<Switch>(R.id.localOrServer)
        switch.isChecked = true

        val dataSource = BookPilaDataSource(context)
        books = dataSource.getBooks()

        if (books.size == 0) {
            message.visibility = View.VISIBLE
            message.text = getString(R.string.no_local_books)
        } else {
            val booksList = view.findViewById<RecyclerView>(R.id.booksList)
            booksAdapter = LocalBooksAdapter(books)
            booksList.setLayoutManager(LinearLayoutManager(context))
            booksList.adapter = booksAdapter

//            fragmentManager.addOnBackStackChangedListener {
//                //                booksAdapter.notifyItemInserted()
//                val newBooks = dataSource.getBooks()
//
//                Log.d(TAG, "onBackStackChangedListener... newBooks.size: ${newBooks.size}")
//
//
//                booksAdapter.bookList = newBooks
//                booksAdapter.newBooks(newBooks)
//                if (newBooks.size == 0) {
//                    defaultTextView.visibility = View.VISIBLE
//                    defaultTextView.text = getString(R.string.no_local_books)
//                }
//            }
        }

        switch.setOnClickListener {
            val prefs = activity.getSharedPreferences(activity.packageName + "_preferences", 0)
            val token = prefs.getString("token", "")

            if (token.isEmpty()) {
                // Open the LoginActivity.
                val intent = Intent(context, LoginActivity::class.java)
                intent.putExtra("loginType", "books");
                startActivityForResult(intent, 200)

                val needToLoginFragment = NeedToLoginFragment()
                val fragmentTransaction = this.fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.container, needToLoginFragment, "needtologin_fragment")
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            } else {
//                buttonView.isChecked = false
//                buttonView.text = getResources().getString(R.string.server_books)
                switch.isChecked = true
                switch.text = getResources().getString(R.string.server_books)

                val serverBooksFragment = ServerBooksFragment()
                val fragmentTransaction = activity.fragmentManager.beginTransaction()

                fragmentTransaction.replace(R.id.container, serverBooksFragment, "server_books_fragment")
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            }
        }

//        switch.setOnCheckedChangeListener { buttonView, isChecked ->
//            if (isChecked) {
//                if (booksFragment.isVisible) {
//                    Log.d(TAG, "booksFragment.isVisible(): ${booksFragment.isVisible()}")
//                    buttonView.isChecked = false
//                    onBackPressed()
//                }
//
//            } else {
//                if (token.isEmpty()) {
//                    // Open the LoginActivity.
//                    val intent = Intent(this, LoginActivity::class.java)
//                    intent.putExtra("loginType", "books");
//                    startActivityForResult(intent, 200)
//
//                    val needToLoginFragment = NeedToLoginFragment()
//                    val fragmentTransaction = this.fragmentManager.beginTransaction()
//                    fragmentTransaction.replace(R.id.container, needToLoginFragment, "needtologin_fragment")
//                    fragmentTransaction.addToBackStack(null)
//                    fragmentTransaction.commit()
//                } else {
//                    buttonView.isChecked = false
//                    buttonView.text = getResources().getString(R.string.server_books)
//                    booksFragment = ServerBooksFragment()
//                    val fragmentTransaction = this.fragmentManager.beginTransaction()
//
//                    fragmentTransaction.replace(R.id.container, booksFragment, "server_books_fragment")
//                    fragmentTransaction.addToBackStack(null)
//                    fragmentTransaction.commit()
//                }
//            }

        return view
    }
}
