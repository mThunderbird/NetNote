/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.utils;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;

import client.src.ClientNote;
import commons.Collection;
import commons.ErrorCodes;
import commons.Note;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;

public class ServerUtils
{
    @Inject
    private MyWebSocketClient myWebSocketClient;

    /**
     * Requests a Collection from the server by providing the server url
     * and the collection id. The path is api/collections/{id}
     * @param address The server address
     * @param collectionId The collection id
     * @return The collection with the given id on that server
     */
    public Collection getCollectionByID(String address, long collectionId)
    {
        Response response = ClientBuilder.newClient(new ClientConfig())
                .target(address).path("api/collections/" + collectionId)
                .request(APPLICATION_JSON)
                .get();

        if (response.getStatus() != 200)
        {
            throw new RuntimeException(String.valueOf(response.getStatus()));
        }

        return response.readEntity(Collection.class);
    }

    /**
     * Requests a Collection from the server by providing the server url
     * and the collection title. The path is api/collections/title/{title}
     * @param address The server address
     * @param title The title
     * @return The collection with the given title on that server
     */
    public Collection getCollectionByTitle(String address, String title)
    {
        Response response = ClientBuilder.newClient(new ClientConfig())
                .target(address).path("api/collections/title/" + title)
                .request(APPLICATION_JSON)
                .get();

        if(response.getStatus() == ErrorCodes.COLLECTION_DOES_NOT_EXIST.getCode())
        {
            return null;
        }
        if (response.getStatus() != 200)
        {
            throw new RuntimeException(String.valueOf(response.getStatus()));
        }

        return response.readEntity(Collection.class);
    }

    /**
     * Request a new collection from the given server.
     * You only need to provide the title of the new collection.
     *
     * @param address The server to create the collection on
     * @param title The title for the new collection
     * @return The created collection
     */
    public Collection createCollection(String address, String title)
    {
        Response response = ClientBuilder.newClient(new ClientConfig())
                .target(address).path("api/collections/create")
                .request(APPLICATION_JSON)
                .post(Entity.entity(title, APPLICATION_JSON));

        if (response.getStatus() != 200)
        {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }

        return response.readEntity(Collection.class);
    }

    /**
     * Updates collection on server
     *
     * @param address The server to update the collection on
     * @param collection The updated collection
     */
    public void updateCollection(String address, Collection collection)
    {
        Response response = ClientBuilder.newClient(new ClientConfig())
                .target(address).path("api/collections/update")
                .request(APPLICATION_JSON)
                .post(Entity.entity(collection, APPLICATION_JSON));

        if (response.getStatus() != 200)
        {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }

        myWebSocketClient.sendMessage(
                address,
                "updateCollection|" + collection.getId() + "|" + address + "|"
        );
    }

    /**
     * Deletes collection on server
     *
     * @param address The server to delete the collection on
     * @param id The id of the collection to delete
     */
    public void deleteCollection(String address, long id)
    {
        Response response = ClientBuilder.newClient(new ClientConfig())
                .target(address).path("api/collections/delete")
                .request(APPLICATION_JSON)
                .post(Entity.entity(id, APPLICATION_JSON));

        if (response.getStatus() != 200)
        {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }

        myWebSocketClient
                .sendMessage(address, "deleteCollection|" + id + "|" + address + "|");
    }

    /**
     * Requests a Note from the server by providing the server url and the collection id.
     * The path is api/notes/{id}
     * @param address The server address
     * @param noteId The note id
     * @return The note with the given id on that server
     */
    public Note getNoteById(String address, long noteId)
    {
        Response response = ClientBuilder.newClient(new ClientConfig())
                .target(address).path("api/notes/" + noteId)
                .request(APPLICATION_JSON)
                .get();

        if (response.getStatus() != 200)
        {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }

        return response.readEntity(Note.class);
    }

    /**
     * Requests a List<Note> from the server by providing the server url
     * and the collection id. Notes returned will be from the collection.
     * The path is api/notes/byCollectionId/{id}
     * @param address The server address
     * @param collectionId The collection id
     * @return The list of notes with the given collection id on that server
     */
    public List<Note> getNotesByServerCollection(String address, long collectionId)
    {
        Response response = ClientBuilder.newClient(new ClientConfig())
                .target(address).path("api/notes/byCollectionId/" + collectionId)
                .request(APPLICATION_JSON)
                .get();

        if (response.getStatus() != 200)
        {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }

        return response.readEntity(new GenericType<>() {});
    }

    /**
     * Request a new note from the given server.You only need to provide the
     * id of an existing collection on that server, and you will receive a
     * blank note with an autogenerated valid title
     *
     * @param address The server to create a note on
     * @param collectionId The id of the collection to create the note in
     * @return The created note
     */
    public Note createNote(String address, long collectionId)
    {
        Response response = ClientBuilder.newClient(new ClientConfig())
                .target(address).path("api/notes/create")
                .request(APPLICATION_JSON)
                .post(Entity.entity(collectionId, APPLICATION_JSON));

        if (response.getStatus() != 200)
        {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }

        Note newNote = response.readEntity(Note.class);

        // Note creation was successful -> time to notify other clients of the change
        myWebSocketClient
                .sendMessage(address, "addedNote|" + newNote.getId() + "|" + address + "|");

        return newNote;
    }

    /**
     * Move a note from one collection to another.
     * @param collectionAddress The address of collection to move into
     * @param note The note to move
     * @param collectionId The id of the collection to move the note into
     * @return The moved note
     */
    public ClientNote moveNoteIntoCollection(
            String collectionAddress,
            ClientNote note,
            long collectionId) throws IllegalArgumentException, RuntimeException
    {
        Note temp;
        if (!note.getServerURL().equals(collectionAddress))
        {
            temp = moveNoteForeign(collectionAddress, note, collectionId);
        }
        else
        {
            temp = moveNoteLocal(note, collectionId);
        }
        return new ClientNote(temp, collectionAddress, null);
    }

    private Note moveNoteForeign(String address, ClientNote note, long collectionId)
    {
        Response response = ClientBuilder.newClient(new ClientConfig())
                .target(address).path("api/notes/move/foreign/" + collectionId)
                .request(APPLICATION_JSON)
                .post(Entity.entity(note.getNoteData(), APPLICATION_JSON));

        if (response.getStatus() == ErrorCodes.INVALID_NOTE_TITLE.getCode())
        {
            throw new IllegalArgumentException(
                    String.valueOf(ErrorCodes.INVALID_NOTE_TITLE.getCode()));
        }
        if (response.getStatus() != 200)
        {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }
        Note newNote = response.readEntity(Note.class);
        myWebSocketClient.sendMessage(
                address,
                "moveNoteForeign|" +
                        newNote.getId() + "|" +
                        collectionId + "|" +
                        address + "|"
        );
        deleteNote(note.getServerURL(), note.getNoteId());
        return newNote;
    }

    private Note moveNoteLocal(ClientNote note, long collectionId)
    {
        Response response = ClientBuilder.newClient(new ClientConfig())
                .target(note.getServerURL()).path("api/notes/move/local/" + collectionId)
                .request(APPLICATION_JSON)
                .post(Entity.entity(note.getNoteData(), APPLICATION_JSON));

        if (response.getStatus() == ErrorCodes.INVALID_NOTE_TITLE.getCode())
        {
            throw new IllegalArgumentException(
                    String.valueOf(ErrorCodes.INVALID_NOTE_TITLE.getCode()));
        }
        if (response.getStatus() != 200)
        {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }

        myWebSocketClient.sendMessage(
                note.getServerURL(),
                "moveNoteLocal|" +
                        note.getNoteId() + "|"
                        + collectionId + "|"
                        + note.getServerURL() + "|"
        );

        return response.readEntity(Note.class);
    }

    /**
     * Delete a note from the server.
     * The path is /api/notes/delete.
     * No response is used for now.
     * @param address The address of the server that hosts the note
     * @param noteId The id of the note to delete
     */
    public void deleteNote(String address, long noteId)
    {
        Response response = ClientBuilder.newClient(new ClientConfig())
                .target(address).path("api/notes/delete")
                .request(APPLICATION_JSON)
                .post(Entity.entity(noteId, APPLICATION_JSON));

        if (response.getStatus() != 200)
        {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }

        // Note deletion was successful -> time to notify other clients of the change
        myWebSocketClient
                .sendMessage(address, "deleteNote|" + noteId + "|" + address + "|");
    }

    /**
     * Update a note on the given server.
     * The path is api/notes/update.
     * No response is used for now.
     *
     * @param address The address of the server that hosts the note
     * @param note The note to update
     */
    public void updateNote(String address, Note note)
    {
        Response response = ClientBuilder.newClient(new ClientConfig())
                .target(address).path("api/notes/update")
                .request(APPLICATION_JSON)
                .post(Entity.entity(note, APPLICATION_JSON));

        if (response.getStatus() == ErrorCodes.INVALID_NOTE_TITLE.getCode())
        {
            throw new IllegalArgumentException(String.
                    valueOf(ErrorCodes.INVALID_NOTE_TITLE.getCode()));
        }

        if (response.getStatus() == ErrorCodes.EMPTY_NOTE_TITLE.getCode())
        {
            throw new IllegalArgumentException(String.
                    valueOf(ErrorCodes.EMPTY_NOTE_TITLE.getCode()));
        }

        if (response.getStatus() != 200)
        {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }

        // Note update was successful -> time to notify other clients of the change
        myWebSocketClient
                .sendMessage(address, "updateNote|" + note.getId() + "|" + address + "|");
    }
}