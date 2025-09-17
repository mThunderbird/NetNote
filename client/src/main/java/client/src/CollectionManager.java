package client.src;

import client.scenes.CollectionCtrl;
import client.utils.Configuration;
import client.utils.MyWebSocketClient;
import client.utils.ServerUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import commons.Collection;
import commons.ErrorCodes;
import jakarta.inject.Inject;
import javafx.application.Platform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CollectionManager
{
    private ClientCollection currentDefault;
    private List<ClientCollection> clientCollections;

    @Inject
    private Configuration configuration;

    @Inject
    private ServerUtils serverUtils;

    @Inject
    private NoteManager noteManager;

    @Inject
    private MyWebSocketClient myWebSocketClient;

    @Inject
    private CollectionCtrl collectionCtrl;

    /**
     * Constructor for CollectionManager
     */
    public CollectionManager()
    {
        this.clientCollections = new ArrayList<>();
    }

    /**
     * Initialize with all collections from config
     * but make them online until a callback from activates them
     */
    public void initialize()
    {
        configuration.getCollections().forEach(collection ->
        {
            collection.setOnline(false);
            clientCollections.add(collection);
        });

        if (clientCollections.isEmpty())
        {
            currentDefault = null;
        }
        else if (clientCollections.size() == 1)
        {
            currentDefault = clientCollections.getFirst();
        }
        else
        {
            // If there is more than 1 collection, we need to find the default one

            long defaultCollectionID = configuration.getDefaultCollection().getId();
            String defaultServerURL = configuration.getDefaultCollection().getServerURL();
            currentDefault = clientCollections.stream()
                    .filter(c -> c.getId() == defaultCollectionID
                            && c.getServerURL().equals(defaultServerURL))
                    .findFirst().orElse(clientCollections.getFirst());
        }
    }

    /**
     * Initialize the CollectionManager with all collections
     * from the configuration that have this server url
     * because this server came online.
     * @param url The server url that came online
     */
    public void serverOnline(String url)
    {
        List<ClientCollection> collections =
                new ArrayList<>(clientCollections
                        .stream().filter(c -> c.getServerURL().equals(url)).toList());

        for (ClientCollection collection : collections)
        {
            // Make sure the collection is up to date with the server
            try
            {
                Collection serverCollection =
                        serverUtils
                                .getCollectionByID(url, collection.getId());
                collection.setCollectionData(serverCollection);
                collection.setOnline(true);
                noteManager.addCollection(collection);
            }
            catch (Exception e)
            {
                if (e.getMessage()
                        .equals(String.valueOf(ErrorCodes.COLLECTION_DOES_NOT_EXIST.getCode())))
                {
                    // The collection was deleted on the server
                    clientCollections.remove(collection);
                    if (collection.equals(currentDefault))
                    {
                        removeCurrentDefault();
                    }
                }
                else if (e.getMessage()
                        .equals(String.valueOf(ErrorCodes.SERVER_UNREACHABLE.getCode())))
                {
                    throw new RuntimeException("Server is unreachable in " +
                            "CollectionManager serverOnline which should not be the case!");
                }
            }
        }
    }

    /**
     * Lock all collections from the server with the given url
     * and send this information to the note manager
     * @param url The server url that went offline
     */
    public void serverOffline(String url)
    {
        for (ClientCollection collection : clientCollections)
        {
            if (collection.getServerURL().equals(url))
            {
                collection.setOnline(false);
            }
        }

        noteManager.serverOffline(url);
    }

    /**
     * This method filters the distinct server urls
     * present in the collections we have subscribed to.
     * Used for establishing websocket connections
     * @return A List<String> with distinct server urls
     */
    public List<String> getDistinctServerURLs()
    {
        return clientCollections.stream().map(ClientCollection::getServerURL).distinct().toList();
    }

    /**
     * Refreshes all collections
     */
    public void refreshCollections()
    {
        clientCollections.forEach(collection ->
        {
            try
            {
                Collection serverCollection =
                        serverUtils.getCollectionByID(collection.getServerURL()
                                , collection.getId());
                collection.setCollectionData(serverCollection);
                collection.setOnline(true);
            }
            catch (Exception e)
            {
                if (e.getMessage().equals(String.valueOf(
                        ErrorCodes.COLLECTION_DOES_NOT_EXIST.getCode())))
                {
                    // The collection was deleted on the server
                    remove(collection);
                    if (collection.equals(currentDefault))
                    {
                        removeCurrentDefault();
                    }
                }
                else if (e.getMessage().equals(String.valueOf(
                        ErrorCodes.SERVER_UNREACHABLE.getCode())))
                {
                    collection.setOnline(false);
                }
            }
        });
    }

    public ClientCollection getCollectionByNickName(String nickName)
    {
        for (ClientCollection collection : clientCollections)
        {
            if (collection.getNickname().equals(nickName))
            {
                return collection;
            }
        }
        return null;
    }

    public List<ClientCollection> getCollections()
    {
        return clientCollections;
    }

    public ClientCollection getCurrentDefault()
    {
        return currentDefault;
    }

    public void setCurrentDefault(ClientCollection currentDefault)
    {
        this.currentDefault = currentDefault;
        configuration.setAndSave(clientCollections, currentDefault);
    }

    /**
     * Method to remove current default
     * If no collection is left in clientCollections, set to null
     * Otherwise set default to first collection in clientCollections
     */
    public void removeCurrentDefault()
    {
        if(clientCollections.isEmpty())
        {
            currentDefault = null;
        }
        else
        {
            currentDefault = clientCollections.getFirst();
        }
        configuration.setAndSave(clientCollections, currentDefault);
    }

    /**
     * Checks if any nicknames match
     * @param nickname the nickname to check for
     * @return true if found, false otherwise
     */
    public boolean checkNickname(String nickname)
    {
        return clientCollections.stream()
                .map(ClientCollection::getNickname)
                .distinct()
                .anyMatch(n -> n.equals(nickname));
    }

    /**
     * Initialize the CollectionManager with all collections from a JSON string.
     * Used for testing purposes.*
     * @param json the JSON string containing collection data
     */
    public void initializeFromJson(String json)
    {
        try
        {
            ObjectMapper om = new ObjectMapper();
            clientCollections = om.readValue(json,
                    new com.fasterxml.jackson.core.type.TypeReference<>() {});
            currentDefault = clientCollections.getFirst();
            System.out.println("Configuration reading successful!");
        }
        catch (IOException e)
        {
            System.err.println("Configuration reading failed!");
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates and adds a ClientCollection to CollectionManager
     * @param data the commons collection data
     * @param nickname the local nickname
     * @param address the server address
     * @return the created ClientCollection instance
     */
    public ClientCollection addCollection(Collection data, String nickname, String address)
    {
        ClientCollection collection = new ClientCollection(data, nickname, address);
        collection.setOnline(false);
        if (! collectionExists(data, address))
        {
            clientCollections.add(collection);
        }
        if(currentDefault == null)
        {
            currentDefault = collection;
        }
        myWebSocketClient.attemptWithSessionCheck(address);
        configuration.setAndSave(clientCollections, currentDefault);
        return collection;
    }

    /**
     * Checks if collection already exists on client
     * @param data the commons collection data
     * @param address the server address
     * @return true if collection already saved
     */
    public boolean collectionExists(Collection data, String address)
    {
        return clientCollections.stream().anyMatch(x ->
                x.getCollectionData().equals(data) &&
                x.getServerURL().equals(address));
    }

    /**
     * Removes the given collection from client collections
     * If it is default, sets default to null
     * @param collection the collection to remove
     */
    public void remove(ClientCollection collection)
    {
        clientCollections.remove(collection);
        if(currentDefault.equals(collection))
        {
            removeCurrentDefault();
        }
        noteManager.removeCollection(collection);
        if(!getDistinctServerURLs().contains(collection.getServerURL()))
        {
            myWebSocketClient.endConnection(collection.getServerURL());
        }
        configuration.setAndSave(clientCollections, currentDefault);
    }

    /**
     * The start for handling notifications about collection update
     * @param id the updated collection's id
     * @param address the updated collection's server address
     */
    public void updateCollectionNotif(long id, String address)
    {
        ClientCollection collection = clientCollections
                .stream()
                .filter(x -> x.getId() == id && x.getServerURL().equals(address))
                .findFirst().orElse(null);
        if(collection == null)
        {
            return;
        }
        try
        {
            collection.setCollectionData(serverUtils.getCollectionByID(address, id));
            Platform.runLater(() -> collectionCtrl.updateNotif(collection));
        }
        catch (Exception _)
        { }
    }

    /**
     * The start for handling notifications about collection delete
     * @param id the deleted collection's id
     * @param address the deleted collection's server address
     */
    public void deleteCollectionNotif(long id, String address)
    {
        ClientCollection collection = clientCollections
                .stream()
                .filter(x -> x.getId() == id && x.getServerURL().equals(address))
                .findFirst().orElse(null);
        if(collection == null)
        {
            return;
        }
        Platform.runLater(() ->
        {
            remove(collection);
            collectionCtrl.deleteNotif(collection);
        });
    }
}
