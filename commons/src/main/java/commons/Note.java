package commons;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

@Entity
public class Note
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    /*
    @ManyToOne
    private Collection collection;

    This is not used as all notes would need to be updated by us every time collection changed title
     */

    @Column(nullable = false)
    private long collectionId;

    @Column(nullable = false)
    private String title;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    /**
     * Constructor for Note
     * id is generated when saved to DB
     * @param collectionId the ID of the collection the note belongs to
     * @param title the title of the note
     * @param body the markdown body of the note
     */
    public Note(long collectionId, String title, String body)
    {
        this.collectionId = collectionId;
        this.title = title;
        this.body = body;
    }

    /**
     * Empty constructor for JPA
     */
    public Note()
    {

    }

    public long getId()
    {
        return id;
    }

    public long getCollectionId()
    {
        return collectionId;
    }

    public String getTitle()
    {
        return title;
    }

    public String getBody()
    {
        return body;
    }

    public void setCollectionId(long collectionId)
    {
        this.collectionId = collectionId;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setBody(String body)
    {
        this.body = body;
    }

    @Override
    public boolean equals(Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, MULTI_LINE_STYLE);
    }
}
