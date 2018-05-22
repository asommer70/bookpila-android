package com.thehoick.bookpila

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import org.json.JSONArray

class BookPagination(var layoutManager: LinearLayoutManager, var totalBooks: Int, var nextUrl: String, var adapter: BooksAdapter): RecyclerView.OnScrollListener() {
    val TAG = BookPagination::class.java.simpleName
    var isLoading: Boolean = false

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val visibleItemCount = layoutManager.getChildCount()
        val totalItemCount = layoutManager.getItemCount()
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

        if (!isLoading && nextUrl != "null") {

            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount &&
                totalItemCount >= totalBooks && firstVisibleItemPosition >= totalBooks - 2) {
                loadMoreBooks(recyclerView)
            }
        }

    }

    override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
    }

    private fun loadMoreBooks(recyclerView: RecyclerView) {
        isLoading = true

        if (nextUrl != "null") {
            // Get more books.
            Fuel.get(nextUrl).responseJson { request, response, result ->
                val newBooks = result.get().obj().get("results") as JSONArray
                val newBookList = JSONArray()

                nextUrl = result.get().obj().get("next").toString()

                Log.d(TAG, "oldBookList: ${recyclerView.adapter }")
                for (i in 0..(adapter.bookList!!.length() - 1)) {
                    newBookList.put(adapter.bookList!![i])
                }
                for (i in 0..(newBooks.length() - 1)) {
                    newBookList.put(newBooks[i])
                }

                adapter.bookList = newBookList
                Log.d(TAG, "booksAdapter?.bookList.length(): ${adapter.bookList?.length()}")
                adapter.notifyDataSetChanged()
                isLoading = false
            }
        }
    }
}