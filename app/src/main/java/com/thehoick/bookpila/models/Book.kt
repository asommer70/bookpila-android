package com.thehoick.bookpila.models

import android.content.Context
import android.util.Log
import com.folioreader.Config
import com.folioreader.Constants
import com.folioreader.util.FolioReader
import com.folioreader.util.LastReadStateCallback
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.result.Result
import com.mcxiaoke.koi.ext.getActivityManager
import com.thehoick.bookpila.R
import org.json.JSONObject
import java.sql.Timestamp
import android.net.NetworkInfo
import android.net.ConnectivityManager



class Book (
        val _id: Int?,
        val id: Int?,
        val title: String?,
        val author: String?,
        val about: String?,
        val isbn: String?,
        val upload: String?,
        val current_loc: String?,
        var current_loc_folio: String?,
        val cover: String?,
        val cover_image: String?,
        val cover_url: String?,
        var local_filename: String?,
        var local_path: String?,
        var local_cover: String?,
        val created_at: String?,
        var updated_at: String?
) {
    private val TAG = Book::class.java.simpleName

    override fun toString(): String {
        var currentLocFolio: String = "\" \""
        if (!current_loc_folio!!.isEmpty()) {
            currentLocFolio = current_loc_folio!!
        }
        return """{
            "_id": $_id,
            "id": $id,
            "title": "$title",
            "author": "$author",
            "about": "$about",
            "isbn": "$isbn",
            "upload": "$upload",
            "current_loc": "$current_loc",
            "current_loc_folio": $currentLocFolio,
            "cover": "$cover",
            "cover_image": "$cover_image",
            "cover_url": "$cover_url",
            "local_filename": "$local_filename",
            "local_path": "$local_path",
            "local_cover": "$local_cover",
            "created_at": "$created_at",
            "updated_at": "$updated_at"
        }"""
    }

    fun toList(): List<Pair<String, String>> {
        return listOf(
                "id" to this.id.toString(),
                "title" to this.title.toString(),
                "author" to this.author.toString(),
                "about" to this.author.toString(),
                "isbn" to this.isbn.toString(),
                "current_loc" to this.current_loc.toString(),
                "current_loc_folio" to this.current_loc_folio.toString(),
                "upadted_at" to this.updated_at.toString()
        )
    }

    fun read(context: Context) {
        val config = Config.ConfigBuilder()
                .nightmode(true)
                .fontSize(1)
                .setShowTts(false)
                .font(Constants.FONT_RALEWAY)
                .themeColor(R.color.pink)
                .build()

        val folioReader = FolioReader(context.applicationContext)
        val dataSource = BookPilaDataSource(context)
        val prefs = context.getSharedPreferences(context.packageName + "_preferences", 0)

        // Retrieve and set last read location.
        if (!this.current_loc_folio!!.isEmpty()) {
            val currentLocFolio = JSONObject(this.current_loc_folio)
            folioReader.setLastReadState(
                    currentLocFolio.get("lastReadChapterIndex") as Int,
                    currentLocFolio.get("lastReadSpanIndex").toString()
            )
        }

        if (this.title.equals("The Sign of Four")) {
            Log.d(TAG, "opening: file://${this.local_path}")
            folioReader.openBook("file://${this.local_path}", config)
        } else {
            folioReader.openBook("${this.local_path}", config)
        }

        val localBook = this

        folioReader.setLastReadStateCallback(object : LastReadStateCallback {
            override fun saveLastReadState(lastReadChapterIndex: Int, lastReadSpanIndex: String?) {
                // Save lastReadChapterIndex and lastReadSpanIndex to the local database.
                val currentLocFolio = JSONObject()
                currentLocFolio.put("lastReadChapterIndex", lastReadChapterIndex)
                if (lastReadSpanIndex!!.isEmpty()) {
                    currentLocFolio.put("lastReadSpanIndex", "")
                } else {
                    currentLocFolio.put("lastReadSpanIndex", JSONObject(lastReadSpanIndex))
                }
                localBook.current_loc_folio = currentLocFolio.toString()
                localBook.updated_at = Timestamp(System.currentTimeMillis()).toString()
                dataSource.updateBook(localBook)

                val editor = prefs.edit()
                editor.putString("last_book", localBook.title)
                editor.apply()

                Log.d(TAG, "localBook.current_loc_folio: ${localBook.current_loc_folio}")

                // Check for network connectivity.
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val netInfo = cm.activeNetworkInfo
                if (netInfo.isConnectedOrConnecting) {
                    // Check for token and update book on server.
                    val url = prefs.getString("url", "")
                    val token = prefs.getString("token", "")
                    FuelManager.instance.baseHeaders = mapOf("Authorization" to "Token " + token)

                    if (!token.isEmpty()) {
                        Fuel.put(
                                "$url/api/books/${localBook.id}",
                                localBook.toList()
                        ).response { request, response, result ->
                            when (result) {
                                is Result.Failure -> {
                                    val ex = result.getException()
                                    Log.d(TAG, "Book: ${localBook.title} NOT updated... ${ex.message}")
                                }
                                is Result.Success -> {
                                    val data = result.get()
                                    Log.d(TAG, "Book: ${localBook.title} updated... ${response.statusCode}")
                                }
                            }
                        }
                    }
                }
            }
        })
    }

}