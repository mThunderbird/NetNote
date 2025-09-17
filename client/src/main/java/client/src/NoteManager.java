package client.src;

import client.utils.LanguageManager;
import client.utils.ServerUtils;
import commons.Note;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class NoteManager
{
    private List<ClientNote> availableNotes;

    private VBox notesContainer;

    private TextField searchBar;

    private ChoiceBox<String> cbCollectionFilter;
    
    @Inject
    private ServerUtils serverUtils;
    
    @Inject
    private CurrentNoteManager currentNoteManager;
    
    @Inject 
    private CollectionManager collectionManager;

    @Inject
    private LanguageManager languageManager;

    @Inject
    private PopUpManager popUpManager;

    /**
     * Constructor for NoteManager
     */
    public NoteManager() {}

    /**
     * Initialize the NoteManager with the note list, search bar, and collection filter
     * @param vbNoteList The note list
     * @param tfSearchBar The search bar
     * @param cbCollectionFilter The collection filter
     */
    public void initialize(VBox vbNoteList,
                           TextField tfSearchBar,
                           ChoiceBox<String> cbCollectionFilter)
    {
        this.availableNotes = new ArrayList<>();

        this.notesContainer = vbNoteList;
        this.searchBar = tfSearchBar;
        this.cbCollectionFilter = cbCollectionFilter;
    }

    /**
     * Go through each collection we have subscribed to and fetch
     * the notes that should be shown to the client.
     * @param collection The collection to add notes from
     */
    public void addCollection(ClientCollection collection)
    {
        try
        {
            List<Note> notes = serverUtils.
                    getNotesByServerCollection(collection.getServerURL(), collection.getId());

            for (Note note : notes)
            {
                boolean found = false;

                for (ClientNote clientNote : availableNotes)
                {
                    if (note.getId() == clientNote.getNoteId()
                            && collection.getServerURL().equals(clientNote.getServerURL()))
                    {
                        clientNote.setOnline(true);
                        found = true;
                    }
                }

                if (!found)
                    Platform.runLater(() -> addNote(note, collection.getServerURL()));
            }
            Platform.runLater(() -> setCurrentNote(currentNoteManager.getCurrentNote()));
        }
        catch (Exception _)
        { }
    }

    /**
     * Lock all notes that are on this server
     * @param url The url of the server that went offline
     */
    public void serverOffline(String url)
    {
        for(ClientNote note : availableNotes)
        {
            if(note.getServerURL().equals(url))
            {
                note.setOnline(false);
            }
        }
        Platform.runLater(() -> setCurrentNote(currentNoteManager.getCurrentNote()));
    }

     /**
     * Add a note to the list of available and current notes
     * and to the UI.
     *
     * @param note The note to add
     * @param serverURL The url of the server that hosts this note
     * @return The created client note
     */
    public ClientNote addNote(Note note, String serverURL)
    {
        ClientNote newNote = new ClientNote(note, serverURL, this);
        availableNotes.add(newNote);
        notesContainer.getChildren().add(ClientNote.createNoteBox(newNote));
        updateVisibility(newNote);

        return newNote;
    }

    /**
     * Delete a note from the list of available and current notes
     * and from the UI.
     *
     * @param note The note to delete
     */
    public void deleteNote(ClientNote note)
    {
        try
        {
            serverUtils
                    .deleteNote(note.getServerURL(), note.getNoteId());

            // Remove note from the UI
            notesContainer.getChildren().remove(note.getNoteBox());

            // Remove note from the lists
            availableNotes.remove(note);

            if (currentNoteManager.getCurrentNote() != null &&
                    currentNoteManager.getCurrentNote().equals(note))
            {
                availableNotes
                        .stream()
                        .filter(this::filterCheck).findFirst()
                        .ifPresentOrElse(currentNoteManager
                                ::setCurrentNote, currentNoteManager
                                ::reset);
            }
        }
        catch (Exception _)
        {}
    }

    /**
     * Foreach collection we have subscribed to, fetch the notes
     * and update the notes on the client. This is a massive update
     * and uses all the available notes because it is called
     * manually with the refresh button.
     */
    public void refreshNotes()
    {
        for (ClientCollection cc : collectionManager
                .getCollections())
        {
            if (!cc.isOnline())
                continue;
            try
            {
                // Fetch the current state of notes on each (collection, server) pair
                List<Note> notes = serverUtils
                        .getNotesByServerCollection(cc.getServerURL(), cc.getId());

                notes.forEach(note ->
                {
                    for (ClientNote availableNote : availableNotes)
                    {
                        // ClientNote is identified by (noteId, serverURL)
                        if (availableNote.getNoteId().equals(note.getId()) &&
                                availableNote.getServerURL().equals(cc.getServerURL()))
                        {
                            ((Label) (availableNote.getNoteBox().getChildren().getFirst()))
                                    .setText(note.getTitle());
                            availableNote.setNoteTitle(note.getTitle());
                            availableNote.setNoteBody(note.getBody());
                            currentNoteManager.syncChanges();
                            return;
                        }
                    }

                    // If the (noteId, serverURL) is not found, it is a new note
                    this.addNote(note, cc.getServerURL());
                });

                availableNotes.removeIf(oldNote ->
                {
                    // if there is no note in notes from the same collection
                    // such that (noteId, serverURL) is the same as oldNote
                    // it has been deleted and should be removed from the UI
                    if (notes.stream().noneMatch
                            (newNote -> newNote.getId() == oldNote.getNoteId()
                                    && cc.getServerURL().equals(oldNote.getServerURL()))
                                    && oldNote.getNoteCollectionId() == cc.getId())
                    {
                        notesContainer.getChildren().remove(oldNote.getNoteBox());
                        return true;
                    }
                    return false;
                });
            }
            catch (Exception _)
            {}
        }
    }

    /**
     * Pushed by server when a note is outdated and
     * should be updated
     * @param noteId The id of the note to update
     * @param serverUrl The url of the server that hosts the note
     */
    public void updateNote(long noteId, String serverUrl)
    {
        try
        {
            Note note = serverUtils
                    .getNoteById(serverUrl, noteId);

            for (ClientNote availableNote : availableNotes)
            {
                if (availableNote.getNoteId().equals(note.getId()) &&
                        availableNote.getServerURL().equals(serverUrl))
                {
                    Platform.runLater(() ->
                    {
                        ((Label) (availableNote.getNoteBox().getChildren().getFirst()))
                                .setText(note.getTitle());
                        // If the changed note is the current note we are editing, sync changes
                        if (currentNoteManager.getCurrentNote() != null &&
                                currentNoteManager.getCurrentNote().equals(availableNote))
                        {
                            availableNote.setNoteTitle(note.getTitle());
                            availableNote.setNoteBody(note.getBody());
                            currentNoteManager.syncChanges();
                        }
                        // Else just update the note data
                        else
                        {
                            availableNote.setNoteTitle(note.getTitle());
                            availableNote.setNoteBody(note.getBody());
                        }
                    });
                }
            }
        }
        catch (Exception _)
        {}
    }

    /**
     * Pushed by server when a note is deleted and
     * should be removed on the client
     * @param noteId The id of the note to delete
     * @param serverUrl The url of the server that hosts the note
     */
    public void deleteNoteNotif(long noteId, String serverUrl)
    {
        for (ClientNote availableNote : availableNotes)
        {
            if (availableNote.getNoteId().equals(noteId) &&
                    availableNote.getServerURL().equals(serverUrl))
            {
                Platform.runLater(() -> deleteNote(availableNote));
                return;
            }
        }
    }

    /**
     * Pushed by server when a note is added and
     * should be added on the client
     * @param noteId The id of the note to add
     * @param serverUrl The url of the server that hosts the note
     */
    public void addNoteNotif(long noteId, String serverUrl)
    {
        try
        {
            Note newNote = serverUtils
                    .getNoteById(serverUrl, noteId);
            Platform.runLater(() -> addNote(newNote, serverUrl));
        }
        catch (Exception _)
        {}
    }

    /**
     * Pushed by server when a note is moved to a different collection
     * but the collection is on the same server so no changes in serverUrl
     * @param noteId The id of the note to move
     * @param collectionId The id of the collection to move to
     * @param serverUrl The url of the server that hosts the note
     */
    public void moveNoteLocalNotif(long noteId, long collectionId, String serverUrl)
    {
        for (ClientNote availableNote : availableNotes)
        {
            if (availableNote.getNoteId().equals(noteId) &&
                    availableNote.getServerURL().equals(serverUrl))
            {
                boolean isSubscribedToNewCollection = collectionManager
                        .getCollections()
                        .stream()
                        .anyMatch(x -> x.getId() == collectionId &&
                                x.getServerURL().equals(serverUrl));
                if (!isSubscribedToNewCollection)
                {
                    Platform.runLater(() -> notesContainer.getChildren()
                            .remove(availableNote.getNoteBox()));
                    availableNotes.remove(availableNote);
                }
                else
                {
                    availableNote.setNoteCollectionId(collectionId);
                    Platform.runLater(() -> currentNoteManager.syncChanges());
                    updateVisibility(availableNote);
                }
            }
        }
    }

    /**
     * Pushed by server when a note is moved to a different collection
     * and the collection is on a different server. This means the note
     * has just been created on the server.
     * @param noteId The id of the note to move
     * @param collectionId The id of the collection to move to
     * @param serverUrl The url of the server that hosts the note
     */
    public void moveNoteForeignNotif(long noteId, long collectionId, String serverUrl)
    {
        boolean isSubscribedToNewCollection = collectionManager
                .getCollections()
                .stream()
                .anyMatch(x -> x.getId() == collectionId &&
                        x.getServerURL().equals(serverUrl));

        if (!isSubscribedToNewCollection)
        {
            return;
        }

        try
        {
            Note newNote = serverUtils
                    .getNoteById(serverUrl, noteId);
            Platform.runLater(() -> addNote(newNote, serverUrl));
        }
        catch (Exception _)
        {}
    }

    public void setCurrentNote(ClientNote newCurrentNote)
    {
        if (newCurrentNote != null)
        {
            newCurrentNote.getNoteBox().requestFocus();
        }
        currentNoteManager.saveChanges();
        currentNoteManager.setCurrentNote(newCurrentNote);
    }

    /**
     * Checks if given client note passes search bar filters
     * and collection filter.
     * @param note the client note to check
     * @return true if filters passed
     */
    public boolean filterCheck(ClientNote note)
    {
        // Apply the collection filter
        boolean collectionFilterResult = filterCollection(note);

        // Apply the search bar filter
        boolean searchFilterResult = filterSearchBar(note);

        // Combine both filters. A note is visible if both the collection and search filters pass.
        return collectionFilterResult && searchFilterResult;
    }


    /**
     Checks if given client note passes filters.
     * Currently has to have search bar text in title
     * or as keywords anywhere in body, both case-insensitive.
     * @param note the client note to check
     * @return true if filters passed
     */
    public boolean filterSearchBar(ClientNote note)
    {
        String search = searchBar.getText().toLowerCase();
        if (note.getNoteTitle().toLowerCase().matches(".*\\Q" + search + "\\E.*"))
        {
            return true;
        }
        String body = note.getNoteBody().toLowerCase();
        for(String s : search.split("\\s+"))
        {
            if(!body.contains(s))
            {
                return false;
            }
            body = body.replaceFirst("\\Q" + s + "\\E", " ");
        }
        return true;
    }

    /**
     * Checks if a client note is in the selected collection.
     * @param note the client note to check
     * @return true if the note is in the selected collection,
     * and false otherwise
     */
    public boolean filterCollection(ClientNote note)
    {
        // Get the selected collection filter
        String selectedCollection = cbCollectionFilter.getValue();

        // If "All Collections" is selected, show all notes
        if (languageManager.getProperty("all_collections").getValue().equals(selectedCollection))
        {
            return true;  // All notes are visible
        }

        // Retrieve the `ClientCollection` object using the collection manager
        ClientCollection collection = collectionManager
                .getCollectionByNickName(selectedCollection);

        // Compare the collection name with the selected filter
        return collection != null && collection.getId() == note.getNoteCollectionId() &&
                collection.getServerURL().equals(note.getServerURL());
    }



    /**
     * Updates visibility of note box in UI based on filterCheck()
     * @param note the note to update
     */
    public void updateVisibility(ClientNote note)
    {
        boolean status = filterCheck(note);
        note.getNoteBox().setVisible(status);
        note.getNoteBox().setManaged(status);
    }

    /**
     * Calls updateVisibility() on all currently available notes
     */
    public void updateVisibilityAll()
    {
        for(ClientNote note: availableNotes)
        {
            updateVisibility(note);
        }
    }

    /**
     * Removes notes
     * @param collection the collection to remove notes for
     */
    public void removeCollection(ClientCollection collection)
    {
        for(ClientNote note: availableNotes)
        {
            if(note.getNoteCollectionId().equals(collection.getId()) &&
                    note.getServerURL().equals(collection.getServerURL()))
            {
                Platform.runLater(() -> notesContainer.getChildren().remove(note.getNoteBox()));
                if (currentNoteManager.getCurrentNote()!=null &&
                        currentNoteManager.getCurrentNote().equals(note))
                {
                    currentNoteManager.reset();
                }
            }
        }
        availableNotes.removeIf(x ->
                x.getNoteCollectionId().equals(collection.getId()) &&
                x.getServerURL().equals(collection.getServerURL()));
    }
    public PopUpManager getPopUpManager()
    {
        return popUpManager;
    }

    /**
     * Shortcut to move to the next note in the list
     */
    public void moveToNextNote()
    {
        int startId = 0;
        ClientNote currentNote = currentNoteManager.getCurrentNote();
        for (int i = 0; i < notesContainer.getChildren().size(); i ++)
        {
            if(currentNote == null)
            {
                startId = 0;
                break;
            }
            if(notesContainer.getChildren().get(i).equals(
                    currentNote.getNoteBox()) &&
                    i + 1 < notesContainer.getChildren().size())
            {
                startId = i + 1;
                break;
            }
        }

        for (int i = startId; i < notesContainer.getChildren().size(); i++)
        {
            if (filterCheck(availableNotes.get(i)))
            {
                this.setCurrentNote(availableNotes.get(i));
                return;
            }
        }
    }

    /**
     * Shortcut to move to the previous note in the list
     */
    public void moveToPreviousNote()
    {
        int startId = 0;
        ClientNote currentNote = currentNoteManager.getCurrentNote();
        for (int i = 0; i < notesContainer.getChildren().size(); i ++)
        {
            if(currentNote == null)
            {
                startId = notesContainer.getChildren().size()-1;
                break;
            }
            if(notesContainer.getChildren().get(i).equals(
                    currentNote.getNoteBox()) && i - 1 >= 0)
            {
                startId = i - 1;
                break;
            }
        }

        for (int i = startId; i >= 0; i--)
        {
            if (filterCheck(availableNotes.get(i)))
            {
                this.setCurrentNote(availableNotes.get(i));
                return;
            }
        }
    }
}
