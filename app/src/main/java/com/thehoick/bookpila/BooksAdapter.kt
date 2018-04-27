package com.thehoick.bookpila

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.view.LayoutInflater
import org.json.JSONArray
import org.json.JSONObject


class BookViewHolder(itemView: View?): RecyclerView.ViewHolder(itemView) {
    var title: TextView? = null
    var cover: ImageView? = null
    var author: TextView? = null
    var about: TextView? = null

    init {
        title = itemView?.findViewById<TextView?>(R.id.title)
        cover = itemView?.findViewById<ImageView?>(R.id.cover)
        about = itemView?.findViewById<TextView?>(R.id.about)
        author = itemView?.findViewById<TextView?>(R.id.author)
    }
}

class BooksAdapter(): RecyclerView.Adapter<BookViewHolder>() {
    val TAG = BooksAdapter::class.java.simpleName
    var bookList: JSONArray? = null

    init {
//        bookList = books
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.book, parent, false)

        return BookViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList!!.get(position) as JSONObject
        Log.d(TAG, "onBindViewHolder book.title ${book.get("title")}")
        holder.title?.text = book.get("title").toString()
        holder.about?.text = book.get("about").toString()
        holder.author?.text = book.get("author").toString()

//        holder.about?.text = book.about
//        holder.author?.text = book.author
//        holder.cover
    }

    override fun getItemCount(): Int {
        return bookList!!.length()
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }
}