package com.thehoick.bookpila

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.thehoick.bookpila.models.Book

class LocalBookViewHolder(itemView: View?): RecyclerView.ViewHolder(itemView) {
    val TAG = BookViewHolder::class.java.simpleName
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

class LocalBooksAdapter(books: List<Book>): RecyclerView.Adapter<LocalBookViewHolder>() {
    val TAG = BooksAdapter::class.java.simpleName
    var bookList: List<Book>? = null

    init {
        bookList = books
    }

    fun newBooks(books: List<Book>) {
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalBookViewHolder {
        val itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false)
        return LocalBookViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: LocalBookViewHolder, position: Int) {
        val book = bookList!![position]
        holder.title?.text = book.title
        holder.author?.text = "Author: ${book.author}"
        holder.about?.text = "About:\n ${book.about}"

        val cover = book.cover
        val coverImage = book.cover_image
        Glide.with(holder.itemView.context).load(book.cover_url).into(holder.cover!!)

        holder.itemView.setOnClickListener {
            val bookFragment = BookFragment()
            val manager = (it.context as Activity).fragmentManager
            val fragmentTransaction = manager.beginTransaction()

            val bundle = Bundle()
            bundle.putString(bookFragment.book, book.toString())
            bundle.putBoolean(bookFragment.only_book, if (bookList!!.size == 1) true else false)
            bookFragment.arguments = bundle

            fragmentTransaction.replace(R.id.container, bookFragment, "book_fragment")
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()

            // TODO:as get result from fragment and update adapter.
        }
    }

    override fun getItemCount(): Int {
        return bookList!!.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun onBindViewHolder(holder: LocalBookViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }


}