package org.succlz123.naix.app

import android.app.Application
import org.succlz123.naix.lib.Naix

class MainApplication : Application() {

    @Naix
    override fun onCreate() {
        super.onCreate()
    }
}