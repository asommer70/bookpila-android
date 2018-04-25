package com.thehoick.bookpila

class MainActivityPresenter(val view: MainActivityView) {

    fun openSettingsFragment() {
        view.openSettingsFragment()
    }

    fun launchLoginActivity(activity: Class<LoginActivity>) {
        view.launchLoginActivity(activity)
    }

}
