package client.utils;

import client.src.CollectionManager;
import client.src.NoteManager;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

public class MyStompSessionHandler extends StompSessionHandlerAdapter
{

    private StompSession session = null;
    private final String serverURL;

    private final NoteManager noteManager;
    private final CollectionManager collectionManager;

    private final MyWebSocketClient myWebSocketClient;

    /**
     * Constructor for the handler
     * @param serverURL The server URL
     * @param noteManager The note manager reference
     * @param collectionManager The collection manager reference
     * @param myWebSocketClient The web socket client reference
     */
    public MyStompSessionHandler(String serverURL,
                                  NoteManager noteManager,
                                  CollectionManager collectionManager,
                                  MyWebSocketClient myWebSocketClient)
    {
        this.serverURL = serverURL;
        this.noteManager = noteManager;
        this.collectionManager = collectionManager;
        this.myWebSocketClient = myWebSocketClient;
    }

    /**
     * Call-back on connection
     * @param session The current session
     * @param connectedHeaders The STOMP headers
     */
    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders)
    {
        System.out.println("Connected to server: " + serverURL);
        this.session = session;
    }

    /**
     * Call-back on error
     * @param session The current session
     * @param exception The exception
     */
    @Override
    public void handleTransportError(StompSession session, Throwable exception)
    {
        if (!session.isConnected())
        {
            System.err.println("Connection to server failed: " + serverURL);
            this.session = null;
            myWebSocketClient
                    .connectionToServerLost(serverURL);
        }
    }

    /**
     * Called on message pushed to the selected topic
     * @param headers The headers
     * @param payload The message object
     */
    @Override
    public void handleFrame(StompHeaders headers, Object payload)
    {
        String[] msg = ((String) payload).split("\\|");

        // If the sender is the same session ignore the message
        if (msg[msg.length - 1].equals(session.getSessionId()))
        {
            return;
        }

        // Call the appropriate method
        String action = msg[0];

        switch (action)
        {
            case "updateNote":
                noteManager
                        .updateNote(Long.parseLong(msg[1]), msg[2]);
                break;
            case "deleteNote":
                noteManager
                        .deleteNoteNotif(Long.parseLong(msg[1]), msg[2]);
                break;
            case "addedNote":
                noteManager
                        .addNoteNotif(Long.parseLong(msg[1]), msg[2]);
                break;
            case "moveNoteLocal":
                noteManager
                        .moveNoteLocalNotif(Long.parseLong(msg[1]), Long.parseLong(msg[2]), msg[3]);
                break;
            case "moveNoteForeign":
                noteManager
                        .moveNoteForeignNotif(Long.parseLong(msg[1]),
                                Long.parseLong(msg[2]), msg[3]);
                break;
            case "updateCollection":
                collectionManager.updateCollectionNotif(Long.parseLong(msg[1]), msg[2]);
                break;
            case "deleteCollection":
                collectionManager.deleteCollectionNotif(Long.parseLong(msg[1]), msg[2]);
                break;
        }
    }
}
