package com.swiply.backend.media

import com.swiply.backend.config.SwiplyProperties
import io.minio.BucketExistsArgs
import io.minio.GetObjectArgs
import io.minio.GetPresignedObjectUrlArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import io.minio.http.Method
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream


interface MediaStorage {
    fun put(key: String, bytes: ByteArray, contentType: String)
    fun get(key: String): ByteArray
    fun delete(key: String)
    fun presignedGetUrl(key: String): String
}

@Component
class MinioMediaStorage(private val props: SwiplyProperties) : MediaStorage {

    private val log = LoggerFactory.getLogger(javaClass)
    private val bucket = props.media.photosBucket

    private val client: MinioClient = MinioClient.builder()
        .endpoint(props.media.endpoint)
        .credentials(props.media.accessKey, props.media.secretKey)
        .region(REGION)
        .build()


    private val presignClient: MinioClient = MinioClient.builder()
        .endpoint(props.media.publicEndpoint)
        .credentials(props.media.accessKey, props.media.secretKey)
        .region(REGION)
        .build()

    private companion object {
        const val REGION = "us-east-1"
    }

    @PostConstruct
    fun ensureBucket() {
        val exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())
        if (!exists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
            log.info("Создан бакет {}", bucket)
        }
    }

    override fun put(key: String, bytes: ByteArray, contentType: String) {
        ByteArrayInputStream(bytes).use { stream ->
            client.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(key)
                    .stream(stream, bytes.size.toLong(), -1)
                    .contentType(contentType)
                    .build(),
            )
        }
    }

    override fun get(key: String): ByteArray =
        client.getObject(GetObjectArgs.builder().bucket(bucket).`object`(key).build())
            .use { it.readBytes() }

    override fun delete(key: String) {
        client.removeObject(RemoveObjectArgs.builder().bucket(bucket).`object`(key).build())
    }

    override fun presignedGetUrl(key: String): String =
        presignClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucket)
                .`object`(key)
                .expiry(props.media.presignTtl.seconds.toInt())
                .build(),
        )
}
