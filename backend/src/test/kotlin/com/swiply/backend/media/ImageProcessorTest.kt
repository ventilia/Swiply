package com.swiply.backend.media

import com.swiply.backend.common.BadRequestException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ImageProcessorTest {

    private val processor = ImageProcessor()

    private fun imageBytes(format: String, width: Int = 320, height: Int = 200): ByteArray {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val out = ByteArrayOutputStream()
        ImageIO.write(image, format, out)
        return out.toByteArray()
    }

    @Test
    fun `определяет jpeg и png по magic bytes`() {
        assertEquals("jpeg", processor.detectFormat(imageBytes("jpg")))
        assertEquals("png", processor.detectFormat(imageBytes("png")))
    }

    @Test
    fun `расширение не обманывает — важно содержимое`() {
        assertNull(processor.detectFormat("GIF89a-вовсе-не-фото".toByteArray()))
        assertNull(processor.detectFormat(ByteArray(10) { 0x00 }))
    }

    @Test
    fun `переэнкод ограничивает разрешение`() {
        val big = imageBytes("png", width = 4000, height = 3000)
        val processed = processor.reencodeToJpeg(big, maxDimensionPx = 512)
        assertTrue(processed.width <= 512 && processed.height <= 512)
        // результат — валидный JPEG
        assertEquals("jpeg", processor.detectFormat(processed.bytes))
    }

    @Test
    fun `png с прозрачностью конвертируется в jpeg без падения`() {
        val image = BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB)
        val out = ByteArrayOutputStream()
        ImageIO.write(image, "png", out)

        val processed = processor.reencodeToJpeg(out.toByteArray(), maxDimensionPx = 128)
        assertEquals("jpeg", processor.detectFormat(processed.bytes))
    }

    @Test
    fun `не-изображение отклоняется`() {
        assertThrows<BadRequestException> {
            processor.reencodeToJpeg("это точно не картинка".toByteArray(), 512)
        }
    }

    @Test
    fun `полиглот с валидным magic но битым телом отклоняется`() {
        val fake = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte()) + "мусор".toByteArray()
        assertThrows<BadRequestException> { processor.reencodeToJpeg(fake, 512) }
    }

    @Test
    fun `thumbnail получается квадратным заданного размера`() {
        val thumb = processor.thumbnail(imageBytes("jpg", 800, 600), sizePx = 128)
        val decoded = ImageIO.read(ByteArrayInputStream(thumb))
        assertEquals(128, decoded.width)
        assertEquals(128, decoded.height)
    }
}
