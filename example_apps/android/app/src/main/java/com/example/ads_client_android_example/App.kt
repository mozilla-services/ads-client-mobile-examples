package com.example.ads_client_android_example

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

