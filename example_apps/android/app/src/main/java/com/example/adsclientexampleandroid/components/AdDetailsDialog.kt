package com.example.adsclientexampleandroid.components

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import mozilla.appservices.adsclient.MozAdsImage

@Composable
fun AdDetailsDialog(placement: MozAdsImage, onDismiss: () -> Unit = {}) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("Ad details") },
        text = {
            val callbacks = placement.callbacks
            val details = buildString {
                appendLine("{")
                appendLine("""  "blockKey": "${placement.blockKey}",""")
                appendLine("""  "format": "${placement.format}",""")
                appendLine("""  "imageUrl": "${placement.imageUrl}",""")
                appendLine("""  "url": "${placement.url}",""")
                appendLine("""  "altText": ${quoteOrNull(placement.altText)},""")
                appendLine("""  "callbacks": {""")
                appendLine("""    "click": "${callbacks.click}",""")
                appendLine("""    "impression": "${callbacks.impression}",""")
                appendLine("""    "report": ${quoteOrNull(callbacks.report)}""")
                appendLine("  }")
                appendLine("}")
            }
            SelectionContainer { Text(details) }
        }
    )
}

private fun quoteOrNull(s: String?): String = s?.let { "\"$it\"" } ?: "null"
