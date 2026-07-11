package com.swiply.backend.media

import com.swiply.backend.common.SecurityUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

data class ReorderPhotosRequest(
    @field:NotEmpty
    val photoIds: List<UUID>,
)

@RestController
@RequestMapping("/api/v1/users/me/photos")
@Tag(name = "Photos", description = "Фото профиля (до 6 штук)")
class MediaController(private val photoService: PhotoService) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Загрузка фото: magic-bytes валидация, EXIF-стрип, thumbnail'ы асинхронно")
    suspend fun upload(@RequestPart("file") file: MultipartFile): PhotoResponse {
        val userId = SecurityUtils.currentUserId()
        return withContext(Dispatchers.IO) { photoService.upload(userId, file) }
    }

    @DeleteMapping("/{photoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удаление фото")
    suspend fun delete(@PathVariable photoId: UUID) {
        val userId = SecurityUtils.currentUserId()
        withContext(Dispatchers.IO) { photoService.delete(userId, photoId) }
    }

    @PutMapping("/order")
    @Operation(summary = "Смена порядка фото")
    suspend fun reorder(@Valid @RequestBody request: ReorderPhotosRequest): List<PhotoResponse> {
        val userId = SecurityUtils.currentUserId()
        return withContext(Dispatchers.IO) {
            photoService.reorder(userId, request.photoIds)
            photoService.listFor(userId)
        }
    }
}
