package commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NoteTest {
    @Test
    public void checkConstructor()
    {
        var q = new Note(1, "NewCollection", "Hello");
        assertNotNull(q);
    }
    @Test
    public void getCollectionIDTest()
    {
        var q = new Note(1, "NewCollection", "Hi there");
        assertEquals(q.getCollectionId(), 1);
    }
    @Test
    public void getTitleTest()
    {
        var q = new Note(1, "NewCollection", "Hello world");
        assertEquals(q.getTitle(), "NewCollection");
    }
    @Test
    public void getBodyTest()
    {
        var q = new Note(1, "NewCollection", "My name is haha");
        assertEquals(q.getBody(), "My name is haha");
    }
    @Test
    public void setCollectionIDTest()
    {
        var q = new Note(1, "NewCollection", "My name is haha");
        q.setCollectionId(2);
        assertEquals(q.getCollectionId(), 2);
    }
    @Test
    public void setTitleTest()
    {
        var q = new Note(1, "NewCollection", "My name is haha");
        q.setTitle("My Collection");
        assertEquals(q.getTitle(), "My Collection");
    }
    @Test
    public void setBodyTest()
    {
        var q = new Note(1, "NewCollection", "My name is haha");
        q.setBody("What are you doing");
        assertEquals(q.getBody(), "What are you doing");
    }

    @Test
    public void notEqualsAllHashCode()
    {
        var q = new Note(1, "NewCollection", "My name is haha");
        var m = new Note(3, "My Collection", "My name is hahahaha");
        assertNotEquals(q, m);
        assertNotEquals(q.hashCode(), m.hashCode());
    }
    @Test
    public void notEqualsTitleHashCode()
    {
        var q = new Note(1, "NewCollection", "My name is haha");
        var m = new Note(1, "My Collection", "My name is haha");
        assertNotEquals(q, m);
        assertNotEquals(q.hashCode(), m.hashCode());
    }
    @Test
    public void notEqualsCollectionIDHashCode()
    {
        var q = new Note(1, "NewCollection", "My name is haha");
        var m = new Note(2, "NewCollection", "My name is haha");
        assertNotEquals(q, m);
        assertNotEquals(q.hashCode(), m.hashCode());
    }
    @Test
    public void notEqualsBodyHashCode()
    {
        var q = new Note(1, "NewCollection", "My name is haha");
        var m = new Note(1, "NewCollection", "My name is lala");
        assertNotEquals(q, m);
        assertNotEquals(q.hashCode(), m.hashCode());
    }
    @Test
    public void notEqualsBodyTest()
    {
        var q = new Note(1, "NewCollection", "My name is haha");
        var m = new Note(1, "NewCollection", "My name is lala");
        assertFalse(q.equals(m));
    }
    @Test
    public void notEqualsTitleTest()
    {
        var q = new Note(1, "NewCollection", "My name is haha");
        var m = new Note(1, "Super Collection", "My name is haha");
        assertFalse(q.equals(m));
    }
    @Test
    public void hasToString()
    {
        var actual = new Note(1, "NewCollection", "My name is haha").toString();
        assertTrue(actual.contains("\n"));
        assertTrue(actual.contains("Collection"));
    }
}
