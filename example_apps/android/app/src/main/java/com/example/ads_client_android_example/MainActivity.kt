package com.example.ads_client_android_example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ads_client_android_example.ui.theme.AdsclientandroidexampleTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import mozilla.appservices.ads_client.MozAdsClient
import mozilla.appservices.ads_client.MozAdsPlacement
import mozilla.appservices.ads_client.MozAdsPlacementConfig
import mozilla.appservices.ads_client.IabContent
import mozilla.appservices.ads_client.IabContentTaxonomy

import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size

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
          AdsDemo(
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

@Composable
fun AdsDemo(modifier: Modifier = Modifier) {
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
            Log.e("AdsDemo", "requestAds failed", t)
          }
        }
      }) { Text("Fetch 2 Ads") }

      OutlinedButton(onClick = {
        adBillboard1 = null; adBillboard2 = null; status = "Cleared"
      }) { Text("Clear") }
    }

    Text(status, style = MaterialTheme.typography.bodyMedium)

    val columns = rememberAdaptiveColumns(minItemWidth = 360.dp)
    LazyVerticalGrid(
      columns = GridCells.Adaptive(minSize = columns.minWidth),
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

@Composable
private fun AdCard(
  title: String,
  placement: MozAdsPlacement?,
  aspectRatio: Float,
  onImpression: (MozAdsPlacement) -> Unit,
  onClick: (MozAdsPlacement) -> Unit,
  onReport: (MozAdsPlacement) -> Unit,
) {
  val context = LocalContext.current
  ElevatedCard(
    shape = RoundedCornerShape(20.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text(title, style = MaterialTheme.typography.titleMedium)

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .aspectRatio(aspectRatio),
        contentAlignment = Alignment.Center
      ) {
        if (placement == null) {
          Text("No ad loaded")
        } else {
          val url = placement.content.imageUrl
          var retryKey by remember { mutableStateOf(0) }

          val request = remember(url, retryKey) {
            ImageRequest.Builder(context)
              .data(url)
              .crossfade(true)
              .size(Size.ORIGINAL)
              .build()
          }
          val painter = rememberAsyncImagePainter(model = request)
          val state = painter.state

          Image(
            painter = painter,
            contentDescription = "Ad image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
          )

          when (state) {
            is AsyncImagePainter.State.Loading -> {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(strokeWidth = 2.dp)
                Spacer(Modifier.height(6.dp))
                Text("Loading…", style = MaterialTheme.typography.labelMedium)
              }
            }
            is AsyncImagePainter.State.Error -> {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Image failed", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { retryKey++ }) { Text("Retry") }
              }
            }
            else -> Unit
          }
        }
      }

      if (placement != null) {
        val ad = placement.content
        Text("placementId: ${placement.placementConfig.placementId}", style = MaterialTheme.typography.labelMedium)
        ad.format?.let { Text("format: $it", style = MaterialTheme.typography.labelMedium) }
        ad.url?.let { Text("url: $it", style = MaterialTheme.typography.labelMedium) }

        FlowRowSpaceEvenly {
          FilledTonalButton(onClick = { onImpression(placement) }) { Text("Record impression") }
          FilledTonalButton(onClick = { onClick(placement) }) { Text("Record click") }
          OutlinedButton(onClick = { onReport(placement) }) { Text("Report") }
          OutlinedButton(onClick = {
            ad.url?.let { u ->
              try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(u))) } catch (_: Throwable) {}
            }
          }) { Text("Open URL") }
          var showDetails by remember { mutableStateOf(false) }
          OutlinedButton(onClick = { showDetails = true }) { Text("Examine") }
          if (showDetails) {
            AdDetailsDialog(placement = placement, onDismiss = { showDetails = false })
          }
        }
      }
    }
  }
}

@Composable
private fun FlowRowSpaceEvenly(content: @Composable RowScope.() -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.CenterVertically,
    content = content
  )
}

@Composable
private fun rememberAdaptiveColumns(minItemWidth: Dp): AdaptiveColumns {
  val widthDp = LocalConfiguration.current.screenWidthDp.dp
  return remember(widthDp) { AdaptiveColumns(minItemWidth) }
}
private data class AdaptiveColumns(val minWidth: Dp)

@Composable
private fun AdDetailsDialog(placement: MozAdsPlacement, onDismiss: () -> Unit = {}) {
  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    title = { Text("Ad details") },
    text = {
      val ad = placement.content
      val details = buildString {
        appendLine("{")
        appendLine("""  "placementId": "${placement.placementConfig.placementId}",""")
        appendLine("""  "format": ${quoteOrNull(ad.format)},""")
        appendLine("""  "imageUrl": ${quoteOrNull(ad.imageUrl)},""")
        appendLine("""  "url": ${quoteOrNull(ad.url)},""")
        appendLine("""  "callbacks": {""")
        val cb = ad.callbacks
        appendLine("""    "click": ${quoteOrNull(cb?.click)},""")
        appendLine("""    "impression": ${quoteOrNull(cb?.impression)},""")
        appendLine("""    "report": ${quoteOrNull(cb?.report)}""")
        appendLine("  }")
        appendLine("}")
      }
      SelectionContainer { Text(details) }
    }
  )
}

private fun quoteOrNull(s: String?): String = s?.let { "\"$it\"" } ?: "null"

private suspend inline fun safeCall(tag: String, crossinline f: suspend () -> Unit) {
  try { f() } catch (t: Throwable) { Log.e("AdsDemo", "$tag failed", t) }
}

@Preview(showBackground = true)
@Composable
fun PreviewAdsDemo() {
  AdsclientandroidexampleTheme { AdsDemo() }
}
