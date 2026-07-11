package com.swiply.app.core.common.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.swiply.app.core.common.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * Клиентское сжатие перед загрузкой (требование ТЗ): downsampling до 2048px
 * по большей стороне + JPEG ~80%. Экономит трафик и время загрузки.
 */
@Singleton
class ImageCompressor @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    companion object {
        private const val MAX_DIMENSION = 2048
        private const val JPEG_QUALITY = 80
    }

    /** null — если uri не читается или это не изображение */
    suspend fun compress(uri: Uri): ByteArray? = withContext(ioDispatcher) {
        val resolver = context.contentResolver

        // 1-й проход: только границы, чтобы посчитать inSampleSize без OOM
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) } ?: return@withContext null
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext null

        var sampleSize = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= MAX_DIMENSION) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val sampled = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: return@withContext null

        // точное ограничение стороны после грубого downsample
        val scale = MAX_DIMENSION.toFloat() / max(sampled.width, sampled.height)
        val bitmap = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                sampled,
                (sampled.width * scale).toInt().coerceAtLeast(1),
                (sampled.height * scale).toInt().coerceAtLeast(1),
                true,
            ).also { if (it != sampled) sampled.recycle() }
        } else {
            sampled
        }

        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
        bitmap.recycle()
        output.toByteArray()
    }
}
