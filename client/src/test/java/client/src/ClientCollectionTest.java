package client.src;

import commons.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClientCollectionTest
{
    private ClientCollection clientCollection;
    private Collection collection;

    @BeforeEach
    void setUp(){
        collection = new Collection("Test Collection");
        clientCollection = new ClientCollection(collection, "TestNickname","server1");
    }
    @Test
    void getServerURL()
    {
        String serverURL = clientCollection.getServerURL();
        assertEquals(serverURL, "server1");
    }
    @Test
    void getId()
    {
        Long id = clientCollection.getId();
        assertEquals(id, collection.getId());
    }
    @Test
    void getCollectionData()
    {
        Collection collection1 = clientCollection.getCollectionData();
        assertEquals(collection, collection1);
    }
    @Test
    void getTitle()
    {
        String title = clientCollection.getTitle();
        assertEquals("Test Collection", title);
    }
    @Test
    void getNickname()
    {
        String nickname = clientCollection.getNickname();
        assertEquals("TestNickname", nickname);
    }
    @Test
    void setNickname()
    {
        clientCollection.setNickname("TestNewNickname");
        assertEquals("TestNewNickname", clientCollection.getNickname());
    }
    @Test
    void setCollectionData()
    {
        Collection collection1 = new Collection("Test Collection1");
        clientCollection.setCollectionData(collection1);
        assertEquals(collection1, clientCollection.getCollectionData());
    }
    @Test
    void testNotEqualsNickname()
    {
        ClientCollection clientCollection2 = new ClientCollection(collection, "OtherNickname","server1");
        assertNotEquals(clientCollection, clientCollection2);
    }
    @Test
    void testDifferentHashCode()
    {
        ClientCollection clientCollection2 = new ClientCollection(collection, "OtherNickname","server1");
        assertNotEquals(clientCollection.hashCode(), clientCollection2.hashCode());
    }
}