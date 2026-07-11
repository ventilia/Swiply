package com.swiply.backend.discovery

import com.swiply.backend.config.RabbitConfig
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component


@Component
class DiscoveryRecomputeWorker(private val discoveryService: DiscoveryService) {

    private val log = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = [RabbitConfig.QUEUE_DISCOVERY_RECOMPUTE])
    fun handle(task: DiscoveryRecomputeTask) {
        if (discoveryService.hasCache(task.userId)) {
            discoveryService.recompute(task.userId)
        } else {
            log.debug("У {} нет активного кэша ленты — пересчёт отложен до первого запроса", task.userId)
        }
    }
}
