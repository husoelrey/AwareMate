package org.awaremate.shared.presentation.growth.components

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.awaremate.shared.domain.model.MoodEntry
import org.awaremate.shared.domain.model.WeeklyMoodScreenTimeCorrelation
import java.io.File
import java.io.FileOutputStream

@Composable
actual fun WeeklyInsightShareSection(
    moodEntries: List<MoodEntry>,
    correlation: WeeklyMoodScreenTimeCorrelation,
    modifier: Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    var isSharing by remember { mutableStateOf(false) }
    var shareError by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        WeeklyInsightCardContent(
            moodEntries = moodEntries,
            correlation = correlation,
            modifier = Modifier.drawWithContent {
                graphicsLayer.record { this@drawWithContent.drawContent() }
                drawLayer(graphicsLayer)
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                scope.launch {
                    isSharing = true
                    shareError = null
                    runCatching {
                        val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                        val uri = saveShareImage(context, bitmap)
                        openShareSheet(context, uri)
                    }.onFailure {
                        shareError = "The image couldn't be prepared. Please try again."
                    }
                    isSharing = false
                }
            },
            enabled = !isSharing,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Share my private weekly insight as an image" }
        ) {
            if (isSharing) {
                CircularProgressIndicator(strokeWidth = 2.dp)
            } else {
                Text("Share my weekly insight")
            }
        }
        shareError?.let {
            Spacer(modifier = Modifier.height(6.dp))
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

private suspend fun saveShareImage(context: Context, bitmap: Bitmap) = withContext(Dispatchers.IO) {
    val directory = File(context.cacheDir, "shared_insights").apply { mkdirs() }
    val image = File(directory, "awaremate_weekly_insight.png")
    FileOutputStream(image).use { output ->
        check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
            "PNG compression failed"
        }
    }
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", image)
}

private fun openShareSheet(context: Context, uri: android.net.Uri) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, "My private weekly AwareMate insight")
        clipData = ClipData.newUri(context.contentResolver, "AwareMate weekly insight", uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(shareIntent, "Share your weekly insight")
    if (context !is Activity) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
}
