package com.thehoick.bookpila

import org.junit.Test

import org.junit.Assert.*
import org.junit.Before


class MainActivityTest {
    lateinit var activity: MainActivity

    @Before
    fun setUp() {
        activity = MainActivity()
//        activity?.onCreate(null)
    }


    @Test
    fun textViewIsThere() {
        // Arrange
        val textView = activity.defaultTextView

        // Act
        textView.text = "Beans..."

        // Assert
        assertEquals("Beans...", textView.text.toString())
    }
}
