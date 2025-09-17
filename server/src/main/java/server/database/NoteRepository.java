package server.database;

import org.springframework.data.jpa.repository.JpaRepository;

import commons.Note;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long>
{
    /**
     * Custom SQL query to retrieve all notes in a collection
     * @param collectionId the ID of collection to search for
     * @return list of all notes in requested collection
     */
    List<Note> findAllByCollectionId(long collectionId);

    /**
     * Delete all notes by given collection ID
     * Used when deleting a collection
     * @param collectionId the collection ID
     */
    void deleteNotesByCollectionId(long collectionId);

    /**
     * Checks if a note exists with given title and collection id, but not with given id
     * @param title the title of the note
     * @param collectionId the collection id of the note
     * @param id the id of the note
     * @return true if statement is satisfied, false otherwise
     */
    boolean existsByTitleAndCollectionIdAndIdNot(String title, long collectionId, long id);

    /**
     * Checks if a note exists with given title and collection id
     * @param title The title of the note
     * @param collectionId The collection id of the note
     * @return true if statement is satisfied, false otherwise
     */
    boolean existsByTitleAndCollectionId(String title, long collectionId);
}
