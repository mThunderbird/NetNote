package client.scenes;

import client.utils.Configuration;
import client.utils.LanguageManager;
import client.src.*;
import client.utils.MyWebSocketClient;
import client.utils.ServerUtils;
import commons.Note;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.WindowEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class BaseCtrl implements Initializable
{
    /**
     * Specific menu containers
     */
    @FXML
    private VBox root;
    @FXML
    private VBox hbErrorMsgPane;
    @FXML
    private VBox vbNoteList;
    @FXML
    private TextField tfCurrentNoteTitle;
    @FXML
    private TextArea taCurrentNote;
    @FXML
    private Button addNote;
    @FXML
    private Button refreshButton;
    @FXML
    private Button collectManage;
    @FXML
    private ComboBox<String> languageSelector;
    @FXML
    private WebView wvNoteRender;
    @FXML
    private TextField tfSearchBar;
    @FXML
    private ChoiceBox<String> cbCollectionFilter;
    @FXML
    private ChoiceBox<String> cbNoteMoveCollection;
    @FXML
    private Label lMoveCollection;

    private ObservableList<String> collectionList;

    @Inject
    private NoteManager noteManager;

    @Inject
    private CurrentNoteManager currentNoteManager;

    @Inject
    private ServerUtils serverUtils;

    @Inject
    private MyWebSocketClient wsClient;

    @Inject
    private CollectionManager collectionManager;

    @Inject
    private Configuration configuration;

    @Inject
    private LanguageManager languageManager;

    @Inject
    private MainCtrl mainCtrl;

    @Inject
    private CollectionCtrl collectionCtrl;

    @Inject
    private PopUpManager popUpManager;

    /**
     * Default Inject constructor for BaseCtrl
     */
    public BaseCtrl()
    {}

    /**
     * Request a new note from the server.
     * Call the add note method in the note manager
     * with the received note
     */
    public void addNoteBtnClick()
    {
        tfSearchBar.clear();

        ClientCollection toAddInto;

        if(cbCollectionFilter.getValue().equals(languageManager.
                getProperty("all_collections").getValue())
                || cbCollectionFilter.getValue() == null)
        {
            toAddInto = collectionManager.getCurrentDefault();
        }
        else
        {
            toAddInto = collectionManager.getCollectionByNickName(cbCollectionFilter.getValue());
        }

        if (toAddInto == null || !toAddInto.isOnline())
        {
            popUpManager.noCollectionAvailableNotification();
            return;
        }

        try
        {
            Note newNote = serverUtils.createNote(toAddInto.getServerURL(), toAddInto.getId());

            ClientNote newClientNote = noteManager.addNote(newNote, toAddInto.getServerURL());
            noteManager.setCurrentNote(newClientNote);
            noteManager.updateVisibilityAll();
        }
        catch (Exception _){}
    }

    /**
     * Refresh the note list by requesting the notes from the server
     * and updating them in the note manager
     */
    public void refreshBtnClick()
    {
        mainCtrl.closeCollections();
        collectionManager.refreshCollections();
        noteManager.refreshNotes();
        noteManager.updateVisibilityAll();
        popUpManager.refreshNotification();

    }

    /**
     * Initialize the note manager and give it a reference to the
     * note list VBox container
     * Also adds an event listener to the search bar
     *
     * @param url The URL
     * @param resourceBundle The resource bundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        configuration.initialize();
        languageManager.initialize(languageSelector);

        currentNoteManager.initialize(tfCurrentNoteTitle, taCurrentNote,
                cbNoteMoveCollection, lMoveCollection, wvNoteRender);
        noteManager.initialize(vbNoteList, tfSearchBar, cbCollectionFilter);


        popUpManager.setErrorPane(hbErrorMsgPane);

        // Initialize the collection manager with all collections from the
        // configuration and make them offline
        collectionManager.initialize();
        // Initialize the ws client to attempt connecting to servers that
        // we have subscribed to
        // On connection established, the wsClient will notify:
        // the collection manager via CollectionManager.serverOnline(String serverURL)
        // next, the collection manager will propagate this to the note manager
        // via NoteManager.addCollection(ClientCollection collection)
        // for each collection that came online
        //
        // On connection lost, the wsClient will notify:
        // the collection manager via CollectionManager.serverOffline(String serverURL)
        // next, the collection manager will propagate this to the note manager
        // via NoteManager.serverOffline(String serverURL)
        wsClient.initialize();

        tfSearchBar.textProperty().addListener( e -> noteManager.updateVisibilityAll());

        refreshFilter();
        languageManager.getProperty("all_collections").addListener((ob, oldValue, newValue) ->
        {
            collectionList.set(0, newValue);
            if(cbCollectionFilter.getValue() != null &&
                    cbCollectionFilter.getValue().equals(oldValue))
            {
                cbCollectionFilter.setValue(collectionList.getFirst());
            }
        });

        cbCollectionFilter.setOnAction(event -> noteManager.updateVisibilityAll());

        noteManager.updateVisibilityAll();

        addNote.textProperty().bind(languageManager.getProperty("add_note"));
        refreshButton.textProperty().bind(languageManager.getProperty("refresh"));
        collectManage.textProperty().bind(languageManager.getProperty("manage_collections"));
        tfSearchBar.promptTextProperty().bind(languageManager.getProperty("search_by_title"));
        taCurrentNote.promptTextProperty().bind(languageManager.getProperty("enter_notes"));
        tfCurrentNoteTitle.promptTextProperty()
                .bind(languageManager.getProperty("enter_notes_title"));
        lMoveCollection.textProperty().bind(languageManager.getProperty("label_move_collection"));

        root.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
    }

    private void handleKeyPress(KeyEvent e)
    {
        if (e.getCode() == KeyCode.ESCAPE)
        {
            tfSearchBar.requestFocus();
            return;
        }

        if (!e.isControlDown())
            return;

        switch (e.getCode())
        {
            case N:
                addNoteBtnClick();
                break;
            case Q:
                root.fireEvent(new WindowEvent(
                        root.getScene().getWindow(),
                        WindowEvent.WINDOW_CLOSE_REQUEST));
                break;
            case UP:
                noteManager.moveToPreviousNote();
                break;
            case DOWN:
                noteManager.moveToNextNote();
                break;
            case RIGHT:
                cbCollectionFilter.getSelectionModel().selectNext();
                break;
            case LEFT:
                cbCollectionFilter.getSelectionModel().selectPrevious();
                break;
            case M:
                openCollectionManagement();
                break;
        }
    }

    /**
     * Calls method to open collections scene
     */
    public void openCollectionManagement()
    {
        mainCtrl.showCollections();
    }

    /**
     * Refresh the list of collection for filtering
     */
    public void refreshFilter()
    {
        collectionList = FXCollections.observableArrayList();
        collectionList.add(languageManager.getProperty("all_collections").getValue());
        for (ClientCollection collection : collectionManager.getCollections())
        {
            collectionList.add(collection.getNickname());
        }
        cbCollectionFilter.setItems(collectionList);
        cbCollectionFilter.setValue(collectionList.getFirst());
    }

    /**
     * On closing the base scene
     * and save the configuration
     */
    public void close()
    {
        configuration.setCollections(collectionManager.getCollections());
        configuration.setDefaultCollection(collectionManager.getCurrentDefault());
        configuration.saveConfiguration();
    }
}
