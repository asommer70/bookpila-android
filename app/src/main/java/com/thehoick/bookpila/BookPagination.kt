package com.thehoick.bookpila

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log

class BookPagination(var layoutManager: LinearLayoutManager, var totalBooks: Int): RecyclerView.OnScrollListener() {
    val TAG = BookPagination::class.java.simpleName
    var isLoading: Boolean = true
    var isLastPage: Boolean = false

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val visibleItemCount = layoutManager.getChildCount()
        val totalItemCount = layoutManager.getItemCount()
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

        Log.d(TAG, "visibleItemCount: $visibleItemCount, totalItemCount: $totalItemCount, firstVisibleItemPosition: $firstVisibleItemPosition")
        Log.d(TAG, "visibleItemCount + firstVisibleItemPosition: ${visibleItemCount + firstVisibleItemPosition}")
        Log.d(TAG, "totalItemCount: $totalItemCount, totalBooks: $totalBooks")
        if (!isLoading && !isLastPage) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount
                    && firstVisibleItemPosition >= 0
                    && totalItemCount >= totalBooks) {
                loadMoreBooks(recyclerView)
            }
        }

    }

    override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
    }

    private fun loadMoreBooks(recyclerView: RecyclerView) {
        Log.d(TAG, "Loading more books...")
        isLoading = false
        recyclerView.adapter.notifyDataSetChanged()
    }
}