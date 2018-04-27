package com.thehoick.bookpila


import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView

class NeedToLoginFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater!!.inflate(R.layout.activity_main, container, false)

        val message = view.findViewById<TextView>(R.id.defaultTextView)
        message.setText(getString(R.string.please_login))
        message.visibility = VISIBLE
        return view
    }
}