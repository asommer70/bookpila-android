package com.thehoick.bookpila

import android.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.folioreader.util.FolioReader
import com.github.kittinunf.fuel.Fuel
import org.json.JSONObject
import java.io.File

class BookFragment(): Fragment() {
    val TAG = BookFragment::class.java.simpleName
    val book = "BOOK"

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater!!.inflate(R.layout.fragment_book, container, false)

        val prefs = activity.getSharedPreferences(activity.packageName + "_preferences", 0)
        val localDir = prefs.getString("local_dir", "")

        val bookString = arguments.getString(book)
        val book = JSONObject(bookString)
        val fileName = book.get("upload").toString().split("/").last().split(".").first()

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

            Log.d(TAG, "localDir: $localDir fileName: $fileName")

            Fuel.download(book.get("upload").toString()).destination { response, url ->
                File.createTempFile(fileName, ".epub", File(localDir))
            }.response { req, res, result ->
                Log.d(TAG, "file downloaded...")

                // TODO:as save book data into a local SQL database.
            }
        }

        read.setOnClickListener {
            Log.d(TAG, "Read ${book.get("title")}...")

            val folioReader = FolioReader(activity)
            //        folioReader?.registerHighlightListener(this)z
//            folioReader.setLastReadStateCallback(activity)

            folioReader.openBook(File("$localDir/$fileName.epub").toString())

//            getHighlightsAndSave();
//            getLastReadPositionAndSave();
        }

        return view
    }
}