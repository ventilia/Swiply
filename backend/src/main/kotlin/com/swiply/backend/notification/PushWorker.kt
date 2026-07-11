package com.swiply.backend.notification

import com.swiply.backend.config.RabbitConfig
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component


@Component
class PushWorker(private val pushSender: PushNotificationSender) {

    @RabbitListener(queues = [RabbitConfig.QUEUE_PUSH_SEND])
    fun handle(task: PushSendTask) {
        pushSender.send(task.userId, task.type, task.title, task.body)
    }
}
