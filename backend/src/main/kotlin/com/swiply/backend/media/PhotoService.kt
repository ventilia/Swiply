package com.swiply.backend.media

import com.swiply.backend.common.AfterCommitPublisher
import com.swiply.backend.common.BadRequestException
import com.swiply.backend.common.NotFoundException
import com.swiply.backend.config.RabbitConfig
import com.swiply.backend.config.SwiplyProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

data class PhotoResponse(
    val id: UUID,
    val position: Int,
    val status: PhotoStatus,
    val url: String,
    val thumbUrl: String?,
)

@Service
class PhotoService(
    private val photoRepository: PhotoRepository,
    private val storage: MediaStorage,
    private val imageProcessor: ImageProcessor,
    private val publisher: AfterCommitPublisher,
    private val props: SwiplyProperties,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun upload(userId: UUID, file: MultipartFile): PhotoResponse {
        if (file.isEmpty) throw BadRequestException("EMPTY_FILE", "Пустой файл")
        if (file.size > props.media.maxPhotoBytes) {
            throw BadRequestException("FILE_TOO_LARGE", "Файл больше ${props.media.maxPhotoBytes / 1024 / 1024} МБ")
        }
        return uploadBytes(userId, file.bytes)
    }

    /**  (демо-сид) */
    @Transactional
    fun uploadBytes(userId: UUID, bytes: ByteArray): PhotoResponse {
        val existing = photoRepository.findByUserIdOrderByPosition(userId)
        if (existing.size >= props.media.maxPhotosPerUser) {
            throw BadRequestException("TOO_MANY_PHOTOS", "Максимум ${props.media.maxPhotosPerUser} фото")
        }


        val processed = imageProcessor.reencodeToJpeg(bytes, props.media.maxDimensionPx)

        val photoId = UUID.randomUUID()
        val key = "users/$userId/photos/$photoId/orig.jpg"
        storage.put(key, processed.bytes, "image/jpeg")

        val photo = photoRepository.save(
            Photo(
                id = photoId,
                userId = userId,
                position = (existing.maxOfOrNull { it.position } ?: -1) + 1,
                storageKey = key,
            ),
        )
        publisher.publish(RabbitConfig.EXCHANGE, RabbitConfig.QUEUE_MEDIA_PROCESS, MediaProcessTask(photo.id))
        log.debug("Фото {} пользователя {} загружено, задача на обработку отправлена", photo.id, userId)
        return toResponse(photo)
    }

    @Transactional
    fun delete(userId: UUID, photoId: UUID) {
        val photo = photoRepository.findById(photoId)
            .orElseThrow { NotFoundException("PHOTO_NOT_FOUND", "Фото не найдено") }
        if (photo.userId != userId) throw NotFoundException("PHOTO_NOT_FOUND", "Фото не найдено")

        deleteStorageKeys(photo)
        photoRepository.delete(photo)


        photoRepository.findByUserIdOrderByPosition(userId).forEachIndexed { index, p ->
            if (p.position != index) {
                p.position = index
                photoRepository.save(p)
            }
        }
    }

    @Transactional
    fun reorder(userId: UUID, orderedIds: List<UUID>) {
        val photos = photoRepository.findByUserIdOrderByPosition(userId)
        if (orderedIds.toSet() != photos.map { it.id }.toSet()) {
            throw BadRequestException("BAD_ORDER", "Список должен содержать все фото пользователя ровно один раз")
        }
        val byId = photos.associateBy { it.id }
        orderedIds.forEachIndexed { index, id ->
            val photo = byId.getValue(id)
            if (photo.position != index) {
                photo.position = index
                photoRepository.save(photo)
            }
        }
    }

    fun listFor(userId: UUID): List<PhotoResponse> =
        photoRepository.findByUserIdOrderByPosition(userId).map { toResponse(it) }


    fun listApprovedFor(userId: UUID): List<PhotoResponse> =
        photoRepository.findByUserIdOrderByPosition(userId)
            .filter { it.status == PhotoStatus.APPROVED }
            .map { toResponse(it) }

    fun deleteAllFor(userId: UUID) {
        photoRepository.findByUserIdOrderByPosition(userId).forEach { photo ->
            deleteStorageKeys(photo)
            photoRepository.delete(photo)
        }
    }

    fun toResponse(photo: Photo): PhotoResponse = PhotoResponse(
        id = photo.id,
        position = photo.position,
        status = photo.status,
        url = storage.presignedGetUrl(photo.storageKey),
        thumbUrl = photo.thumbKey?.let { storage.presignedGetUrl(it) },
    )

    private fun deleteStorageKeys(photo: Photo) {
        runCatching { storage.delete(photo.storageKey) }
            .onFailure { log.warn("Не удалось удалить {}: {}", photo.storageKey, it.message) }
        photo.thumbKey?.let { key ->
            runCatching { storage.delete(key) }.onFailure { log.warn("Не удалось удалить {}", key) }
        }
        photo.thumbSmallKey?.let { key ->
            runCatching { storage.delete(key) }.onFailure { log.warn("Не удалось удалить {}", key) }
        }
    }
}
