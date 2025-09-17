package client.src;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import commons.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CollectionManagerTest {
    private CollectionManager collectionManager;
    @BeforeEach
    void setUp()
    {
        collectionManager = new CollectionManager();
    }
    @Test
    void testInitializeFromJson_SuccessfulLoad()
    {
        String mockJson = """
                [
                    {
                        "collectionData": {
                            "id": 1,
                            "title": "Collection 1"
                        },
                        "nickname": "Nick 1",
                        "serverURL": "http://server1.com/"
                    },
                    {
                        "collectionData": {
                            "id": 2,
                            "title": "Collection 2"
                        },
                        "nickname": "Nick 2",
                        "serverURL": "http://server2.com/"
                    }
                ]
                """;
        collectionManager.initializeFromJson(mockJson);
        List<ClientCollection> collections = collectionManager.getCollections();
        assertEquals(2, collections.size());
        assertEquals("http://server1.com/", collections.get(0).getServerURL());
        assertEquals("Nick 1", collections.get(0).getNickname());
    }
    @Test
    void testGetDistinctServerURLs1()
    {
        ClientCollection collection1 = new ClientCollection(
                new Collection("Collection 1"), "Nick 1", "http://server1.com");
        ClientCollection collection2 = new ClientCollection(
                new Collection("Collection 2"), "Nick 2", "http://server2.com");
        ClientCollection collection3 = new ClientCollection(
                new Collection("Collection 3"), "Nick 3", "http://server1.com");
        collectionManager.getCollections().addAll(List.of(collection1, collection2, collection3));
        List<String> distinctUrls = collectionManager.getDistinctServerURLs();
        assertEquals(2, distinctUrls.size());
        assertTrue(distinctUrls.contains("http://server1.com"));
        assertTrue(distinctUrls.contains("http://server2.com"));
    }
    @Test
    void testGetCollections_EmptyInitially()
    {
        assertTrue(collectionManager.getCollections().isEmpty());
    }
}
