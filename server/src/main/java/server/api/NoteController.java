package server.api;

import commons.Note;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.services.NoteService;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController
{

    private final NoteService noteService;

    /**
     * Constructor for dependency injection.
     *
     * @param noteService The note service.
     */
    public NoteController(NoteService noteService)
    {
        this.noteService = noteService;
    }

    /**
     * Return all notes
     * @return the list of notes
     */
    @GetMapping("/")
    public List<Note> getAll()
    {
        return noteService.getAllNotes();
    }

    /**
     * Return note by ID
     * @param id the note ID to look for
     * @return either the note with corresponding ID or bad request
     */
    @GetMapping("/{id}")
    public ResponseEntity<Note> getById(@PathVariable("id") long id)
    {
        return noteService.getNoteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping("/byCollectionId/{collectionId}")
    public ResponseEntity<List<Note>> getByCollectionId(@PathVariable("collectionId")
                                                            long collectionId)
    {
        return noteService.getNotesByCollectionId(collectionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    /**
     * Create new note as requested
     * @param collectionId the collection ID for the new note
     * @return the new note or bad request
     */
    @PostMapping("/create")
    public ResponseEntity<Note> create(@RequestBody long collectionId)
    {
        try
        {
            Note newNote = noteService.createNote(collectionId);
            return ResponseEntity.ok(newNote);
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Move note to another collection
     * @param note The note to move
     * @param source The source can be either foreign or local
     * foreign -> note is from another server
     * local -> note from a collection on this server
     * @param collectionId The collection ID to move the note to
     * @return the note if move worked, otherwise bad request
     */
    @PostMapping("/move/{source}/{collectionId}")
    public ResponseEntity<Note> move(@RequestBody Note note,
                                     @PathVariable String source,
                                     @PathVariable long collectionId)
    {
        return noteService.moveNoteToCollection(note, source, collectionId);
    }

    /**
     * Delete note by ID
     * @param id the note ID
     * @return the current list of notes
     */
    @PostMapping("/delete")
    public List<Note> delete(@RequestBody long id)
    {
        noteService.deleteNoteById(id);
        return noteService.getAllNotes();
    }

    /**
     * Update the given note in DB if it exists there
     * @param note the already updated note
     * @return the note if update worked, otherwise bad request
     */
    @PostMapping("/update")
    public ResponseEntity<Note> update(@RequestBody Note note)
    {
        return noteService.updateNote(note);
    }
}

