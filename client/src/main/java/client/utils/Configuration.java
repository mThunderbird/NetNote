package client.utils;

import client.src.ClientCollection;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Configuration
{
    /**
     * The collections we have subscribed to
     */
    private List<ClientCollection> collections;

    /**
     * The default collection we have selected - saved by id + serverURL;
     */
    @JsonProperty("defaultId")
    private Long defaultId;
    @JsonProperty("defaultServerURL")
    private String defaultServerURL;

    /**
     * The currently selected languageCode
     */
    @JsonProperty("languageCode")
    private String languageCode;

    /**
     * Constructor for Configuration
     */
    public Configuration() {}

    /**
     * Read configuration from file and initialize the object
     */
    public void initialize()
    {
        Configuration config = readConfiguration();
        this.collections = config.getCollections();
        this.defaultId = config.defaultId;
        this.defaultServerURL = config.defaultServerURL;
        this.languageCode = config.languageCode;
    }

    /**
     * Save the configuration to a file
     */
    public void saveConfiguration()
    {
        try
        {
            ObjectMapper om = new ObjectMapper();
            String json = om.writerWithDefaultPrettyPrinter().writeValueAsString(this);

            File file = new File("client/src/main/resources/config.json");
            FileUtils.writeStringToFile(file, json, "UTF-8");
        }
        catch (IOException e)
        {
            System.err.println("Configuration saving failed!");
            throw new RuntimeException(e);
        }
    }

    /**
     * Read the configuration from a file
     * @return The configuration object
     */
    public static Configuration readConfiguration()
    {
        try
        {
            File file = new File("client/src/main/resources/config.json");

            if (!file.exists())
            {
                Configuration newConfig = new Configuration();
                newConfig.defaultId = -1L;
                newConfig.defaultServerURL = "";
                newConfig.languageCode = "en";
                newConfig.collections = new ArrayList<>();
                return newConfig;
            }

            String json = FileUtils.readFileToString(file,"UTF-8");

            ObjectMapper om = new ObjectMapper();
            return om.readValue(json, Configuration.class);
        }
        catch (IOException e)
        {
            System.err.println("Configuration saving failed!");
            throw new RuntimeException(e);
        }
    }

    public List<ClientCollection> getCollections()
    {
        return collections;
    }

    @JsonIgnore
    public ClientCollection getDefaultCollection()
    {
        return collections.stream().filter(c -> c.getId() == defaultId
                && c.getServerURL().equals(defaultServerURL)).findFirst().orElse(null);
    }

    public String getLanguageCode()
    {
        return languageCode;
    }

    public void setCollections(List<ClientCollection> collections)
    {
        this.collections = collections;
    }

    @JsonIgnore
    public void setDefaultCollection(ClientCollection defaultCollection)
    {
        if (defaultCollection == null)
        {
            this.defaultId = -1L;
            this.defaultServerURL = "";
        }
        else if (!collections.contains(defaultCollection))
        {
            throw new IllegalArgumentException("Default collection " +
                    "must be in the collections list");
        }
        else
        {
            this.defaultId = defaultCollection.getId();
            this.defaultServerURL = defaultCollection.getServerURL();
        }
    }
    public void setLanguageCode(String languageCode)
    {
        this.languageCode = languageCode;
    }

    /**
     * Sets and saves config file for updates to collections
     * @param collections the list of all collections
     * @param defaultCollection the default collection
     */
    public void setAndSave(List<ClientCollection> collections, ClientCollection defaultCollection)
    {
        setCollections(collections);
        setDefaultCollection(defaultCollection);
        saveConfiguration();
    }
}
