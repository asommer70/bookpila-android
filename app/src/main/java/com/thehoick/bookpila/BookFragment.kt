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
import android.net.Uri
import android.view.Window
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.result.Result
import java.sql.Date
import java.sql.Timestamp
import java.util.*


class BookFragment: Fragment() {
    val TAG = BookFragment::class.java.simpleName
    val book = "BOOK"
    val only_book = "only_book"
    lateinit var tempFile: String
    lateinit var coverTempFile: String
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
        val coverFileName = book.get("cover_url").toString().split("/").last().split(".").first()
        val coverFileExt = book.get("cover_url").toString().split("/").last().split(".").last()
        Log.d(TAG, "fileName: $fileName, fileExt: $fileExt |")
        Log.d(TAG, "coverFileName: $coverFileName, coverFileExt: $coverFileExt |")


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

        // Set button visibility based on Local or Server.
        val dataSource = BookPilaDataSource(context)
        localBook = dataSource.getBook(book.get("title").toString())
        Log.d(TAG, "fileExt: |${fileExt}|")

        if (localBook?._id != null || fileExt.equals("null") || !fileExt.equals("epub")) {
            Glide.with(view.context).load(Uri.fromFile(File(localBook?.local_cover))).into(cover)
            download.visibility = INVISIBLE

            if (fileExt.equals("null") || !fileExt.equals("epub")) {
                read.visibility = INVISIBLE
            }

            if (onlyBook) {
                delete.visibility = INVISIBLE
            }
        } else {
            Glide.with(view.context).load(book.get("cover_url").toString()).into(cover)
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
                book.put("local_cover", " ")
                dataSource.createBook(book as JSONObject)

                File(tempFile).renameTo(File("$localDir/$fileName.epub"))

//                this.fragmentManager.popBackStackImmediate()
                val coverDir = File(localDir + "/covers")
                if (!coverDir.exists()) {
                    try {
                        coverDir.mkdir()
                    } catch (se: SecurityException) {
                        //handle it
                        Log.d(TAG, "Could not create $localDir/covers...")
                    }
                }

                Fuel.download(book.get("cover_url").toString()).destination { response, url ->
                    val coverTemp = File.createTempFile(coverFileName, coverFileExt, File(localDir + "/covers"))
                    coverTempFile = coverTemp.absolutePath

                    coverTemp
                }.response { req, res, result ->
                    Log.d(TAG, "cover file downloaded...")

                    download.visibility = INVISIBLE

                    // Save book cover location.
                    val localBook = dataSource.getBook(book.get("title").toString())
                    localBook?.local_cover = "$localDir/covers/$coverFileName.$coverFileExt"
                    dataSource.updateBook(localBook!!)

                    File(coverTempFile).renameTo(File("$localDir/covers/$coverFileName.$coverFileExt"))

                    this.fragmentManager.popBackStackImmediate()
                }
            }
        }

        read.setOnClickListener {
            Log.d(TAG, "Read ${book.get("title")}...")
//            activity.getWindow().requestFeature(Window.FEATURE_ACTION_BAR)
//            activity.getActionBar().hide()
            localBook?.read(context)
        }

        delete.setOnClickListener {
            dataSource.deleteBook(book.get("title").toString())
            this.fragmentManager.popBackStackImmediate()
        }

        return view
    }

    //    fun isNetworkOnline(): Boolean {
//        var status = false
//        try {
//            val cm = this.activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//            var netInfo: NetworkInfo? = cm.getNetworkInfo(0)
//            if (netInfo != null && netInfo.state == NetworkInfo.State.CONNECTED) {
//                status = true
//            } else {
//                netInfo = cm.getNetworkInfo(1)
//                if (netInfo != null && netInfo.state == NetworkInfo.State.CONNECTED)
//                    status = true
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            return false
//        }
//
//        return status
//    }
}