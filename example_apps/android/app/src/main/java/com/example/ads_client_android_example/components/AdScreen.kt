package com.example.ads_client_android_example.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import mozilla.appservices.ads_client.MozAdsClient
import mozilla.appservices.ads_client.MozAdsPlacement
import mozilla.appservices.ads_client.MozAdsPlacementConfig
import mozilla.appservices.ads_client.IabContent
import mozilla.appservices.ads_client.IabContentTaxonomy

@Composable
fun AdsScreen(modifier: Modifier = Modifier) {
  val client = remember { MozAdsClient() }
  val scope = rememberCoroutineScope()

  var status by remember { mutableStateOf("Idle") }
  var adBillboard1 by remember { mutableStateOf<MozAdsPlacement?>(null) }
  var adBillboard2 by remember { mutableStateOf<MozAdsPlacement?>(null) }

  val slots = listOf(
    AdSlotUi("Billboard1", "pocket_billboard_1", 320f / 100f),
    AdSlotUi("Billboard2", "pocket_billboard_2", 320f / 100f)
  )

  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Button(onClick = {
        status = "Requesting…"
        adBillboard1 = null; adBillboard2 = null
        scope.launch {
          try {
            val configs = listOf(
              MozAdsPlacementConfig(
                placementId = "pocket_billboard_1",
                fixedSize = null,
                iabContent = IabContent(IabContentTaxonomy.IAB2_1, listOf("entertainment"))
              ),
              MozAdsPlacementConfig(
                placementId = "pocket_billboard_2",
                fixedSize = null,
                iabContent = IabContent(IabContentTaxonomy.IAB2_1, listOf("entertainment"))
              ),
            )
            val map = withContext(Dispatchers.IO) { client.requestAds(configs) }
            adBillboard1 = map["pocket_billboard_1"]
            adBillboard2 = map["pocket_billboard_2"]
            status = "Got: ${map.keys.joinToString().ifEmpty { "no ads" }}"
          } catch (t: Throwable) {
            status = "Error: ${t.message}"
            Log.e("AdsScreen", "requestAds failed", t)
          }
        }
      }) { Text("Fetch 2 Ads") }

      OutlinedButton(onClick = {
        adBillboard1 = null; adBillboard2 = null; status = "Cleared"
      }) { Text("Clear") }
    }

    Text(status, style = MaterialTheme.typography.bodyMedium)

    val minItemWidth = 360.dp
    LazyVerticalGrid(
      columns = GridCells.Adaptive(minSize = minItemWidth),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(slots) { slot ->
        val placement = when (slot.placementId) {
          "pocket_billboard_1" -> adBillboard1
          "pocket_billboard_2" -> adBillboard2
          else -> null
        }
        AdCard(
          title = slot.title,
          placement = placement,
          aspectRatio = slot.aspectRatio,
          onImpression = { p -> scope.launch(Dispatchers.IO) { safeCall("impression") { client.recordImpression(p) } } },
          onClick      = { p -> scope.launch(Dispatchers.IO) { safeCall("click")       { client.recordClick(p) } } },
          onReport     = { p -> scope.launch(Dispatchers.IO) { safeCall("report")      { client.reportAd(p) } } },
        )
      }
    }
  }
}

private data class AdSlotUi(
  val title: String,
  val placementId: String,
  val aspectRatio: Float,
)

private suspend inline fun safeCall(tag: String, crossinline f: suspend () -> Unit) {
  try { f() } catch (_: Throwable) { }
}
