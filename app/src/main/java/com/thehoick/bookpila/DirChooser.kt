package com.thehoick.bookpila

import android.app.ActionBar
import android.app.AlertDialog
import android.content.Context
import android.view.ViewGroup
import android.content.DialogInterface
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.*
import java.io.File
import java.io.IOException
import java.util.*


// Callback interface for selected directory
interface ChosenDirectoryListener {
    var m_isNewFolderEnabled: Boolean

    fun onChosenDir(chosenDir: String)

    fun DirectoryChooserDialog(context: Context, chosenDirectoryListener: ChosenDirectoryListener)


    fun setNewFolderEnabled(isNewFolderEnabled: Boolean) {
        m_isNewFolderEnabled = isNewFolderEnabled
    }

    fun getNewFolderEnabled(): Boolean {
        return m_isNewFolderEnabled
    }

//    fun chooseDirectory()

    fun chooseDirectory(dir: String)
}


class DirectoryChooserDialog(private val m_context: Context, chosenDirectoryListener: ChosenDirectoryListener) {
    var newFolderEnabled = true
    private var m_sdcardDirectory = ""
    private var m_titleView: TextView? = null

    private var m_dir = ""
    private var m_subdirs: MutableList<String>? = null
    private var m_chosenDirectoryListener: ChosenDirectoryListener? = null
    private var m_listAdapter: ArrayAdapter<String>? = null

    interface ChosenDirectoryListener {
        fun onChosenDir(chosenDir: String)
    }

    init {
        m_sdcardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath()
        m_chosenDirectoryListener = chosenDirectoryListener

        try {
            m_sdcardDirectory = File(m_sdcardDirectory).getCanonicalPath()
        } catch (ioe: IOException) {
        }

    }

    @JvmOverloads
    fun chooseDirectory(dir: String = m_sdcardDirectory) {
        var dir = dir
        val dirFile = File(dir)
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            dir = m_sdcardDirectory
        }

        try {
            dir = File(dir).getCanonicalPath()
        } catch (ioe: IOException) {
            return
        }

        m_dir = dir
        m_subdirs = getDirectories(dir)

        class DirectoryOnClickListener : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, item: Int) {
                // Navigate into the sub-directory
                m_dir += "/" + (dialog as AlertDialog).getListView().getAdapter().getItem(item)
                updateDirectory()
            }
        }

        val dialogBuilder = createDirectoryChooserDialog(dir, m_subdirs, DirectoryOnClickListener())

        dialogBuilder.setPositiveButton("OK", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
                // Current directory chosen
                if (m_chosenDirectoryListener != null) {
                    // Call registered listener supplied with the chosen directory
                    m_chosenDirectoryListener!!.onChosenDir(m_dir)
                }
            }
        }).setNegativeButton("Cancel", null)

        val dirsDialog = dialogBuilder.create()

        dirsDialog.setOnKeyListener(object : DialogInterface.OnKeyListener {
            override fun onKey(dialog: DialogInterface, keyCode: Int, event: KeyEvent): Boolean {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() === KeyEvent.ACTION_DOWN) {
                    // Back button pressed
                    if (m_dir == m_sdcardDirectory) {
                        // The very top level directory, do nothing
                        return false
                    } else {
                        // Navigate back to an upper directory
                        m_dir = File(m_dir).getParent()
                        updateDirectory()
                    }

                    return true
                } else {
                    return false
                }
            }
        })

        // Show directory chooser dialog
        dirsDialog.show()
    }

    private fun createSubDir(newDir: String): Boolean {
        val newDirFile = File(newDir)
        return if (!newDirFile.exists()) {
            newDirFile.mkdir()
        } else false

    }

    private fun getDirectories(dir: String): MutableList<String> {
        val dirs = ArrayList<String>()

        try {
            val dirFile = File(dir)
            if (!dirFile.exists() || !dirFile.isDirectory()) {
                return dirs
            }

            for (file in dirFile.listFiles()) {
                if (file.isDirectory()) {
                    dirs.add(file.getName())
                }
            }
        } catch (e: Exception) {
        }

        Collections.sort(dirs, object : Comparator<String> {
            override fun compare(o1: String, o2: String): Int {
                return o1.compareTo(o2)
            }
        })

        return dirs
    }

    private fun createDirectoryChooserDialog(title: String, listItems: List<String>?,
                                             onClickListener: DialogInterface.OnClickListener): AlertDialog.Builder {
        val dialogBuilder = AlertDialog.Builder(m_context)

        val titleLayout = LinearLayout(m_context)
        titleLayout.orientation = LinearLayout.VERTICAL

        m_titleView = TextView(m_context)
        m_titleView!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        m_titleView!!.setTextAppearance(m_context, android.R.style.TextAppearance_Large)
        m_titleView!!.setTextColor(m_context.resources.getColor(android.R.color.white))
        m_titleView!!.gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
        m_titleView!!.text = title

        val newDirButton = Button(m_context)
        newDirButton.setLayoutParams(ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        newDirButton.text = m_titleView!!   .context.getString(R.string.new_folder)
        newDirButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val input = EditText(m_context)

                // Show new folder name input dialog
                AlertDialog.Builder(m_context).setTitle("New folder name").setView(input).setPositiveButton("OK", DialogInterface.OnClickListener { dialog, whichButton ->
                    val newDir = input.text
                    val newDirName = newDir.toString()
                    // Create new directory
                    if (createSubDir("$m_dir/$newDirName")) {
                        // Navigate into the new directory
                        m_dir += "/$newDirName"
                        updateDirectory()
                    } else {
                        Toast.makeText(
                                m_context, "Failed to create '" + newDirName +
                                "' folder", Toast.LENGTH_SHORT).show()
                    }
                }).setNegativeButton("Cancel", null).show()
            }
        })

        if (!newFolderEnabled) {
            newDirButton.setVisibility(View.GONE)
        }

        titleLayout.addView(m_titleView)
        titleLayout.addView(newDirButton)

        dialogBuilder.setCustomTitle(titleLayout)

        m_listAdapter = createListAdapter(listItems)

        dialogBuilder.setSingleChoiceItems(m_listAdapter, -1, onClickListener)
        dialogBuilder.setCancelable(false)

        return dialogBuilder
    }

    private fun updateDirectory() {
        m_subdirs!!.clear()
        m_subdirs!!.addAll(getDirectories(m_dir))
        m_titleView!!.text = m_dir

        m_listAdapter!!.notifyDataSetChanged()
    }

    private fun createListAdapter(items: List<String>?): ArrayAdapter<String> {
        return object : ArrayAdapter<String>(m_context, android.R.layout.select_dialog_item, android.R.id.text1, items!!) {
            override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
                Log.d("CreateListAdapter getView", "position: $position")
                val v = super.getView(position, convertView, parent)

                if (v is TextView) {
                    // Enable list item (directory) text wrapping
                    v.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    v.ellipsize = null
                }
                return v
            }
        }
    }
}