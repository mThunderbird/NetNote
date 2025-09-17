package client.src;

import commons.Note;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClientNoteTest
{
    private Note noteData;
    private NoteManager noteManager;
    private CurrentNoteManager currentNoteManager;
    private VBox notesContainer;
    private TextField currentNoteTitleField;
    private TextArea currentNoteBodyArea;
    private ClientNote clientNote;


    //    @BeforeAll
//    public static void initToolkit()
//    {
//        Platform.startup(() -> {});
//    }
    @BeforeEach
    public void setUp()
    {
        noteData = new Note(1, "New Collection", "Haha note");
//        notesContainer = new VBox();
//        currentNoteTitleField = new TextField();
//        currentNoteBodyArea = new TextArea();
//        currentNoteManager = new CurrentNoteManager(currentNoteTitleField, currentNoteBodyArea);
//        noteManager = new NoteManager(notesContainer, currentNoteManager);
        clientNote = new ClientNote(noteData, "", null);
    }

    //    @Test
//    public void setCurrentNoteTest()
//    {
//        noteManager.setCurrentNote(clientNote);
//        assertEquals("New Collection", currentNoteTitleField.getText());
//        assertEquals("Haha note", currentNoteBodyArea.getText());
//        assertEquals(clientNote, currentNoteManager.getCurrentNote());
//    }
    @Test
    public void getNoteTitleTest()
    {
        assertEquals("New Collection", clientNote.getNoteTitle());
    }

    @Test
    public void getNoteBodyTest()
    {
        assertEquals("Haha note", clientNote.getNoteBody());
    }

    @Test
    public void getNoteCollectionIdTest()
    {
        assertEquals(1, clientNote.getNoteCollectionId());
    }

    @Test
    public void setNoteTitleTest()
    {
        clientNote.setNoteTitle("Update the title");
        assertEquals("Update the title", clientNote.getNoteTitle());
    }

    @Test
    public void setNoteBodyTest()
    {
        clientNote.setNoteBody("Update the body");
        assertEquals("Update the body", clientNote.getNoteBody());
    }

    @Test
    public void setNoteCollectionIdTest()
    {
        clientNote.setNoteCollectionId((long) 5);
        assertEquals((long) 5, clientNote.getNoteCollectionId());
    }

    @Test
    public void equalsSameTest()
    {
        assertTrue(clientNote.equals(clientNote));
    }

    @Test
    public void equalsDifferentTitleTest()
    {
        Note otherNote = new Note(1, "This Collection", "Haha note");
        ClientNote otherClientNote = new ClientNote(otherNote, "", null);
        assertFalse(clientNote.equals(otherClientNote));
    }

    @Test
    public void hashcodeSameTest()
    {
        assertEquals(clientNote.hashCode(), clientNote.hashCode());
    }

    @Test
    public void hashcodeDifferentBodyAndTitleTest()
    {
        Note otherNote = new Note(1, "This Collection", "Hello note");
        ClientNote otherClientNote = new ClientNote(otherNote, "", null);
        assertNotEquals(clientNote.hashCode(), otherClientNote.hashCode());
    }

    @Test
    public void toStringTest()
    {
        String result = clientNote.toString();
        assertTrue(result.contains("New Collection"));
    }
}