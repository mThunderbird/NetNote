package client.src;

import commons.Note;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

public class ClientNote
{
    private HBox noteBox;
    private Note noteData;
    private Button deleteButton;
    private static final String NOTEBOX_STYLE = """
            -fx-background-radius: 5px;
            -fx-background-color: -bg-secondary;
            -fx-border-color: -border;
            -fx-border-width: 2px;
            -fx-border-radius: 5px;
            -fx-text-fill: -text-primary;""";

    private static final String HIGHLIGHT_NOTEBOX_STYLE = """
            -fx-background-radius: 5px;
            -fx-background-color: -accent-blue;
            -fx-border-color: -border;
            -fx-border-width: 2px;
            -fx-border-radius: 5px;
            -fx-text-fill: -text-secondary;""";

    private String serverURL;
    private boolean isOnline;

    private NoteManager noteManager;

    /**
     * Create a new ClientNote using a Note for data
     * and a reference to the NoteManager class.
     * @param noteData The data for the note
     * @param serverURL The server that hosts this note
     * @param noteManager The NoteManager class reference
     */
    public ClientNote(Note noteData, String serverURL, NoteManager noteManager)
    {
        this.noteData = noteData;
        this.serverURL = serverURL;
        this.isOnline = true;
        this.noteManager = noteManager;
    }

    /**
     * Create a horizontal container for a note
     * in the list.
     * It is made of a label for the note title
     * and a delete button.
     * Also sets note.noteBox to the created HBox.
     * The label has an on-click action onTitleClick().
     * The delete button has an on-click action deleteNote().
     *
     * @param note The note to create the box for
     * @return The HBox FXML component
     */
    public static HBox createNoteBox(ClientNote note)
    {
        // Create a horizontal container for the
        // title and delete button
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        hbox.setPrefHeight(40.0);
        hbox.setFocusTraversable(true);
        hbox.setOnKeyPressed(e ->
        {
            if (e.getCode() == KeyCode.ENTER)
                note.onTitleClick();
        });
        hbox.setOnMouseClicked(e ->
        {
            note.onTitleClick();
        });

        // Create a label for the note title
        // that fills all available space
        Label label = new Label(note.getNoteTitle());
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.setFocusTraversable(false);
        label.setStyle("-fx-text-fill: inherit;");

        // Padding on label
        HBox.setMargin(label, new Insets(0, 0, 0, 10));

        HBox.setHgrow(label, Priority.ALWAYS);


        // Create a delete button
        Button button = new Button("🗑");
        button.setAlignment(Pos.CENTER);
        button.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        button.setFont(new Font(14.0));
        button.setFocusTraversable(true);
        button.setStyle("""
                -fx-background-color: -accent-red;
                -fx-background-radius: 5px;
                -fx-background-insets: 1px;
                -fx-border-color: -border;
                -fx-border-width: 2px;
                -fx-border-radius: 5px;
                -fx-text-fill: -text-secondary;
                """);

        HBox.setMargin(button, new Insets(5, 5, 5, 0));

        // Clicking the delete button should delete
        // the note from the NoteManager, propagate
        // the change to the server, and update the UI
        button.setOnAction(e ->
        {
            note.deleteNote();
        });
        button.setVisible(false);
        note.deleteButton = button;

        hbox.getChildren().addAll(label, button);
        
        hbox.setStyle(NOTEBOX_STYLE);
        note.noteBox = hbox;
        return hbox;
    }

    /**
     * Toggles if delete button is visible
     * @param isVisible visible if true, not visible if false
     */
    public void toggleDeleteButton(boolean isVisible)
    {
        if(deleteButton != null)
        {
            deleteButton.setVisible(isVisible);
        }
    }

    private void onTitleClick()
    {
        noteManager.setCurrentNote(this);
    }

    private void deleteNote()
    {
        boolean confirmed = noteManager.getPopUpManager().showNoteDeleteConfirmation();

        if (!confirmed)
            return;

        noteManager.deleteNote(this);
    }

    public void setNoteTitle(String title)
    {
        noteData.setTitle(title);
    }

    public void setNoteBody(String body)
    {
        noteData.setBody(body);
    }

    public void setNoteCollectionId(Long collectionId)
    {
        noteData.setCollectionId(collectionId);
    }

    public void setOnline(boolean online)
    {
        isOnline = online;
    }

    public String getNoteTitle()
    {
        return noteData.getTitle();
    }

    public Long getNoteId()
    {
        return noteData.getId();
    }

    public String getNoteBody()
    {
        return noteData.getBody();
    }

    public Long getNoteCollectionId()
    {
        return noteData.getCollectionId();
    }

    public HBox getNoteBox()
    {
        return noteBox;
    }

    public Note getNoteData()
    {
        return noteData;
    }

    public String getServerURL()
    {
        return serverURL;
    }

    /**
     * Check if the note is online
     * @return true if the note is online, false otherwise
     */
    public boolean isOnline()
    {
        return isOnline;
    }

    @Override
    public boolean equals(Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, MULTI_LINE_STYLE);
    }

    /**
     * Toggle the highlighting of a note box
     * @param isHighlighted true to activate highlighting, false otherwise
     */
    public void highlight(boolean isHighlighted)
    {
        if (isHighlighted)
        {
            noteBox.setStyle(HIGHLIGHT_NOTEBOX_STYLE);
        }
        else
        {
            noteBox.setStyle(NOTEBOX_STYLE); // Resets to default
        }
    }

    public void setServerURL(String address)
    {
        serverURL = address;
    }

    public void setNoteData(Note noteData)
    {
        this.noteData = noteData;
    }
}
