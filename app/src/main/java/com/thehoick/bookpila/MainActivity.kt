package com.thehoick.bookpila

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.folioreader.util.FolioReader
import java.io.IOException
import android.widget.Toast
import com.fasterxml.jackson.core.type.TypeReference
import com.folioreader.model.HighLight
import com.folioreader.ui.base.OnSaveHighlight
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.BufferedReader
import java.io.InputStreamReader
//import com.folioreader.util.
//import com.folioreader.util.FolioReader
import com.folioreader.util.LastReadStateCallback
import com.folioreader.util.OnHighlightListener


class MainActivity : AppCompatActivity(), LastReadStateCallback {
    private val TAG = MainActivity::class.java.simpleName
    private var folioReader: FolioReader? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        folioReader = FolioReader(this)
//        folioReader?.registerHighlightListener(this);
        folioReader?.setLastReadStateCallback(this);

        folioReader?.openBook("file:///android_asset/books/TheSilverChair.epub");

        getHighlightsAndSave();
        getLastReadPositionAndSave();
    }

    private fun getLastReadPositionAndSave() {

        Thread(Runnable {
            val lastReadChapterIndex = 12
            val lastReadSpanIndex = "{\"usingId\":false,\"value\":21}"
            Log.d(TAG, "getLastReadPositionAndSave thread lastReadChapterIndex: $lastReadChapterIndex")
            folioReader?.setLastReadState(lastReadChapterIndex, lastReadSpanIndex)
        }).start()
    }

    override fun saveLastReadState(lastReadChapterIndex: Int, lastReadSpanIndex: String) {

        Toast.makeText(this, "lastReadChapterIndex = " + lastReadChapterIndex +
                ", lastReadSpanIndex = " + lastReadSpanIndex, Toast.LENGTH_SHORT).show()
        Log.d(TAG, "-> saveLastReadState -> lastReadChapterIndex = "
                + lastReadChapterIndex + ", lastReadSpanIndex = " + lastReadSpanIndex)
    }

    /*
     * For testing purpose, we are getting dummy highlights from asset. But you can get highlights from your server
     * On success, you can save highlights to FolioReader DB.
     */
    private fun getHighlightsAndSave() {
        Log.d(TAG, "highlighting...")
//        Thread(Runnable {
//            var highlightList: ArrayList<HighLight>? = null
//            val objectMapper = ObjectMapper()
//            try {
//                highlightList = objectMapper.readValue(
//                        loadAssetTextAsString("highlights/highlights_data.json"),
//                        object : TypeReference<List<HighlightData>>() {
//
//                        })
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//
//            if (highlightList == null) {
//                folioReader.saveReceivedHighLights(highlightList, OnSaveHighlight {
//                    //You can do anything on successful saving highlight list
//                })
//            }
//        }).start()
    }

    private fun loadAssetTextAsString(name: String): String? {
        var `in`: BufferedReader? = null
        try {
            val buf = StringBuilder()
            val `is` = assets.open(name)
            `in` = BufferedReader(InputStreamReader(`is`))

            var str: String
            var isFirst = true
//            while ((str = `in`!!.readLine()) != null) {
//                if (isFirst)
//                    isFirst = false
//                else
//                    buf.append('\n')
//                buf.append(str)
//            }
            return buf.toString()
        } catch (e: IOException) {
            Log.e("HomeActivity", "Error opening asset $name")
        } finally {
            if (`in` != null) {
                try {
                    `in`!!.close()
                } catch (e: IOException) {
                    Log.e("HomeActivity", "Error closing asset $name")
                }

            }
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        folioReader?.unregisterHighlightListener()
        folioReader?.removeLastReadStateCallback()
    }

    fun onHighlight(highlight: HighLight, type: HighLight.HighLightAction) {
        Toast.makeText(this,
                "highlight id = " + highlight.uuid + " type = " + type,
                Toast.LENGTH_SHORT).show()
    }
}
