package com.swiply.backend.common

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager


@Component
class AfterCommitPublisher(private val rabbitTemplate: RabbitTemplate) {

    fun publish(exchange: String, routingKey: String, payload: Any) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                object : TransactionSynchronization {
                    override fun afterCommit() {
                        rabbitTemplate.convertAndSend(exchange, routingKey, payload)
                    }
                },
            )
        } else {
            rabbitTemplate.convertAndSend(exchange, routingKey, payload)
        }
    }
}
