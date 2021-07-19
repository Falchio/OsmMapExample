package com.example.osmandroidmap

import android.app.Application
import android.content.Context

class App:Application() {
    companion object{
        val instance = App()
    }
}