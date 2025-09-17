package server.api;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import server.services.MessageBroadcaster;

@Controller
public class MessageController
{
    private final MessageBroadcaster messageBroadcaster;

    /**
     * Constructor that used to inject the SMT
     * @param messageBroadcaster The service used to broadcast messages.
     */
    public MessageController(MessageBroadcaster messageBroadcaster)
    {
        this.messageBroadcaster = messageBroadcaster;
    }

    /**
     * This is not used for now.
     * It is needed if you want to manipulate the message before
     * sending it back
     * @param message The received message from the client
     */
    @MessageMapping("/news")
    public void broadcastNews(@Payload String message)
    {
        System.out.println("Message received: " + message);
        messageBroadcaster.broadcast("/topic/news", message);
    }
}


