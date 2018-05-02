package com.thehoick.bookpila

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        return view
    }
}
