package com.example.adsclientexampleandroid.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import mozilla.appservices.adsclient.MozAdsImage

@Composable
fun AdCard(
    title: String,
    placement: MozAdsImage?,
    aspectRatio: Float,
    onImpression: (MozAdsImage) -> Unit,
    onClick: (MozAdsImage) -> Unit,
    onReport: (MozAdsImage) -> Unit,
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
                    val url = placement.imageUrl
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
                Text(
                    "blockKey: ${placement.blockKey}",
                    style = MaterialTheme.typography.labelMedium
                )
                Text("format: ${placement.format}", style = MaterialTheme.typography.labelMedium)
                Text("url: ${placement.url}", style = MaterialTheme.typography.labelMedium)
                placement.altText?.let { Text("altText: $it", style = MaterialTheme.typography.labelMedium) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(onClick = { onImpression(placement) }) { Text("Record impression") }
                    FilledTonalButton(onClick = { onClick(placement) }) { Text("Record click") }
                    OutlinedButton(onClick = { onReport(placement) }) { Text("Report") }
                    OutlinedButton(onClick = {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(placement.url)))
                        } catch (_: Throwable) {
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
