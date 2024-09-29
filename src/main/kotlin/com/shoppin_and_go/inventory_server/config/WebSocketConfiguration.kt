package com.shoppin_and_go.inventory_server.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.*

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS()
    }

    override fun configureMessageBroker(config: org.springframework.messaging.simp.config.MessageBrokerRegistry) {
        config.enableSimpleBroker("/queue/device")
        config.setApplicationDestinationPrefixes("/app")
    }
}
