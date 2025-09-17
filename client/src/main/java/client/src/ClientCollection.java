package client.src;

import com.fasterxml.jackson.annotation.JsonIgnore;
import commons.Collection;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

public class ClientCollection
{
    private Collection collectionData;
    private String nickname;
    private String serverURL;
    private boolean isOnline;

    /**
     * Default constructor needed for
     * the ObjectMapper
     */
    public ClientCollection() {}

    /**
     * Constructor for ClientCollection
     * @param collectionData The collection from the server
     * @param nickname The local nickname
     * @param serverURL The server that hosts this collection
     */
    public ClientCollection(Collection collectionData, String nickname, String serverURL)
    {
        this.collectionData = collectionData;
        this.nickname = nickname;
        this.serverURL = serverURL;
    }

    public String getServerURL()
    {
        return serverURL;
    }

    @JsonIgnore
    public long getId()
    {
        return collectionData.getId();
    }

    public Collection getCollectionData()
    {
        return collectionData;
    }

    @JsonIgnore
    public String getTitle()
    {
        return collectionData.getTitle();
    }

    public String getNickname()
    {
        return nickname;
    }

    /**
     * Check if the collection is online
     * @return true if online
     */
    @JsonIgnore
    public boolean isOnline()
    {
        return isOnline;
    }

    public void setNickname(String nickname)
    {
        this.nickname = nickname;
    }

    public void setCollectionData(Collection collection)
    {
        this.collectionData = collection;
    }

    @JsonIgnore
    public void setOnline(boolean online)
    {
        isOnline = online;
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
