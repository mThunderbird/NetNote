package commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CollectionTest {
    @Test
    public void checkConstructor()
    {
        var q = new Collection("My Collection");
        assertNotNull(q);
    }
    @Test
    public void getTitleTest()
    {
        var q = new Collection("My Collection");
        assertEquals(q.getTitle(), "My Collection");
    }
    @Test
    public void setTitleTest()
    {
        var q = new Collection("My Collection");
        q.setTitle("NewCollection");
        assertEquals(q.getTitle(), "NewCollection");
    }
    @Test
    public void notEqualsHashCode()
    {
        var q = new Collection("My Collection");
        var m = new Collection("This Collection");
        assertNotEquals(q, m);
        assertNotEquals(q.hashCode(), m.hashCode());
    }
    @Test
    public void notEqualsTitleTest()
    {
        var q = new Collection("My Collection");
        var m = new Collection("This Collection");
        assertFalse(q.equals(m));
    }
    @Test
    public void hasToString()
    {
        var actual = new Collection("My Collection").toString();
        assertTrue(actual.contains("\n"));
        assertTrue(actual.contains("Collection"));
    }
}