package server.api;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer
{
    /**
     * Set up the subscription destination /topic
     * @param registry Used for configuration
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry)
    {
        // Enable a simple message broker for the topic and queue prefixes
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Set up the initial connection endpoint /ws-connect
     * @param registry Used for configuration
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry)
    {
        // Register the endpoint for clients to connect
        registry.addEndpoint("/ws-connect").setAllowedOrigins("*");
    }
}
