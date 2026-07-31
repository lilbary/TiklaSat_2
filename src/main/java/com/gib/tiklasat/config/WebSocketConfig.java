package com.gib.tiklasat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket (Gerçek Zamanlı Bildirim) Boru Hattı Ayarları.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // İstemcilerin (Örn: React, Vue, Vanilla JS) bağlanacağı kapı adresi.
        // setAllowedOriginPatterns ile tüm alan adlarından gelen bağlantılara izin veriyoruz (CORS).
        // withSockJS() eski tarayıcılarda da çalışabilmesi için bir geri dönüş senaryosu sağlar.
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Sunucunun kullanıcılara mesaj göndereceği kanal önekleri.
        // Örn: /topic/auctions/123 veya /queue/user/456
        registry.enableSimpleBroker("/topic", "/queue");
        
        // Kullanıcıların sunucuya mesaj gönderirken kullanacağı önek
        registry.setApplicationDestinationPrefixes("/app");
    }
}
