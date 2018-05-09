package com.thehoick.bookpila

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.folioreader.Config
import com.folioreader.Constants
import com.folioreader.util.FolioReader
import com.folioreader.util.LastReadStateCallback
import com.github.kittinunf.fuel.Fuel
import com.thehoick.bookpila.models.Book
import com.thehoick.bookpila.models.BookPilaDataSource
import org.json.JSONObject
import java.io.File
import java.io.IOException

class BookActivity: AppCompatActivity() {
    lateinit var localBook: Book
    lateinit var tempFile: String
    private var folioReader: FolioReader? = null
    private val TAG = BookActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_book)


        val prefs = this.getSharedPreferences(this.packageName + "_preferences", 0)
        val localDir = prefs.getString("local_dir", "")
        val extras = intent.extras
        val onlyBook = extras.get("only_book")

        val title = findViewById<TextView>(R.id.detailTitle)
        val author = findViewById<TextView>(R.id.detailAuthor)
        val about = findViewById<TextView>(R.id.detailAbout)
        val cover = findViewById<ImageView>(R.id.detailCover)
        val download = findViewById<Button>(R.id.downloadButton)
        val read = findViewById<Button>(R.id.readButton)
        val delete = findViewById<Button>(R.id.deleteButton)

        // Set button visibility based on Local or Server.
        val dataSource = BookPilaDataSource(this)
        localBook = dataSource.getBook(extras.getString("title", ""))!!

        val fileName = localBook.upload?.split("/")?.last()?.split(".")?.first()
        val fileExt = localBook.upload?.split("/")?.last()?.split(".")?.last()

        title.text = localBook.title
        author.text = "Author: ${localBook.author}"
        about.text = "About:\n ${localBook.about}"
        Glide.with(this).load(localBook.cover_url).into(cover)

        if (localBook._id != null || fileExt.equals("null") || !fileExt.equals("epub")) {
            download.visibility = View.INVISIBLE

            if (fileExt.equals("null") || !fileExt.equals("epub")) {
                read.visibility = View.INVISIBLE
            }

            if (onlyBook.equals("false")) {
                delete.visibility = View.INVISIBLE
            }
        } else {
            read.visibility = View.INVISIBLE
            delete.visibility = View.INVISIBLE
        }

        download.setOnClickListener {
            Log.d(TAG, "Download ${localBook.upload}")

            Log.d(TAG, "localDir: $localDir fileName: $fileName")

            Fuel.download(localBook.upload!!).destination { response, url ->
                val temp = File.createTempFile(fileName, ".epub", File(localDir))
                tempFile = temp.absolutePath

                temp
            }.response { req, res, result ->
                Log.d(TAG, "file downloaded...")

                download.visibility = View.INVISIBLE

                // Save book data into a local SQL database.
//                book.put("local_filename", "$fileName.epub")
//                book.put("local_path", "$localDir/$fileName.epub")
//                book.addProperty("local_filename", "$fileName.epub")
//                book.addProperty("local_path", "$localDir/$fileName.epub")
                localBook.local_filename = "$fileName.epub"
                localBook.local_path = "$localDir/$fileName.epub"
                dataSource.createBook(localBook.toString() as JSONObject)

                File(tempFile).renameTo(File("$localDir/$fileName.epub"))

                this.fragmentManager.popBackStackImmediate()
            }
        }

        read.setOnClickListener {
            Log.d(TAG, "Read ${localBook.title}...")
//                    folioReader?.registerHighlightListener(this)
//            folioReader.setLastReadStateCallback(activity)



            if (localBook.title.equals("The Sign of Four")) {

                val config = Config.ConfigBuilder()
                        .nightmode(true)
                        .fontSize(1)
                        .setShowTts(false)
                        .font(Constants.FONT_RALEWAY)
                        .themeColor(R.color.pink)
                        .build()

                folioReader = FolioReader(applicationContext)

                Log.d(TAG, "opening: file://${localBook.local_path} config.themeColor: ${config.themeColor}")
                folioReader?.setLastReadState(2, "{\"usingId\":false,\"value\":1}")
                folioReader?.openBook("file://${localBook.local_path}", config)

            } else {
//                folioReader?.openBook("${localBook.local_path}", config)
//                folioReader.setReadPosition(book.get("current_loc").toString());
            }

//            getHighlightsAndSave();
//            getLastReadPositionAndSave()


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
            dataSource.deleteBook(localBook.title!!)
            this.fragmentManager.popBackStackImmediate()
        }
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
//                folioReader!!.setLastReadState(lastReadChapterIndex, lastReadSpanIndex)
                folioReader!!.setLastReadState(1, "{\"usingId\":false,\"value\":7}")
            }
        }).start()
    }

    override fun onDestroy() {
        super.onDestroy()
        folioReader?.unregisterHighlightListener()
        folioReader?.removeLastReadStateCallback()
    }
}