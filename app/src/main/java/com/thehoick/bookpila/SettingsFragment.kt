package com.thehoick.bookpila

import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.angads25.filepicker.controller.DialogSelectionListener
import com.github.angads25.filepicker.model.DialogConfigs
import com.github.angads25.filepicker.model.DialogProperties
import com.github.angads25.filepicker.view.FilePickerDialog
import java.io.File

class SettingsFragment: PreferenceFragment() {
    val TAG = SettingsFragment::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val prefs = activity.getSharedPreferences(activity.packageName + "_preferences", 0)
        val localDir = prefs.getString("local_dir", "")

        val button = findPreference(getString(R.string.localDirSelector))
        button.setSummary(localDir)

        button.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
            override fun onPreferenceClick(preference: Preference): Boolean {

                val properties = DialogProperties()

                properties.selection_mode = DialogConfigs.SINGLE_MODE
                properties.selection_type = DialogConfigs.FILE_AND_DIR_SELECT
                properties.root = File(DialogConfigs.DEFAULT_DIR)
                properties.error_dir = File(DialogConfigs.DEFAULT_DIR)
                properties.offset = File(DialogConfigs.DEFAULT_DIR)
                properties.extensions = null

                val dialog = FilePickerDialog(activity, properties)
                dialog.setTitle("Select a Folder")

                dialog.setDialogSelectionListener(object : DialogSelectionListener {
                    override fun onSelectedFilePaths(files: Array<String>) {
                        //files is an array of the paths of files selected by the Application User.
                        Log.d(TAG, "onSelectedFilePaths files: ${files[0]}")

                        val editor = prefs.edit()
                        editor.putString("local_dir", files[0])
                        editor.apply()
                    }
                })

                dialog.show()

                return true
            }
        }

        // Set the background of the view or else it's transparent.
        view!!.setBackgroundColor(resources.getColor(android.R.color.white))
        return view
    }
}