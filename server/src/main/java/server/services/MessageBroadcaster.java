package server.services;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageBroadcaster
{

    private final SimpMessagingTemplate simpMessagingTemplate;

    /**
     *
     * @param simpMessagingTemplate
     */
    public MessageBroadcaster(SimpMessagingTemplate simpMessagingTemplate)
    {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    /**
     * Broadcast a message to the specified topic.
     *
     * @param topic   The topic to broadcast to.
     * @param message The message to broadcast.
     */
    public void broadcast(String topic, String message)
    {
        simpMessagingTemplate.convertAndSend(topic, message);
    }
}
