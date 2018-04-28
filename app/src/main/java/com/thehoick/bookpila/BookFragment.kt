package com.thehoick.bookpila

import android.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import org.json.JSONObject

class BookFragment(): Fragment() {
    val TAG = BookFragment::class.java.simpleName
    val book = "BOOK"

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater!!.inflate(R.layout.fragment_book, container, false)

        val bookString = arguments.getString(book)
        val book = JSONObject(bookString)

        val title = view.findViewById<TextView>(R.id.detailTitle)
        val author = view.findViewById<TextView>(R.id.detailAuthor)
        val about = view.findViewById<TextView>(R.id.detailAbout)
        val cover = view.findViewById<ImageView>(R.id.detailCover)
        val download = view.findViewById<Button>(R.id.downloadButton)
        val read = view.findViewById<Button>(R.id.readButton)

        title.text = book.get("title").toString()
        author.text = "Author: ${book.get("author").toString()}"
        about.text = "About:\n ${book.get("about").toString()}"
        Glide.with(view.context).load(book.get("cover_url")).into(cover)

        // TODO:as set button visibility based on Local or Server.

        download.setOnClickListener {
            Log.d(TAG, "Download ${book.get("upload")}")
        }

        read.setOnClickListener {
            Log.d(TAG, "Read ${book.get("title")}...")
        }

        return view
    }
}