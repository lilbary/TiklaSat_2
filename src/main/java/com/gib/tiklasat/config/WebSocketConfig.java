package com.gib.tiklasat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Frontend'in (React) sunucuya bağlanacağı kapı (endpoint) burasıdır.
        // setAllowedOriginPatterns("*") ile CORS (farklı portlardan erişim) sorununu çözeriz.
        // withSockJS() ise WebSocket desteklemeyen çok eski tarayıcılar için yedek yoldur.
        registry.addEndpoint("/ws-auction").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Sunucunun kullanıcılara (istemcilere) mesaj iteceği (push) kanalın ön eki: "/topic"
        // Örn: /topic/auctions/123-abc odasındakilere mesaj gidecek.
        // Artık In-Memory Simple Broker yerine RabbitMQ (STOMP Relay) kullanıyoruz:
        registry.enableStompBrokerRelay("/topic")
                .setRelayHost("localhost")
                .setRelayPort(61613) // RabbitMQ STOMP plugin portu
                .setClientLogin("guest")
                .setClientPasscode("guest");

        // Kullanıcıların sunucuya (Backend'e) mesaj atacağı kapının ön eki: "/app"
        registry.setApplicationDestinationPrefixes("/app");
    }
}
