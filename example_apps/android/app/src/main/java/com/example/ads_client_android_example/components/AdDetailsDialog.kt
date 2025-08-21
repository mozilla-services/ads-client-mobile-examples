package com.example.ads_client_android_example.components

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import mozilla.appservices.ads_client.MozAdsPlacement

@Composable
fun AdDetailsDialog(placement: MozAdsPlacement, onDismiss: () -> Unit = {}) {
  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    title = { Text("Ad details") },
    text = {
      val ad = placement.content
      val callbacks = ad.callbacks
      val details = buildString {
        appendLine("{")
        appendLine("""  "placementId": "${placement.placementConfig.placementId}",""")
        appendLine("""  "format": ${quoteOrNull(ad.format)},""")
        appendLine("""  "imageUrl": ${quoteOrNull(ad.imageUrl)},""")
        appendLine("""  "url": ${quoteOrNull(ad.url)},""")
        appendLine("""  "callbacks": {""")
        appendLine("""    "click": ${quoteOrNull(callbacks?.click)},""")
        appendLine("""    "impression": ${quoteOrNull(callbacks?.impression)},""")
        appendLine("""    "report": ${quoteOrNull(callbacks?.report)}""")
        appendLine("  }")
        appendLine("}")
      }
      SelectionContainer { Text(details) }
    }
  )
}

private fun quoteOrNull(s: String?): String = s?.let { "\"$it\"" } ?: "null"
