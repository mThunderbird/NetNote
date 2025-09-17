package server.services;

import commons.Collection;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import server.database.CollectionRepository;
import server.database.NoteRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CollectionService
{

    private final CollectionRepository collectionRepository;
    private final NoteRepository noteRepository;

    /**
     * Constructor for dependency injection.
     *
     * @param collectionRepository The repository for managing collections.
     * @param noteRepository The repository for managing notes.
     */
    public CollectionService(CollectionRepository collectionRepository,
                             NoteRepository noteRepository)
    {
        this.collectionRepository = collectionRepository;
        this.noteRepository = noteRepository;
    }

    /**
     * Retrieve all collections from the database.
     *
     * @return A list of collections.
     */
    public List<Collection> getAllCollections()
    {
        return collectionRepository.findAll();
    }

    /**
     * Retrieve a collection by its ID.
     *
     * @param id The collection ID.
     * @return An optional containing the collection if found.
     */
    public Optional<Collection> getCollectionById(long id)
    {
        return collectionRepository.findById(id);
    }

    /**
     * Retrieve a collection by its title.
     *
     * @param title The collection title.
     * @return An optional containing the collection if found.
     */
    public Optional<Collection> getCollectionByTitle(String title)
    {
        return collectionRepository.findByTitle(title);
    }

    /**
     * Create a new collection with the given title.
     *
     * @param title The title of the new collection.
     * @return The newly created collection.
     */
    public Collection createCollection(String title)
    {
        Collection newCollection = new Collection();
        newCollection.setTitle(title);
        return collectionRepository.save(newCollection);
    }

    /**
     * Update an existing collection.
     *
     * @param collection The collection with updated data.
     * @return An optional containing the updated collection if the update was successful.
     */
    public Optional<Collection> updateCollection(Collection collection)
    {
        if(collection == null || !collectionRepository.existsById(collection.getId()))
        {
            return Optional.empty();
        }
        if (collectionRepository.existsByTitleAndIdNot(collection.getTitle(), collection.getId()))
        {
            return Optional.empty();
        }
        return Optional.of(collectionRepository.save(collection));
    }

    /**
     * Delete a collection and its notes by collection ID.
     *
     * @param id The ID of the collection to delete.
     */
    @Transactional
    public void deleteCollectionAndNotes(long id)
    {
        if (collectionRepository.existsById(id))
        {
            collectionRepository.deleteById(id);
            noteRepository.deleteNotesByCollectionId(id);
        }
    }
}
