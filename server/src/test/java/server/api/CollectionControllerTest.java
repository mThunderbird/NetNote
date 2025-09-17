package server.api;

import commons.Collection;
import commons.ErrorCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import server.database.CollectionRepository;
import server.database.NoteRepository;
import server.services.CollectionService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CollectionControllerTest {

    @Mock
    private CollectionRepository collectionRepository;
    @Mock
    private NoteRepository noteRepository;
    private CollectionService collectionService;
    private CollectionController test;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        collectionService = new CollectionService(collectionRepository, noteRepository);

        test = new CollectionController(collectionService);
    }

    @Test
    void getAll() {
        when(collectionRepository.findAll()).thenReturn(List.of(new Collection("abc")));
        List<Collection> result = test.getAll();
        assertEquals(1, result.size());
        assertEquals("abc", result.getFirst().getTitle());
        verify(collectionRepository, times(1)).findAll();
    }

    @Test
    void getByIdEmpty() {
        when(collectionRepository.findById(11L)).thenReturn(Optional.empty());
        ResponseEntity<Collection> result = test.getById(11);
        assertEquals(ResponseEntity.status(ErrorCodes.COLLECTION_DOES_NOT_EXIST.getCode()).build(), result);
        verify(collectionRepository, times(1)).findById(11L);
    }

    @Test
    void getByIdSuccess()
    {
        Collection collection = new Collection("title");
        when(collectionRepository.findById(collection.getId())).thenReturn(Optional.of(collection));
        ResponseEntity<Collection> result = test.getById(collection.getId());
        assertEquals(ResponseEntity.ok(collection), result);
        assertEquals(collection.getId(), result.getBody().getId());
        verify(collectionRepository, times(1)).findById(collection.getId());
    }

    @Test
    void getByTitleFail()
    {
        when(collectionRepository.findByTitle("title")).thenReturn(Optional.empty());
        ResponseEntity<Collection> result = test.getByTitle("title");
        assertEquals(ResponseEntity.status(ErrorCodes.COLLECTION_DOES_NOT_EXIST.getCode()).build(), result);
        verify(collectionRepository, times(1)).findByTitle("title");
    }
    @Test
    void getByTitleSuccess()
    {
        Collection collection = new Collection("title");
        when(collectionRepository.findByTitle("title")).thenReturn(Optional.of(collection));
        ResponseEntity<Collection> result = test.getByTitle("title");
        assertEquals(ResponseEntity.ok(collection), result);
        assertEquals(collection.getTitle(), result.getBody().getTitle());
        verify(collectionRepository, times(1)).findByTitle("title");
    }
    @Test
    void getByTitleNull()
    {
        when(collectionRepository.findByTitle(null)).thenReturn(Optional.empty());
        ResponseEntity<Collection> result = test.getByTitle(null);
        assertEquals(ResponseEntity.status(ErrorCodes.COLLECTION_DOES_NOT_EXIST.getCode()).build(), result);
        verify(collectionRepository, times(1)).findByTitle(null);
    }

    @Test
    void create() {
        Collection collection = new Collection("title");
        when(collectionRepository.save(any(Collection.class))).thenReturn(collection);
        ResponseEntity<Collection> result = test.create("title");
        assertEquals(ResponseEntity.ok(collection), result);
        verify(collectionRepository, times(1)).save(any(Collection.class));
    }

    @Test
    void createTitleNull() {
        ResponseEntity<Collection> result = test.create(null);
        assertEquals(ResponseEntity.badRequest().build(), result);
        verify(collectionRepository, never()).save(any(Collection.class));
    }

    @Test
    void createCollectionInvalid()
    {
        Collection collection = new Collection("title");
        when(collectionRepository.findByTitle("title")).thenReturn(Optional.of(collection));
        ResponseEntity<Collection> result = test.create("title");
        assertEquals(ResponseEntity.badRequest().build(), result);
        verify(collectionRepository, times(1)).findByTitle("title");
        verify(collectionRepository, never()).save(any(Collection.class));
    }

    @Test
    void update() {
        Collection existing = new Collection("existing");
        when(collectionRepository.existsById(anyLong())).thenReturn(true);
        when(collectionRepository.save(any(Collection.class))).thenReturn(existing);
        ResponseEntity<Collection> result = test.update(existing);
        assertEquals(ResponseEntity.ok(existing), result);
        verify(collectionRepository, times(1)).existsById(anyLong());
        verify(collectionRepository, times(1)).save(any(Collection.class));
    }

    @Test
    void updateFailBecauseOtherCollectionHasTheSameTitle()
    {
        Collection existing = new Collection("existing");
        when(collectionRepository.existsById(anyLong())).thenReturn(true);
        when(collectionRepository.existsByTitleAndIdNot(anyString(), anyLong())).thenReturn(true);
        ResponseEntity<Collection> result = test.update(existing);
        assertEquals(ResponseEntity.badRequest().build(), result);
        verify(collectionRepository, times(1)).existsByTitleAndIdNot(anyString(), anyLong());
        verify(collectionRepository, never()).save(any(Collection.class));
    }

    @Test
    void updateNull() {
        ResponseEntity<Collection> result = test.update(null);
        assertEquals(ResponseEntity.badRequest().build(), result);
        verify(collectionRepository, never()).save(any(Collection.class));
    }

    @Test
    void delete()
    {
        when(collectionRepository.existsById(1L)).thenReturn(true);
        when(collectionRepository.findAll()).thenReturn(List.of());
        List<Collection> result = test.delete(1);
        assertEquals(0, result.size());
        verify(collectionRepository, times(1)).existsById(1L);
        verify(collectionRepository, times(1)).deleteById(1L);
        verify(noteRepository, times(1)).deleteNotesByCollectionId(1L);
    }
    @Test
    void deleteNotFound() {
        when(collectionRepository.existsById(1L)).thenReturn(false);
        when(collectionRepository.findAll()).thenReturn(List.of());
        List<Collection> result = test.delete(1);
        assertEquals(0, result.size());
        verify(collectionRepository, times(1)).existsById(1L);
        verify(collectionRepository, times(1)).findAll();
        verify(noteRepository, never()).deleteNotesByCollectionId(anyLong());
    }
}
