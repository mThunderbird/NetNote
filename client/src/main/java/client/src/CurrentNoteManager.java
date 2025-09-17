package client.src;

import client.utils.LanguageManager;
import client.utils.ServerUtils;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.util.Duration;

import java.util.Timer;
import java.util.TimerTask;

public class CurrentNoteManager
{
    private TextField currentNoteTitle;
    private TextArea currentNoteBody;
    private ChoiceBox<String> cbNoteMoveCollection;
    private Label lMoveCollection;

    private ClientNote currentNote;
    private Timer titleTimer;
    private Timer bodyTimer;
    private MarkdownParser markdownParser;

    private Tooltip ttInvalidTitle;
    private Tooltip ttEmptyTitle;

    private static final int SAVING_DELAY = 500;
    private static final int MAX_TITLE_LENGTH = 20;
    private static final int CHARACTER_SAVE_THRESHOLD = 10;

    @Inject
    private ServerUtils serverUtils;

    @Inject
    private LanguageManager languageManager;

    @Inject
    private CollectionManager collectionManager;

    @Inject
    private PopUpManager popUpManager;

    /**
     * Create a new CurrentNoteManager using a TextField and a TextArea
     */
    public CurrentNoteManager()
    {

    }

    /**
     * Set the text fields and the webview to be used by the manager
     *
     * @param currentNoteTitle     The title text field
     * @param currentNoteBody      The body text field
     * @param cbNoteMoveCollection
     * @param lMoveCollection
     * @param wv                   The webview to render markdown
     */
    public void initialize(TextField currentNoteTitle,
                           TextArea currentNoteBody,
                           ChoiceBox<String> cbNoteMoveCollection,
                           Label lMoveCollection, WebView wv)
    {
        this.currentNoteTitle = currentNoteTitle;
        this.currentNoteBody = currentNoteBody;
        this.cbNoteMoveCollection = cbNoteMoveCollection;
        this.lMoveCollection = lMoveCollection;

        this.markdownParser = new MarkdownParser(wv);

        this.ttInvalidTitle = new Tooltip();
        this.ttInvalidTitle.getStyleClass().add("tooltip");
        // Set the show delay to 0 (immediate)
        this.ttInvalidTitle.setShowDelay(Duration.ZERO);
        this.ttInvalidTitle.setShowDuration(Duration.INDEFINITE);
        this.ttInvalidTitle.setHideDelay(Duration.ZERO);

        this.ttEmptyTitle = new Tooltip();
        this.ttEmptyTitle.getStyleClass().add("tooltip");

        this.ttEmptyTitle.setShowDelay(Duration.ZERO);
        this.ttEmptyTitle.setShowDuration(Duration.INDEFINITE);
        this.ttEmptyTitle.setHideDelay(Duration.ZERO);

        currentNoteTitle.getStyleClass().add("note-title");

        //lock the typing areas
        disable();

        currentNoteTitle.textProperty()
                .addListener((observable, oldValue, newValue) -> onTitleTyping(newValue));
        currentNoteBody.textProperty()
                .addListener((observable, oldValue, newValue) -> onBodyTyping(newValue));
        cbNoteMoveCollection.setOnAction(event -> onNoteMove());

        ttInvalidTitle.textProperty().bind(languageManager.getProperty("invalid_title"));
        ttEmptyTitle.textProperty().bind(languageManager.getProperty("empty_title"));
    }

    /**
     * After a change in the note, syncs the changes
     * To appear in the fields and the webview
     */
    public void syncChanges()
    {
        if (currentNote == null)
        {
            return;
        }

        this.currentNoteTitle.setText(currentNote.getNoteTitle());
        this.currentNoteBody.setText(currentNote.getNoteBody());
        setupCollectionSelect();
        markdownParser.renderMarkdownToWebView(currentNote.getNoteBody());
    }

    /**
     * On change in the title, sets a timer that
     * calls the saveTitleChanges method in <SAVING_DELAY> seconds.
     * @param newTitle The new body input
     */
    private void onTitleTyping(String newTitle)
    {
        if (currentNote == null)
        {
            return;
        }

        // Cancel any previously scheduled save
        if (titleTimer != null)
        {
            titleTimer.cancel();
        }

        if (newTitle.length() > MAX_TITLE_LENGTH)
        {
            currentNoteTitle.setText(newTitle.substring(0, MAX_TITLE_LENGTH));
            return;
        }

        // Cancel if you went back to the original state
        if (currentNote.getNoteTitle().equals(newTitle))
        {
            currentNoteTitle.getStyleClass().remove("note-title-invalid");
            currentNoteTitle.setTooltip(null);
            return;
        }

        // Schedule a new save task
        titleTimer = new Timer();
        titleTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                Platform.runLater(() ->
                {
                    saveTitleChanges(newTitle);
                });
            }
        }, SAVING_DELAY); // 3 seconds delay
    }

    /**
     * On change in the body, sets a timer that
     * calls the saveBodyChanges method in <SAVING_DELAY> seconds.
     * @param newBody The new body input
     */
    private void onBodyTyping(String newBody)
    {
        if (currentNote == null)
        {
            return;
        }

        markdownParser.renderMarkdownToWebView(newBody);

        // Cancel any previously scheduled save
        if (bodyTimer != null)
        {
            bodyTimer.cancel();
        }

        // Cancel if you went back to the original state
        if (currentNote.getNoteBody().equals(newBody))
        {
            return;
        }

        if (Math.abs(currentNote.getNoteBody().length() - newBody.length())
                > CHARACTER_SAVE_THRESHOLD)
        {
            saveBodyChanges(newBody);
            bodyTimer.cancel();
            return;
        }

        // Schedule a new save task
        bodyTimer = new Timer();
        bodyTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                Platform.runLater(() ->
                {
                    saveBodyChanges(newBody);
                });
            }
        }, SAVING_DELAY); // 3 seconds delay
    }

    private void onNoteMove()
    {
        String selectedCollectionNickname = cbNoteMoveCollection.getValue();
        ClientCollection selectedCollection = collectionManager
                .getCollectionByNickName(selectedCollectionNickname);

        if (selectedCollection == null ||
                currentNote.getNoteCollectionId() == selectedCollection.getId() &&
                currentNote.getServerURL().equals(selectedCollection.getServerURL()) ||
                !selectedCollection.isOnline())
            return;

        try
        {
            ClientNote updated = serverUtils
                    .moveNoteIntoCollection(selectedCollection.getServerURL(),
                            currentNote, selectedCollection.getId());

            // If it updates the note, update the current note
            currentNote.setServerURL(updated.getServerURL());
            currentNote.setNoteData(updated.getNoteData());
        }
        catch (IllegalArgumentException e)
        {
            // If the update fails, show an error notification, restore the previous selected val
            // in the combo box
            popUpManager.cannotMoveNoteErrorNotification();
            Platform.runLater(()->
            {
                cbNoteMoveCollection
                        .setValue(collectionManager
                                .getCollections()
                                .stream()
                                .filter(x -> x.getId() == currentNote.getNoteCollectionId() &&
                                        x.getServerURL().equals(currentNote.getServerURL()))
                                .findFirst()
                                .map(ClientCollection::getNickname)
                                .orElse(""));
            });
        }
    }

    /**
     * Changes the title of the note in the note list and
     * in the client note object. Also sends an update request to
     * the server to sync changes.
     * @param newTitle The new title to save
     */
    private void saveTitleChanges(String newTitle)
    {
        if (currentNote != null && currentNote.isOnline())
        {
            String oldTitle = currentNote.getNoteTitle();

            try
            {
                currentNote.setNoteTitle(newTitle);
                serverUtils.updateNote(currentNote.getServerURL() ,currentNote.getNoteData());
                // If the title is invalid this line will throw an exception,
                // and we will handle it in the catch block

                // If it is valid, update the title in the UI, and reset the error indicators

                ((Label) (currentNote.getNoteBox().getChildren().getFirst())).setText(newTitle);

                currentNoteTitle.getStyleClass().remove("note-title-invalid");
                if (!currentNoteTitle.getStyleClass().contains("note-title"))
                {
                    currentNoteTitle.getStyleClass().add("note-title");
                }
                currentNoteTitle.setTooltip(null);
            }
            catch (IllegalArgumentException e)
            {
                // Handle the exception
                // The title is invalid
                // Return to previous title but keep the incorrect one in the UI box
                // and activate the error indicators
                currentNote.setNoteTitle(oldTitle);
                currentNoteTitle.getStyleClass().remove("note-title");

                if (!currentNoteTitle.getStyleClass().contains("note-title-invalid"))
                {
                    currentNoteTitle.getStyleClass().add("note-title-invalid");
                    if(newTitle.isEmpty())
                    {
                        currentNoteTitle.setTooltip(ttEmptyTitle);
                    }
                    else
                    {
                        currentNoteTitle.setTooltip(ttInvalidTitle);
                    }

                }
            }

        }
    }

    /**
     * Changes the body in the client note object.
     * Also sends an update request to the server to sync changes.
     * @param newBody The new body to save
     */
    private void saveBodyChanges(String newBody)
    {
        if (currentNote != null && currentNote.isOnline())
        {
            try
            {
                currentNote.setNoteBody(newBody);
                serverUtils.updateNote(currentNote.getServerURL(), currentNote.getNoteData());
            }
            catch (Exception _)
            {}
        }
    }

    /**
     * External method to change the current note.
     * It saves the final state of the note.
     * Then loads the new note into the fields and into the webview.
     * @param currentNote The new note to set as current
     */
    public void setCurrentNote(ClientNote currentNote)
    {
        if(this.currentNote != null)
        {
            this.currentNote.highlight(false);
            this.currentNote.toggleDeleteButton(false);
        }

        // Load the new note contents
        this.currentNote = currentNote;

        if(this.currentNote != null)
        {
            currentNoteTitle.setText(currentNote.getNoteTitle());
            currentNoteBody.setText(currentNote.getNoteBody());
            markdownParser.renderMarkdownToWebView(currentNote.getNoteBody());
            this.currentNote.highlight(true);
            this.currentNote.toggleDeleteButton(true);
            // Unlock the typing areas

            enable();

            // Remove any effects from the title field
            currentNoteTitle.getStyleClass().remove("note-title-invalid");
            currentNoteTitle.setTooltip(null);

            setupCollectionSelect();

            if (!this.currentNote.isOnline())
            {
                disable();
                currentNote.toggleDeleteButton(false);
            }
        }
    }

    private void enable()
    {
        currentNoteTitle.setDisable(false);
        currentNoteBody.setDisable(false);
        cbNoteMoveCollection.setDisable(false);
        lMoveCollection.setDisable(false);
    }

    private void disable()
    {
        currentNoteTitle.setDisable(true);
        currentNoteBody.setDisable(true);
        cbNoteMoveCollection.setDisable(true);
        lMoveCollection.setDisable(true);
    }

    /**
     * Refreshes list for collection select if current note not null
     */
    public void refreshCollectionSelect()
    {
        if(currentNote != null)
        {
            setupCollectionSelect();
        }
    }

    private void setupCollectionSelect()
    {
        ObservableList<String> collectionList = FXCollections.observableArrayList();
        String val = "";
        for (ClientCollection collection : collectionManager.getCollections())
        {
            if (collection.isOnline())
            {
                collectionList.add(collection.getNickname());
            }

            if (currentNote.getServerURL().equals(collection.getServerURL()) &&
                    currentNote.getNoteCollectionId().equals(collection.getId()))
            {
                val = collection.getNickname();
            }
        }
        cbNoteMoveCollection.setItems(collectionList);
        cbNoteMoveCollection.setValue(val);
    }

    /**
     * Clears all input fields and the webview
     */
    public void reset()
    {
        if (currentNote != null)
        {
            currentNote.highlight(false);
            currentNote.toggleDeleteButton(false);
        }
        currentNote = null;
        currentNoteTitle.clear();
        currentNoteBody.clear();
        markdownParser.renderMarkdownToWebView("");

        // Lock the typing areas
        disable();

        // Remove any effects from the title field
        currentNoteTitle.getStyleClass().remove("note-title-invalid");
        currentNoteTitle.setTooltip(null);
    }

    public ClientNote getCurrentNote()
    {
        return currentNote;
    }

    /**
     * Save the changes in the current note
     */
    public void saveChanges()
    {
        if (currentNote == null)
        {
            return;
        }

        // Remove highlight from the previously selected note
        this.currentNote.highlight(false);

        saveTitleChanges(currentNoteTitle.getText());
        saveBodyChanges(currentNoteBody.getText());
    }
}
