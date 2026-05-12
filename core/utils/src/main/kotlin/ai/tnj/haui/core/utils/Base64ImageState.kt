package ai.tnj.haui.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Base64ImageState(
  val image: ImageBitmap?,
  val failed: Boolean,
)

@Composable
fun rememberBase64ImageState(base64: String): Base64ImageState {
  var image by remember(base64) { mutableStateOf<ImageBitmap?>(null) }
  var failed by remember(base64) { mutableStateOf(false) }

  LaunchedEffect(base64) {
    failed = false
    val bitmap = withContext(Dispatchers.Default) {
      try {
        decodeBase64Bitmap(base64)?.asImageBitmap()
      } catch (_: Throwable) {
        null
      }
    }
    image = bitmap
    failed = (bitmap == null)
  }

  return Base64ImageState(image = image, failed = failed)
}
