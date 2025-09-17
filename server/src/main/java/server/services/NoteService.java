package server.services;

import commons.ErrorCodes;
import commons.Note;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import server.database.CollectionRepository;
import server.database.NoteRepository;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@Service
public class NoteService
{

    private final NoteRepository notes;
    private final CollectionRepository collections;

    /**
     * Constructor for dependency injection.
     *
     * @param notes       The note repository.
     * @param collections The collection repository.
     */
    public NoteService(NoteRepository notes, CollectionRepository collections)
    {
        this.notes = notes;
        this.collections = collections;
    }

    /**
     * Retrieve all notes.
     *
     * @return The list of all notes.
     */
    public List<Note> getAllNotes()
    {
        return notes.findAll();
    }

    /**
     * Retrieve a note by its ID.
     *
     * @param id The ID of the note.
     * @return An optional containing the note if found.
     */
    public Optional<Note> getNoteById(long id)
    {
        if (id < 0)
        {
            return Optional.empty();
        }
        return notes.findById(id);
    }

    /**
     * Retrieve all notes for a specific collection.
     *
     * @param collectionId The collection ID.
     * @return The list of notes for the collection.
     */
    public Optional<List<Note>> getNotesByCollectionId(long collectionId)
    {
        if (!collections.existsById(collectionId))
        {
            return Optional.empty();
        }
        return Optional.of(notes.findAllByCollectionId(collectionId));
    }

    /**
     * Build and save a new note.
     *
     * @param collectionId The collection ID for the new note.
     * @return The newly created note.
     */
    public Note createNote(long collectionId)
    {
        if (collectionId < 0 || !collections.existsById(collectionId))
        {
            throw new IllegalArgumentException("Invalid collection ID");
        }

        Note newNote = buildNewNote(collectionId);
        return notes.save(newNote);
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
    public ResponseEntity<Note> moveNoteToCollection(Note note, String source, long collectionId)
    {
        // Check for bare validity
        if (note == null || note.getId() < 0 ||
                !collections.existsById(collectionId))
        {
            return ResponseEntity.badRequest().build();
        }

        // Check if title is taken in this collection
        // If it is no matter what we cannot save it
        if (notes.existsByTitleAndCollectionId(note.getTitle(), collectionId))
        {
            return ResponseEntity.status(ErrorCodes.INVALID_NOTE_TITLE.getCode()).build();
        }

        // If the source is local then the note already is on the server
        // we can simply save the note and return it
        if (source.equals("local"))
        {
            note.setCollectionId(collectionId);
            notes.save(note);
            return ResponseEntity.ok(note);
        }
        // If the source is foreign then the note is coming from another server
        // we need to check for ID overlap
        else if (source.equals("foreign"))
        {
            // Create new empty note and fill in the fields
            Note newNote = new Note(collectionId, note.getTitle(), note.getBody());
            notes.save(newNote);
            return ResponseEntity.ok(newNote);
        }

        return ResponseEntity.badRequest().build();
    }

    /**
     * Delete a note by its ID.
     *
     * @param id The ID of the note to delete.
     */
    @Transactional
    public void deleteNoteById(long id)
    {
        if(notes.existsById(id))
            notes.deleteById(id);
    }

    /**
     * Update the note saved in the Repository
     * @param note the updated note
     * @return ResponseEntity containing note if successful
     */
    public ResponseEntity<Note> updateNote(Note note)
    {

        if (note == null || note.getId() < 0 || !notes.existsById(note.getId()))
        {
            return ResponseEntity.badRequest().build();
        }

        if (note.getTitle() == null || note.getTitle().trim().isEmpty())
        {
            return ResponseEntity.status(ErrorCodes.EMPTY_NOTE_TITLE.getCode()).build();
        }

        if (notes.existsByTitleAndCollectionIdAndIdNot(note.getTitle(),
                note.getCollectionId(), note.getId()))
        {
            return ResponseEntity.status(ErrorCodes.INVALID_NOTE_TITLE.getCode()).build();
        }

        notes.save(note);
        return ResponseEntity.ok(note);
    }

    /**
     * The method should create a new note with a new title
     * @param collectionId The collection ID for the new note.
     * @return a new instance of a note
     */
    public Note buildNewNote(long collectionId)
    {
        List<Note> currentNotes = notes.findAllByCollectionId(collectionId);
        OptionalInt maxNr = currentNotes.stream()
                .filter(x -> x.getCollectionId() == collectionId)
                .map(Note::getTitle)
                .filter(x -> x.matches("My note .*"))
                .map(x -> x.split(" ")[x.split(" ").length - 1])
                .filter(x -> x.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max();

        if (maxNr.isEmpty())
        {
            return new Note(collectionId, "My note 1", "A new note :)");
        } else
        {
            return new Note(collectionId, "My note " + (maxNr.getAsInt() + 1), "A new note :)");
        }
    }
}
