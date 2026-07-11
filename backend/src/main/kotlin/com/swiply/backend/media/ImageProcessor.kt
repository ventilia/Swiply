package com.swiply.backend.media

import com.swiply.backend.common.BadRequestException
import net.coobird.thumbnailator.Thumbnails
import org.springframework.stereotype.Component
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

data class ProcessedImage(val bytes: ByteArray, val width: Int, val height: Int)

@Component
class ImageProcessor {

    companion object {
        private val JPEG_MAGIC = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
        private val PNG_MAGIC = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)
    }


    fun detectFormat(bytes: ByteArray): String? = when {
        bytes.size > 3 && bytes.copyOfRange(0, 3).contentEquals(JPEG_MAGIC) -> "jpeg"
        bytes.size > 4 && bytes.copyOfRange(0, 4).contentEquals(PNG_MAGIC) -> "png"
        else -> null
    }


    fun reencodeToJpeg(bytes: ByteArray, maxDimensionPx: Int): ProcessedImage {
        detectFormat(bytes)
            ?: throw BadRequestException("UNSUPPORTED_IMAGE", "Поддерживаются только JPEG и PNG")


        val source = try {
            ByteArrayInputStream(bytes).use { ImageIO.read(it) }
        } catch (e: Exception) {
            null
        } ?: throw BadRequestException("UNSUPPORTED_IMAGE", "Файл не является корректным изображением")


        val rgb = BufferedImage(source.width, source.height, BufferedImage.TYPE_INT_RGB)
        rgb.createGraphics().apply {
            color = Color.WHITE
            fillRect(0, 0, source.width, source.height)
            drawImage(source, 0, 0, null)
            dispose()
        }

        val output = ByteArrayOutputStream()
        Thumbnails.of(rgb)
            .size(maxDimensionPx, maxDimensionPx)
            .outputFormat("jpg")
            .outputQuality(0.85)
            .toOutputStream(output)

        val result = output.toByteArray()
        val resultImage = ByteArrayInputStream(result).use { ImageIO.read(it) }
        return ProcessedImage(result, resultImage.width, resultImage.height)
    }


    fun thumbnail(bytes: ByteArray, sizePx: Int): ByteArray {
        val output = ByteArrayOutputStream()
        Thumbnails.of(ByteArrayInputStream(bytes))
            .crop(net.coobird.thumbnailator.geometry.Positions.CENTER)
            .size(sizePx, sizePx)
            .outputFormat("jpg")
            .outputQuality(0.8)
            .toOutputStream(output)
        return output.toByteArray()
    }
}
