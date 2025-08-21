package com.example.ads_client_android_example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ads_client_android_example.ui.theme.AdsclientandroidexampleTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      AdsclientandroidexampleTheme {
        Scaffold(
          topBar = { AppTopBar() },
          modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
          AdsScreen(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
              .padding(16.dp)
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar() {
  TopAppBar(title = { Text("Ads Client Debugger") })
}
