package com.thehoick.bookpila

import com.fasterxml.jackson.databind.JavaType

interface MainActivityView {
    fun openSettingsFragment()

    fun launchLoginActivity(activity: Class<LoginActivity>)
}