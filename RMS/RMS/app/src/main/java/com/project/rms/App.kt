package com.project.rms

import android.app.Application


class App : Application() { //SharedPreferences사용하기 위해 만듬
    companion object{
        lateinit var prefs : SharedPreferences
    }
    override fun onCreate() {
        prefs = SharedPreferences(applicationContext)
        super.onCreate()
    }
}