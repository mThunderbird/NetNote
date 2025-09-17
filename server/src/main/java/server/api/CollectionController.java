package server.api;

import commons.Collection;
import commons.ErrorCodes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.services.CollectionService;

import java.util.List;

@RestController
@RequestMapping("/api/collections")
public class CollectionController
{

    private final CollectionService collectionService;

    /**
     * Constructor for dependency injection.
     *
     * @param collectionService The service used to manage collections.
     */
    public CollectionController(CollectionService collectionService)
    {
        this.collectionService = collectionService;
    }

    /**
     * Return all collections.
     *
     * @return the list of collections.
     */
    @GetMapping("/")
    public List<Collection> getAll()
    {
        return collectionService.getAllCollections();
    }

    /**
     * Return a collection by ID.
     *
     * @param id The ID to look for.
     * @return Either the collection with the corresponding ID or a bad request.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Collection> getById(@PathVariable("id") long id)
    {
        return collectionService.getCollectionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity
                        .status(ErrorCodes.COLLECTION_DOES_NOT_EXIST.getCode())
                        .build());
    }

    /**
     * Return a collection by Title.
     *
     * @param title The title to look for.
     * @return Either the collection with the corresponding title or a bad request.
     */
    @GetMapping("/title/{title}")
    public ResponseEntity<Collection> getByTitle(@PathVariable("title") String title)
    {
        return collectionService.getCollectionByTitle(title)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity
                        .status(ErrorCodes.COLLECTION_DOES_NOT_EXIST.getCode())
                        .build());
    }

    /**
     * Delete a collection by ID and its associated notes.
     *
     * @param id The collection ID.
     * @return The current list of collections.
     */
    @PostMapping("/delete")
    public List<Collection> delete(@RequestBody long id)
    {
        collectionService.deleteCollectionAndNotes(id);
        return collectionService.getAllCollections();
    }

    /**
     * Create a new collection with the given title.
     *
     * @param title The title of the new collection.
     * @return The newly created collection.
     */
    @PostMapping("/create")
    public ResponseEntity<Collection> create(@RequestBody String title)
    {
        if (title == null ||
                title.trim().isEmpty() ||
                collectionService.getCollectionByTitle(title).isPresent())
        {
            return ResponseEntity.badRequest().build();
        }
        Collection newCollection = collectionService.createCollection(title);
        return ResponseEntity.ok(newCollection);
    }

    /**
     * Update the given collection in the database if it exists.
     *
     * @param collection The already updated collection.
     * @return The collection if the update worked, otherwise a bad request.
     */
    @PostMapping("/update")
    public ResponseEntity<Collection> update(@RequestBody Collection collection)
    {
        return collectionService.updateCollection(collection)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }
}