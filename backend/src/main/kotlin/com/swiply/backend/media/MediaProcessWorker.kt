package com.swiply.backend.media

import com.swiply.backend.config.RabbitConfig
import com.swiply.backend.config.SwiplyProperties
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional


@Component
class MediaProcessWorker(
    private val photoRepository: PhotoRepository,
    private val storage: MediaStorage,
    private val imageProcessor: ImageProcessor,
    private val moderationChecker: ContentModerationChecker,
    private val props: SwiplyProperties,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = [RabbitConfig.QUEUE_MEDIA_PROCESS])
    @Transactional
    fun process(task: MediaProcessTask) {
        val photo = photoRepository.findById(task.photoId).orElse(null)
        if (photo == null) {

            log.debug("Фото {} не найдено, пропускаем", task.photoId)
            return
        }

        val original = storage.get(photo.storageKey)
        val basePath = photo.storageKey.removeSuffix("/orig.jpg")

        val sizes = props.media.thumbnailSizes.sortedDescending()
        sizes.forEachIndexed { index, size ->
            val thumbKey = "$basePath/thumb_$size.jpg"
            storage.put(thumbKey, imageProcessor.thumbnail(original, size), "image/jpeg")
            when (index) {
                0 -> photo.thumbKey = thumbKey
                1 -> photo.thumbSmallKey = thumbKey
            }
        }

        if (photo.status == PhotoStatus.PENDING) {
            photo.status = when (moderationChecker.check(original)) {
                ModerationVerdict.APPROVE -> PhotoStatus.APPROVED
                ModerationVerdict.REJECT -> PhotoStatus.REJECTED
                ModerationVerdict.MANUAL_REVIEW -> PhotoStatus.PENDING
            }
        }
        photoRepository.save(photo)
        log.info("Фото {} обработано: thumbs={}, status={}", photo.id, sizes, photo.status)
    }
}
