package com.swiply.backend.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@EnableRabbit
class RabbitConfig {

    companion object {
        const val EXCHANGE = "swiply.topic"
        const val DLX = "swiply.dlx"

        const val QUEUE_MEDIA_PROCESS = "media.process"
        const val QUEUE_DISCOVERY_RECOMPUTE = "discovery.recompute"
        const val QUEUE_PUSH_SEND = "push.send"
    }

    @Bean
    fun messageConverter() = Jackson2JsonMessageConverter()

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory, converter: Jackson2JsonMessageConverter): RabbitTemplate =
        RabbitTemplate(connectionFactory).apply { messageConverter = converter }

    @Bean
    fun rabbitListenerContainerFactory(
        connectionFactory: ConnectionFactory,
        converter: Jackson2JsonMessageConverter,
    ): SimpleRabbitListenerContainerFactory =
        SimpleRabbitListenerContainerFactory().apply {
            setConnectionFactory(connectionFactory)
            setMessageConverter(converter)
            setDefaultRequeueRejected(false)
        }

    @Bean
    fun topicExchange() = TopicExchange(EXCHANGE, true, false)

    @Bean
    fun deadLetterExchange() = TopicExchange(DLX, true, false)

    private fun workQueue(name: String): Queue =
        QueueBuilder.durable(name)
            .withArgument("x-dead-letter-exchange", DLX)
            .withArgument("x-dead-letter-routing-key", name)
            .build()

    @Bean
    fun mediaProcessQueue(): Queue = workQueue(QUEUE_MEDIA_PROCESS)

    @Bean
    fun discoveryRecomputeQueue(): Queue = workQueue(QUEUE_DISCOVERY_RECOMPUTE)

    @Bean
    fun pushSendQueue(): Queue = workQueue(QUEUE_PUSH_SEND)

    @Bean
    fun mediaProcessDlq(): Queue = QueueBuilder.durable("$QUEUE_MEDIA_PROCESS.dlq").build()

    @Bean
    fun discoveryRecomputeDlq(): Queue = QueueBuilder.durable("$QUEUE_DISCOVERY_RECOMPUTE.dlq").build()

    @Bean
    fun pushSendDlq(): Queue = QueueBuilder.durable("$QUEUE_PUSH_SEND.dlq").build()

    @Bean
    fun mediaProcessBinding(): Binding =
        BindingBuilder.bind(mediaProcessQueue()).to(topicExchange()).with(QUEUE_MEDIA_PROCESS)

    @Bean
    fun discoveryRecomputeBinding(): Binding =
        BindingBuilder.bind(discoveryRecomputeQueue()).to(topicExchange()).with(QUEUE_DISCOVERY_RECOMPUTE)

    @Bean
    fun pushSendBinding(): Binding =
        BindingBuilder.bind(pushSendQueue()).to(topicExchange()).with(QUEUE_PUSH_SEND)

    @Bean
    fun mediaProcessDlqBinding(): Binding =
        BindingBuilder.bind(mediaProcessDlq()).to(deadLetterExchange()).with(QUEUE_MEDIA_PROCESS)

    @Bean
    fun discoveryRecomputeDlqBinding(): Binding =
        BindingBuilder.bind(discoveryRecomputeDlq()).to(deadLetterExchange()).with(QUEUE_DISCOVERY_RECOMPUTE)

    @Bean
    fun pushSendDlqBinding(): Binding =
        BindingBuilder.bind(pushSendDlq()).to(deadLetterExchange()).with(QUEUE_PUSH_SEND)
}
