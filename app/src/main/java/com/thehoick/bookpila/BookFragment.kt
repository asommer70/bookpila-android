package com.thehoick.bookpila

//import kotlinx.android.synthetic.main.activity_main.*
import android.app.Fragment
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.folioreader.Config
import com.folioreader.Constants.FONT_RALEWAY
import com.folioreader.util.FolioReader
import com.folioreader.util.LastReadStateCallback
import com.github.kittinunf.fuel.Fuel
import com.thehoick.bookpila.models.Book
import com.thehoick.bookpila.models.BookPilaDataSource
import org.json.JSONObject
import java.io.File
import android.net.NetworkInfo
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.result.Result
import java.sql.Date
import java.util.*


class BookFragment: Fragment() {
    val TAG = BookFragment::class.java.simpleName
    val book = "BOOK"
    val only_book = "only_book"
    lateinit var tempFile: String
    private var localBook: Book? = null
    private var folioReader: FolioReader? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater!!.inflate(R.layout.fragment_book, container, false)
        view.setBackgroundColor(Color.WHITE)

        val prefs = activity.getSharedPreferences(activity.packageName + "_preferences", 0)
        val localDir = prefs.getString("local_dir", "")

        val bookString = arguments.getString(book)
        val onlyBook = arguments.getBoolean(only_book)
        val book = JSONObject(bookString)

        Log.d(TAG, "book.get(upload): ${book.get("upload")}")

        val fileName = book.get("upload").toString().split("/").last().split(".").first()
        val fileExt = book.get("upload").toString().split("/").last().split(".").last()
        Log.d(TAG, "fileName: $fileName, fileExt: $fileExt |")


        val title = view.findViewById<TextView>(R.id.detailTitle)
        val author = view.findViewById<TextView>(R.id.detailAuthor)
        val about = view.findViewById<TextView>(R.id.detailAbout)
        val cover = view.findViewById<ImageView>(R.id.detailCover)
        val download = view.findViewById<Button>(R.id.downloadButton)
        val read = view.findViewById<Button>(R.id.readButton)
        val delete = view.findViewById<Button>(R.id.deleteButton)

        title.text = book.get("title").toString()
        author.text = "Author: ${book.get("author")}"
        about.text = "About:\n ${book.get("about")}"
        Glide.with(view.context).load(book.get("cover_url").toString()).into(cover)

        // Set button visibility based on Local or Server.
        val dataSource = BookPilaDataSource(context)
        localBook = dataSource.getBook(book.get("title").toString())
        Log.d(TAG, "fileExt: |${fileExt}|")

        if (localBook?._id != null || fileExt.equals("null") || !fileExt.equals("epub")) {
            download.visibility = INVISIBLE

            if (fileExt.equals("null") || !fileExt.equals("epub")) {
                read.visibility = INVISIBLE
            }

            if (onlyBook) {
                delete.visibility = INVISIBLE
            }
        } else {
            read.visibility = INVISIBLE
            delete.visibility = INVISIBLE
        }

        download.setOnClickListener {
            Log.d(TAG, "Download ${book.get("upload")}")

            Log.d(TAG, "localDir: $localDir fileName: $fileName")

            Fuel.download(book.get("upload").toString()).destination { response, url ->
                val temp = File.createTempFile(fileName, ".epub", File(localDir))
                tempFile = temp.absolutePath

                temp
            }.response { req, res, result ->
                Log.d(TAG, "file downloaded...")

                download.visibility = INVISIBLE

                // Save book data into a local SQL database.
                book.put("local_filename", "$fileName.epub")
                book.put("local_path", "$localDir/$fileName.epub")
                dataSource.createBook(book as JSONObject)

                File(tempFile).renameTo(File("$localDir/$fileName.epub"))

                this.fragmentManager.popBackStackImmediate()
            }
        }

        read.setOnClickListener {
            Log.d(TAG, "Read ${book.get("title")}...")

            val config = Config.ConfigBuilder()
                    .nightmode(true)
                    .fontSize(1)
                    .setShowTts(false)
                    .font(FONT_RALEWAY)
                    .themeColor(R.color.pink)
                    .build()

            folioReader = FolioReader(context.applicationContext)

            // Retrieve and set last read location.
            val currentLocFolio = JSONObject(localBook?.current_loc_folio)
            folioReader!!.setLastReadState(
                    currentLocFolio.get("lastReadChapterIndex") as Int,
                    currentLocFolio.get("lastReadSpanIndex").toString()
            )

            if (book.get("title").toString().equals("The Sign of Four")) {
                Log.d(TAG, "opening: file://${book.get("local_path")}")
                folioReader?.openBook("file://${book.get("local_path").toString()}", config)
            } else {
                folioReader?.openBook("${book.get("local_path").toString()}", config)
            }


            folioReader?.setLastReadStateCallback(object : LastReadStateCallback {
                override fun saveLastReadState(lastReadChapterIndex: Int, lastReadSpanIndex: String?) {
                    // Save lastReadChapterIndex and lastReadSpanIndex to the local database.
                    val currentLocFolio = JSONObject()
                    currentLocFolio.put("lastReadChapterIndex", lastReadChapterIndex)
                    currentLocFolio.put("lastReadSpanIndex", JSONObject(lastReadSpanIndex))
                    localBook?.current_loc_folio = currentLocFolio.toString()
                    localBook?.updated_at = Date().toString()
                    dataSource.updateBook(localBook!!)

                    Log.d(TAG, "localBook.current_loc_folio: ${localBook?.current_loc_folio}")

                    // Check for network connectivity.
                    if (isNetworkOnline()) {
                        // TODO:as check for token and update book on server.
                        val url = prefs.getString("url", "")
                        val token = prefs.getString("token", "")
                        FuelManager.instance.baseHeaders = mapOf("Authorization" to "Token " + token)

                        if (!token.isEmpty()) {
                            Fuel.put(
                                    "$url/api/books/${book.get("id")}",
                                    localBook?.toList()
                            ).response { request, response, result ->
                                when (result) {
                                    is Result.Failure -> {
                                        val ex = result.getException()
                                        Log.d(TAG, "Book: ${localBook?.title} NOT updated... ${ex.message}")
                                    }
                                    is Result.Success -> {
                                        val data = result.get()
                                        Log.d(TAG, "Book: ${localBook?.title} updated... ${response.statusCode}")
                                    }
                                }
                            }
                        }
                    }
                }
            })
        }

        delete.setOnClickListener {
            dataSource.deleteBook(book.get("title").toString())
            this.fragmentManager.popBackStackImmediate()
        }

        return view
    }

    fun isNetworkOnline(): Boolean {
        var status = false
        try {
            val cm = this.activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            var netInfo: NetworkInfo? = cm.getNetworkInfo(0)
            if (netInfo != null && netInfo.state == NetworkInfo.State.CONNECTED) {
                status = true
            } else {
                netInfo = cm.getNetworkInfo(1)
                if (netInfo != null && netInfo.state == NetworkInfo.State.CONNECTED)
                    status = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return status
    }
}