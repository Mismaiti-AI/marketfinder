package com.marketfinder.core.presentation.media

import androidx.compose.ui.graphics.ImageBitmap

/** Decode a [ByteArray] (JPEG/PNG) into a Compose [ImageBitmap], or null on failure. */
expect fun decodeByteArrayToImageBitmap(bytes: ByteArray): ImageBitmap?
