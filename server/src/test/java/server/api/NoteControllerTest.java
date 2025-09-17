package server.api;

import commons.ErrorCodes;
import commons.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import server.database.CollectionRepository;
import server.database.NoteRepository;
import server.services.NoteService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NoteControllerTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private CollectionRepository collectionRepository;
    private NoteService noteService;

    private NoteController noteController;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        noteService = new NoteService(noteRepository, collectionRepository);
        noteController = new NoteController(noteService);
    }

    @Test
    void getAll()
    {
        List<Note> mockNotes = List.of(new Note(1L, "Test Note", "This is a test note."));
        when(noteRepository.findAll()).thenReturn(mockNotes);

        List<Note> result = noteController.getAll();

        assertEquals(mockNotes, result);
        verify(noteRepository, times(1)).findAll();
    }

    @Test
    void getByIdValid()
    {
        Note mockNote = new Note(1L, "Test Note", "This is a test note.");
        when(noteRepository.findById(1L)).thenReturn(Optional.of(mockNote));

        ResponseEntity<Note> result = noteController.getById(1L);

        assertEquals(ResponseEntity.ok(mockNote), result);
        verify(noteRepository, times(1)).findById(1L);
    }

    @Test
    void getByIdInvalid()
    {
        when(noteRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Note> result = noteController.getById(99L);

        assertEquals(ResponseEntity.badRequest().build(), result);
        verify(noteRepository, times(1)).findById(99L);
    }

    @Test
    void createValid()
    {
        long collectionId = 1L;
        Note mockNote = new Note(collectionId, "My note 1", "A new note :)");
        when(collectionRepository.existsById(collectionId)).thenReturn(true);
        when(noteRepository.save(mockNote)).thenReturn(mockNote);

        ResponseEntity<Note> result = noteController.create(collectionId);

        assertEquals(ResponseEntity.ok(mockNote), result);
        verify(collectionRepository, times(1)).existsById(collectionId);
        verify(noteRepository, times(1)).save(mockNote);
    }

    @Test
    void createInvalidCollectionId()
    {
        long invalidCollectionId = -1L;
        ResponseEntity<Note> result = noteController.create(invalidCollectionId);
        assertEquals(ResponseEntity.badRequest().build(), result);
        verifyNoInteractions(noteRepository);
        verifyNoInteractions(collectionRepository);
    }

    @Test
    void moveNoteInvalid()
    {
        ResponseEntity<Note> response = noteController.move(null, "foreign", 1L);
        assertEquals(ResponseEntity.badRequest().build(), response);
        verify(noteRepository, never()).save(any());
    }
    @Test
    void moveNoteTitleTaken()
    {
        Note note = new Note(1L, "Test Note", "This is a test note.");
        when(collectionRepository.existsById(2L)).thenReturn(true);
        when(noteRepository.existsByTitleAndCollectionId(note.getTitle(), 2L)).thenReturn(true);
        ResponseEntity<Note> response = noteController.move(note, "local", 2L);
        assertEquals(ResponseEntity.status(ErrorCodes.INVALID_NOTE_TITLE.getCode()).build(), response);
        verify(noteRepository, times(1)).existsByTitleAndCollectionId(note.getTitle(), 2L);
        verify(collectionRepository, times(1)).existsById(2L);
        verify(noteRepository, never()).save(note);
    }
    @Test
    void moveNoteLocal()
    {
        Note note = new Note(1L, "Test Note", "This is a test note.");
        when(collectionRepository.existsById(2L)).thenReturn(true);
        when(noteRepository.existsByTitleAndCollectionId(note.getTitle(), 2L)).thenReturn(false);
        when(noteRepository.save(note)).thenReturn(note);
        ResponseEntity<Note> response = noteController.move(note, "local", 2L);
        assertEquals(2L, response.getBody().getCollectionId());
        assertSame(note, response.getBody());
        verify(noteRepository, times(1)).existsByTitleAndCollectionId(note.getTitle(), 2L);
        verify(collectionRepository, times(1)).existsById(2L);
        verify(noteRepository, times(1)).save(note);
    }
    @Test
    void moveNoteForeign()
    {
        Note note = new Note(1L, "Test Note", "This is a test note.");
        when(collectionRepository.existsById(2L)).thenReturn(true);
        when(noteRepository.existsByTitleAndCollectionId(note.getTitle(), 2L)).thenReturn(false);
        when(noteRepository.save(note)).thenReturn(note);
        ResponseEntity<Note> response = noteController.move(note, "foreign", 2L);
        assertEquals(2L, response.getBody().getCollectionId());
        assertNotSame(response.getBody(), note);
        verify(noteRepository, times(1)).existsByTitleAndCollectionId(note.getTitle(), 2L);
        verify(collectionRepository, times(1)).existsById(2L);
        verify(noteRepository, times(1)).save(response.getBody());
    }
    @Test
    void moveNoteInvalidCommand()
    {
        Note note = new Note(1L, "Test Note", "This is a test note.");
        when(collectionRepository.existsById(2L)).thenReturn(true);
        when(noteRepository.existsByTitleAndCollectionId(note.getTitle(), 2L)).thenReturn(false);
        when(noteRepository.save(note)).thenReturn(note);
        ResponseEntity<Note> response = noteController.move(note, "invalid", 2L);
        assertEquals(ResponseEntity.badRequest().build(), response);
        verify(noteRepository, times(1)).existsByTitleAndCollectionId(note.getTitle(), 2L);
        verify(collectionRepository, times(1)).existsById(2L);
        verify(noteRepository, never()).save(note);
    }

    @Test
    void deleteValid()
    {
        long noteId = 1L;
        when(noteRepository.existsById(noteId)).thenReturn(true);
        doNothing().when(noteRepository).deleteById(noteId);
        when(noteRepository.findAll()).thenReturn(List.of());

        List<Note> result = noteController.delete(noteId);

        assertEquals(List.of(), result);
        verify(noteRepository, times(1)).existsById(noteId);
        verify(noteRepository, times(1)).deleteById(noteId);
        verify(noteRepository, times(1)).findAll();
    }

    @Test
    void deleteInvalid()
    {
        long noteId = 1L;
        when(noteRepository.existsById(noteId)).thenReturn(false);
        doNothing().when(noteRepository).deleteById(noteId);
        when(noteRepository.findAll()).thenReturn(List.of());

        List<Note> result = noteController.delete(noteId);

        assertEquals(List.of(), result);
        verify(noteRepository, times(1)).existsById(noteId);
        verify(noteRepository, times(0)).deleteById(noteId);
        verify(noteRepository, times(1)).findAll();
    }

    @Test
    void updateValid()
    {
        Note updatedNote = new Note(1L, "Updated Note", "Updated content");
        when(noteRepository.existsById(updatedNote.getId())).thenReturn(true);
        when(noteRepository.existsByTitleAndCollectionIdAndIdNot(updatedNote.getTitle(), updatedNote.getCollectionId(), updatedNote.getId())).thenReturn(false);
        when(noteRepository.save(updatedNote)).thenReturn(updatedNote);


        ResponseEntity<Note> result = noteController.update(updatedNote);

        assertEquals(ResponseEntity.ok(updatedNote), result);
        verify(noteRepository, times(1)).existsById(updatedNote.getId());
        verify(noteRepository, times(1)).save(updatedNote);
        verify(noteRepository, times(1)).existsByTitleAndCollectionIdAndIdNot(updatedNote.getTitle(), updatedNote.getCollectionId(), updatedNote.getId());
    }

    @Test
    void updateInvalidNull()
    {
        ResponseEntity<Note> result = noteController.update(null);

        assertEquals(ResponseEntity.badRequest().build(), result);
        verifyNoInteractions(noteRepository);
    }

    @Test
    void updateInvalidNonExistent() {

        Note updatedNote = new Note(99L, "Updated Note", "Updated content");
        when(noteRepository.existsById(updatedNote.getId())).thenReturn(false);
        when(noteRepository.existsByTitleAndCollectionIdAndIdNot(updatedNote.getTitle(), updatedNote.getCollectionId(), updatedNote.getId())).thenReturn(false);
        when(noteRepository.save(updatedNote)).thenReturn(updatedNote);


        ResponseEntity<Note> result = noteController.update(updatedNote);
        assertEquals(ResponseEntity.badRequest().build(), result);
        verify(noteRepository, times(1)).existsById(updatedNote.getId());
        verify(noteRepository, never()).save(updatedNote);
        verify(noteRepository, never()).existsByTitleAndCollectionIdAndIdNot(updatedNote.getTitle(), updatedNote.getCollectionId(), updatedNote.getId());

    }
}
