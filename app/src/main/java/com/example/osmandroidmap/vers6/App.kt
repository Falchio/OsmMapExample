package com.example.osmandroidmap.vers6

import android.app.Application

class App:Application() {
    companion object{
        val instance = App()
    }
}