package com.thehoick.bookpila

import org.junit.Before
import org.junit.Test

import org.junit.Assert.*

class MainActivityPresenterTest {
    lateinit var presenter: MainActivityPresenter
    lateinit var view: MainActivityView

    class MockView(): MainActivityView {
        override fun openSettingsFragment() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun launchLoginActivity(activity: Class<LoginActivity>) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    @Before
    fun setUp() {
        view = MockView()
        presenter = MainActivityPresenter(view)
    }

    @Test
    fun openSettingsFragment() {
        // Arrange
        // Act
        // Assert
    }

    @Test
    fun launchLoginActivity() {
    }

    @Test
    fun getView() {
    }
}