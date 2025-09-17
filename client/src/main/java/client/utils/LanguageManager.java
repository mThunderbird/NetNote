package client.utils;

import jakarta.inject.Inject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class LanguageManager
{
    private Locale currentLocale;

    private ResourceBundle bundle;

    private ComboBox<String> languageSelector;

    private final Map<String, StringProperty> propertyMap = new HashMap<>();

    @Inject
    private Configuration configuration;

    /**
     * Constructor for LanguageManager
     */
    public LanguageManager() {}

    /**
     * Set the choice box, pass ref to main controller
     * @param languageSelector The language selector
     */
    public void initialize(ComboBox<String> languageSelector)
    {
        this.languageSelector = languageSelector;
        makePropertyMap();

        languageSelector.setOnKeyPressed(event ->
        {
            if (event.getCode() == KeyCode.SPACE)
                languageSelector.show();
        });
        this.currentLocale = new Locale(configuration.getLanguageCode());

        switchLanguage(configuration.getLanguageCode());

        // Map for language codes and their display names
        Map<String, String> languages = Map.of(
                "en", "English",
                "nl", "Nederland",
                "de", "Deutch",
                "ro", "Romana"
        );

        // Add language codes to the ComboBox items
        ObservableList<String> languageOptions = FXCollections.
                observableArrayList("en", "nl", "de", "ro");
        languageSelector.setItems(languageOptions);

        // Set the initial selected language
        String initialLanguageCode = configuration.getLanguageCode(); // Default to English
        languageSelector.setValue(initialLanguageCode);

        // Customize the ComboBox's button and drop-down appearance
        languageSelector.setCellFactory(new FlagCellFactory(languages));
        languageSelector.setButtonCell(new FlagCellFactory(languages).call(null));

        // Add the setOnAction logic
        languageSelector.setOnAction(event ->
        {
            String selectedLanguage = languageSelector.getValue();

            switchLanguage(selectedLanguage);
        });
    }

    private void makePropertyMap()
    {
        propertyMap.put("add_note", new SimpleStringProperty());
        propertyMap.put("refresh", new SimpleStringProperty());
        propertyMap.put("manage_collections", new SimpleStringProperty());
        propertyMap.put("search_by_title", new SimpleStringProperty());
        propertyMap.put("enter_notes", new SimpleStringProperty());
        propertyMap.put("enter_notes_title", new SimpleStringProperty());
        propertyMap.put("start_add_collection", new SimpleStringProperty());
        propertyMap.put("create_collection", new SimpleStringProperty());
        propertyMap.put("subscribe_collection", new SimpleStringProperty());
        propertyMap.put("remove_collection", new SimpleStringProperty());
        propertyMap.put("delete_collection", new SimpleStringProperty());
        propertyMap.put("server_address", new SimpleStringProperty());
        propertyMap.put("collection_title", new SimpleStringProperty());
        propertyMap.put("local_nickname", new SimpleStringProperty());
        propertyMap.put("no_collection_selected", new SimpleStringProperty());
        propertyMap.put("all_collections", new SimpleStringProperty());
        propertyMap.put("invalid_title", new SimpleStringProperty());
        propertyMap.put("empty_title", new SimpleStringProperty());
        propertyMap.put("collection_search", new SimpleStringProperty());
        propertyMap.put("server_cant_be_reached", new SimpleStringProperty());
        propertyMap.put("collection_title_already_used", new SimpleStringProperty());
        propertyMap.put("empty_field", new SimpleStringProperty());
        propertyMap.put("duplicate_nickname", new SimpleStringProperty());
        propertyMap.put("collection_found", new SimpleStringProperty());
        propertyMap.put("collection_can_be_added", new SimpleStringProperty());
        propertyMap.put("collection_saved", new SimpleStringProperty());
        propertyMap.put("label_move_collection", new SimpleStringProperty());
        propertyMap.put("notif_server_offline", new SimpleStringProperty());
        propertyMap.put("notif_server_online", new SimpleStringProperty());
        propertyMap.put("notif_cannot_move_note", new SimpleStringProperty());
        propertyMap.put("notif_collection_unavailable", new SimpleStringProperty());
        propertyMap.put("delete_action", new SimpleStringProperty());
        propertyMap.put("delete_note_confirm", new SimpleStringProperty());
        propertyMap.put("delete_collection_confirm", new SimpleStringProperty());
        propertyMap.put("undoable_action", new SimpleStringProperty());
        propertyMap.put("server_online", new SimpleStringProperty());
        propertyMap.put("title_saved", new SimpleStringProperty());
        propertyMap.put("nickname_saved", new SimpleStringProperty());
        propertyMap.put("ok", new SimpleStringProperty());
        propertyMap.put("cancel", new SimpleStringProperty());
        propertyMap.put("default_collection", new SimpleStringProperty());
        propertyMap.put("notif_refresh", new SimpleStringProperty());
    }

    /**
     * Method for changing the current selected language
     * @param languageCode A string that stores the value of the current language
     */
    public void switchLanguage(String languageCode)
    {
        currentLocale = new Locale(languageCode);
        configuration.setLanguageCode(languageCode);
        configuration.saveConfiguration();
        bundle = ResourceBundle.getBundle("lang", currentLocale);

        propertyMap.forEach((key, value) -> value.set(bundle.getString(key)));
    }

    public StringProperty getProperty(String key)
    {
        return propertyMap.get(key);
    }
}