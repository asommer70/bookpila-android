package com.thehoick.bookpila

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.view.LayoutInflater
import com.bumptech.glide.Glide
import org.json.JSONArray
import org.json.JSONObject
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import java.io.Serializable


class BookViewHolder(itemView: View?): RecyclerView.ViewHolder(itemView) {
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

class BooksAdapter(books: JSONArray): RecyclerView.Adapter<BookViewHolder>() {
    val TAG = BooksAdapter::class.java.simpleName
    var bookList: JSONArray? = null

    init {
        bookList = books
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false)

//        itemView.setOnClickListener {
//
//        }

        return BookViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList!!.get(position) as JSONObject
        holder.title?.text = book.get("title").toString()
        holder.author?.text = "Author: ${book.get("author").toString()}"
        holder.about?.text = "About:\n ${book.get("about").toString()}"

        val cover = book.get("cover").toString()
        val coverImage = book.get("cover_image").toString()
        Glide.with(holder.itemView.context).load(book.get("cover_url")).into(holder.cover!!)

        holder.itemView.setOnClickListener {

//            val intent = Intent(it.context, BookActivity::class.java)
//            intent.putExtra("title", book.get("title").toString())
//            intent.putExtra("only_book", false.toString())
////            it.startActivityForResult(intent, 100)
//            it.context.startActivity(intent)

            val bookFragment = BookFragment()
            val manager = (it.context as Activity).fragmentManager
            val fragmentTransaction = manager.beginTransaction()

            val bundle = Bundle()
            bundle.putString(bookFragment.book, book.toString())
            bookFragment.arguments = bundle

            fragmentTransaction.replace(R.id.container, bookFragment, "book_fragment")
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
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