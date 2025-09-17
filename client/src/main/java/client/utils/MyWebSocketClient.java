package client.utils;

import client.src.ClientCollection;
import client.src.CollectionManager;
import client.src.NoteManager;
import client.src.PopUpManager;
import jakarta.inject.Inject;
import javafx.application.Platform;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class MyWebSocketClient
{
    private final Map<String, StompSession> stompSessions;

    @Inject
    private Configuration configuration;

    @Inject
    private CollectionManager collectionManager;

    @Inject
    private NoteManager noteManager;

    @Inject
    private PopUpManager popUpManager;

    /**
     * Constructor
     */
    public MyWebSocketClient()
    {
        stompSessions = new HashMap<>();
    }

    /**
     * Establish connections to all distinct servers on which there is
     * a collection we have subscribed to.
     * TODO: Add reconnecting attempts when a server is not reachable.
     */
    public void initialize()
    {
        for (String serverURL : configuration.getCollections()
                .stream().map(ClientCollection::getServerURL).distinct().toList())
        {
            attemptConnection(serverURL);
        }
    }

    /**
     * Checks if connection is up:
     * if it is - calls CollectionManager serverOnline
     * otherwise - attemptConnection
     * @param url the server url
     */
    public void attemptWithSessionCheck(String url)
    {
        if(stompSessions.containsKey(url) && stompSessions.get(url) != null)
        {
            collectionManager.serverOnline(url);
        }
        else
        {
            attemptConnection(url);
        }
    }

    /**
     * Attempt to connect to a server
     * @param url The url of the server
     */
    public void attemptConnection(String url)
    {
        try
        {
            StompSession session = connectToServer(url);
            connectionToServerEstablished(url, session);
        }
        catch (ExecutionException | InterruptedException e)
        {
        }
    }

    /**
     * Establish a connection to a server and subscribe to /topic/news
     * @param url The url of the server
     * @return The stomp session established
     * @throws InterruptedException
     */
    public StompSession connectToServer(String url) throws ExecutionException, InterruptedException
    {
        WebSocketClient wsClient = new StandardWebSocketClient();

        WebSocketStompClient stompClient = new WebSocketStompClient(wsClient);

        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSessionHandler sessionHandler =
                new MyStompSessionHandler(url, noteManager, collectionManager, this);

        String wsurl = url.replace("http", "ws") + "ws-connect";

        System.out.println("Connecting to: " + wsurl);
        StompSession stompSession = stompClient.connect(wsurl, sessionHandler).get();

        stompSession.subscribe("/topic/news", sessionHandler);

        return stompSession;
    }

    /**
     * Called on connection to a server lost
     * Lock the notes and collections and notify the user
     * @param url The url of the server
     */
    public void connectionToServerLost(String url)
    {
        // Here call appropriate methods to lock the notes and collections
        // and notify the user
        // Do this only if the stompSession in the map is not marked as null
        if (!stompSessions.containsKey(url) || stompSessions.get(url) != null)
        {
            Platform.runLater(() -> popUpManager.serverOfflineNotification(url));
            stompSessions.put(url, null);
            collectionManager.serverOffline(url);
        }

        new Timer().schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                attemptConnection(url);
            }
        }, 10 * 1000);
    }

    /**
     * Called on connection to a server established
     * Notify the user
     * Notify the collection manager that the server is online to unlock the notes and collections
     * @param url The url of the server
     * @param session The stomp session
     */
    private void connectionToServerEstablished(String url, StompSession session)
    {
        if (stompSessions.containsKey(url))
        {
            Platform.runLater(() -> popUpManager.serverOnlineNotification(url));
        }
        stompSessions.put(url, session);
        collectionManager.serverOnline(url);
    }

    /**
     * Send a message to a server
     * @param url The url of the server
     * @param message The message
     */
    public void sendMessage(String url, String message)
    {
        stompSessions.get(url).send("/topic/news",
                message + stompSessions.get(url).getSessionId());
    }

    /**
     * Ends websocket connection for given url
     * @param url the url to end WS connection for
     */
    public void endConnection(String url)
    {
        StompSession session = stompSessions.get(url);
        if(session!=null && session.isConnected())
        {
            session.disconnect();
        }
        stompSessions.remove(url);

        System.out.println("Disconnected from server: " + url);
    }
}