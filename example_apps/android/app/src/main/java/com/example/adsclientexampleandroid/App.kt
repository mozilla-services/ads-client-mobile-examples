package com.example.adsclientexampleandroid

import android.app.Application
import mozilla.appservices.httpconfig.RustHttpConfig
import mozilla.components.lib.fetch.okhttp.OkHttpClient as AcOkHttpClient

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // We need to provide the Rust backend with an actual HTTP client
        RustHttpConfig.setClient(lazy { AcOkHttpClient() })
    }
}
