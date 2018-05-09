package com.thehoick.bookpila

import android.app.Fragment
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.folioreader.util.FolioReader
import com.github.kittinunf.fuel.Fuel
import com.thehoick.bookpila.models.BookPilaDataSource
import kotlinx.android.synthetic.main.activity_main.*
//import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_book.*
import org.json.JSONObject
import java.io.File
import android.R.id.edit
import android.content.SharedPreferences
import android.widget.*
import com.folioreader.Config
import com.folioreader.Config.ConfigBuilder
import com.folioreader.Constants.FONT_RALEWAY
import com.folioreader.Font
import com.folioreader.util.LastReadStateCallback
import com.google.gson.JsonParser
import com.thehoick.bookpila.models.Book
import org.readium.r2.streamer.Server.Server
import org.readium.r2_streamer.parser.EpubParser


class BookFragment: Fragment() {
    val TAG = BookFragment::class.java.simpleName
    val book = "BOOK"
    val only_book = "only_book"
    lateinit var tempFile: String
    lateinit var localBook: Book
    private var folioReader: FolioReader? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater!!.inflate(R.layout.fragment_book, container, false)
        view.setBackgroundColor(Color.WHITE)

        val prefs = activity.getSharedPreferences(activity.packageName + "_preferences", 0)
        val localDir = prefs.getString("local_dir", "")

        val bookString = arguments.getString(book)
        val onlyBook = arguments.getBoolean(only_book)
        Log.d(TAG, "bookString: $bookString")
        val book = JsonParser().parse(bookString).getAsJsonObject()

        Log.d(TAG, "book.get(upload): ${book.get("upload").asString}")

        val fileName = book.get("upload").asString.split("/").last().split(".").first()
        val fileExt = book.get("upload").asString.split("/").last().split(".").last()
        Log.d(TAG, "fileName: $fileName, fileExt: $fileExt |")


        val title = view.findViewById<TextView>(R.id.detailTitle)
        val author = view.findViewById<TextView>(R.id.detailAuthor)
        val about = view.findViewById<TextView>(R.id.detailAbout)
        val cover = view.findViewById<ImageView>(R.id.detailCover)
        val download = view.findViewById<Button>(R.id.downloadButton)
        val read = view.findViewById<Button>(R.id.readButton)
        val delete = view.findViewById<Button>(R.id.deleteButton)

        title.text = book.get("title").asString
        author.text = "Author: ${book.get("author").asString}"
        about.text = "About:\n ${book.get("about").asString}"
        Glide.with(view.context).load(book.get("cover_url").asString).into(cover)

        // Set button visibility based on Local or Server.
        val dataSource = BookPilaDataSource(context)
        localBook = dataSource.getBook(book.get("title").asString)!!
        Log.d(TAG, "fileExt: |${fileExt}|")

        if (localBook._id != null || fileExt.equals("null") || !fileExt.equals("epub")) {
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
            Log.d(TAG, "Download ${book.get("upload").asString}")

            Log.d(TAG, "localDir: $localDir fileName: $fileName")

            Fuel.download(book.get("upload").asString).destination { response, url ->
                val temp = File.createTempFile(fileName, ".epub", File(localDir))
                tempFile = temp.absolutePath

                temp
            }.response { req, res, result ->
                Log.d(TAG, "file downloaded...")

                download.visibility = INVISIBLE

                // Save book data into a local SQL database.
//                book.put("local_filename", "$fileName.epub")
//                book.put("local_path", "$localDir/$fileName.epub")
                book.addProperty("local_filename", "$fileName.epub")
                book.addProperty("local_path", "$localDir/$fileName.epub")
                dataSource.createBook(book as JSONObject)

                File(tempFile).renameTo(File("$localDir/$fileName.epub"))

                this.fragmentManager.popBackStackImmediate()
            }
        }

        read.setOnClickListener {
            Log.d(TAG, "Read ${book.get("title").asString}...")

            val config = Config.ConfigBuilder()
                    .nightmode(true)
                    .fontSize(1)
                    .setShowTts(false)
                    .font(FONT_RALEWAY)
                    .themeColor(R.color.pink)
                    .build()

            folioReader = FolioReader(context.applicationContext)
//                    folioReader?.registerHighlightListener(this)
//            folioReader.setLastReadStateCallback(activity)



            if (book.get("title").asString.equals("The Sign of Four")) {
                Log.d(TAG, "opening: file://${book.get("local_path").asString}")
                folioReader?.openBook("file://${book.get("local_path").asString}", config)
            } else {
                folioReader?.openBook("${book.get("local_path").asString}", config)
//                folioReader.setReadPosition(book.get("current_loc").toString());
            }

//            getHighlightsAndSave();
            getLastReadPositionAndSave()

//            folioReader?.setLastReadStateCallback(getlas)

            folioReader?.setLastReadStateCallback(object : LastReadStateCallback {
                override fun saveLastReadState(lastReadChapterIndex: Int, lastReadSpanIndex: String?) {
                    // Save lastReadChapterIndex and lastReadSpanIndex to the local database.
                    val currentLocFolio = JSONObject()
                    currentLocFolio.put("lastReadChapterIndex", lastReadChapterIndex)
                    currentLocFolio.put("lastReadSpanIndex", JSONObject(lastReadSpanIndex))
//                    localBook.current_loc_folio = """{"lastReadChapterIndex": $lastReadChapterIndex, "lastReadSpanIndex": $lastReadSpanIndex}"""
//                    localBook.updated_at = Date.time.now()
//                    val folioStr = currentLocFolio.toString().substring(1, currentLocFolio.toString().length-1)
                    localBook.current_loc_folio = currentLocFolio.toString()
                    Log.d(TAG, "localBook.current_loc_folio: ${localBook.current_loc}")
                    dataSource.updateBook(localBook)
                }
            })
        }

        delete.setOnClickListener {
            dataSource.deleteBook(book.get("title").toString())
            this.fragmentManager.popBackStackImmediate()
        }

        return view
    }

    private fun getLastReadPositionAndSave() {
        // Conan:
        // epubcfi(/6/14[part-001-chapter-001-xhtml]!/4/2[part-001-chapter-001]/2[part-001-chapter-001-heading]/2/2/4/2/2/2/1:0)
        // Dune:
        // epubcfi(/6/20[chapter002]!/4/2[pg302]/2[pg303]/1:0)
        // epubcfi(/6/32[chapter008]!/4/162[pg1068]/1:0)

        Thread(Runnable {
            if (!localBook.current_loc_folio.equals("")) {
//                val currentLocFolio = JSONObject(localBook.current_loc_folio)
                val currentLocFolio = JSONObject("{\"lastReadChapterIndex\":1,\"lastReadSpanIndex\":{\"usingId\":false,\"value\":7}}")
//                val currentLocFolio = JsonParser().parse(localBook.current_loc_folio).getAsJsonObject()
//                val currentLocFolio = localBook.current_loc_folio

                Log.d(TAG, "localBook.current_loc_folio is String?: ${localBook.current_loc_folio is String}")
                Log.d(TAG, "currentLocFolio: $currentLocFolio")
//                Log.d(TAG, "lastChapterIndex: ${currentLocFolio.get("lastChapterIndex").asInt}")

                val lastReadChapterIndex = currentLocFolio.get("lastReadChapterIndex") as Int
                val lastReadSpanIndex = currentLocFolio.get("lastReadSpanIndex").toString()

                Log.d(TAG, "getLastReadPositionAndSave thread lastReadChapterIndex: $lastReadChapterIndex")
                folioReader!!.setLastReadState(lastReadChapterIndex, lastReadSpanIndex)
            }
        }).start()
    }
}