package client.scenes;

import client.src.ClientCollection;
import client.src.CollectionManager;
import client.src.CurrentNoteManager;
import client.src.PopUpManager;
import client.utils.LanguageManager;
import client.utils.ServerUtils;
import commons.Collection;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.WindowEvent;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class CollectionCtrl implements Initializable
{

    @FXML
    private SplitPane root;
    @FXML
    private TextField collectionSearchTextField;
    @FXML
    private ListView<String> collectionList;

    @FXML
    private Button startAdditionButton;

    @FXML
    private Text defaultCollectionText;
    @FXML
    private ChoiceBox<String> defaultCollectionChoiceBox;

    @FXML
    private Text serverAddressText;
    @FXML
    private ComboBox<String> serverAddressComboBox;

    @FXML
    private Text collectionTitleText;
    @FXML
    private TextField collectionTitleTextField;

    @FXML
    private Text localNicknameText;
    @FXML
    private TextField localNicknameTextField;

    @FXML
    private Circle statusCircle;
    @FXML
    private Text serverStatusText;

    @FXML
    private Button addButton;
    @FXML
    private Button removeButton;
    @FXML
    private Button deleteButton;

    @Inject
    private CollectionManager collectionManager;

    @Inject
    private LanguageManager languageManager;

    @Inject
    private ServerUtils serverUtils;

    @Inject
    private BaseCtrl baseCtrl;

    @Inject
    private CurrentNoteManager currentNoteManager;

    @Inject
    private PopUpManager popUpManager;

    @Inject
    private MainCtrl mainCtrl;

    private static final int SAVING_DELAY = 600;
    private Timer changeTimer;

    private ClientCollection collection;

    /**
     * Initialize method for CollectionCtrl
     * By default disables all user input on right side of collections scene
     *
     * @param url the url
     * @param resourceBundle the resource bundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        collectionSearchTextField.textProperty().addListener(ob -> updateList());
        startAdditionButton.textProperty()
                .bind(languageManager.getProperty("start_add_collection"));
        defaultCollectionText.textProperty()
                .bind(languageManager.getProperty("default_collection"));
        addButton.textProperty().bind(languageManager.getProperty("create_collection"));
        removeButton.textProperty().bind(languageManager.getProperty("remove_collection"));
        deleteButton.textProperty().bind(languageManager.getProperty("delete_collection"));
        serverAddressText.textProperty().bind(languageManager.getProperty("server_address"));
        collectionTitleText.textProperty().bind(languageManager.getProperty("collection_title"));
        localNicknameText.textProperty().bind(languageManager.getProperty("local_nickname"));
        collectionSearchTextField.promptTextProperty()
                .bind(languageManager.getProperty("collection_search"));

        serverAddressComboBox.getEditor()
                .textProperty()
                .addListener((ob, ol, newValue) ->
                        serverAddressComboBox.setValue(newValue));
        serverAddressComboBox.valueProperty().addListener((ob, ol, ne) ->
                changeDelay());
        serverAddressComboBox.getEditor().setOnKeyPressed(e ->
        {
            if (e.getCode() == KeyCode.SPACE)
                serverAddressComboBox.show();
        });

        collectionTitleTextField.textProperty().addListener((ob, ol, ne) ->
                changeDelay());
        localNicknameTextField.textProperty().addListener((ob, ol, ne) ->
                changeDelay());

        collectionList.getSelectionModel().selectedItemProperty().addListener((ob, ol, ne) ->
                editSetup());

        defaultCollectionChoiceBox.setOnAction(e -> handleDefault());

        collectionList.addEventFilter(KeyEvent.KEY_PRESSED, e ->
        {
            if (e.isControlDown() && e.getCode() == KeyCode.UP)
            {
                e.consume();
            }
            else if (e.isControlDown() && e.getCode() == KeyCode.DOWN)
            {
                e.consume();
            }
        });

        updateList();
        clearSetup();

        root.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
    }

    private void handleKeyPress(KeyEvent e)
    {
        if (e.getCode() == KeyCode.ESCAPE)
        {
            collectionSearchTextField.requestFocus();
            return;
        }

        if (!e.isControlDown())
            return;

        switch (e.getCode())
        {
            case N:
                additionSetup();
                break;
            case M:
            case Q:
                closeWindow();
                break;
            case UP:
                handleKeyUp();
                break;
            case DOWN:
                handleKeyDown();
                break;

        }
    }

    private void handleKeyUp()
    {
        if(collectionList.getSelectionModel().getSelectedItem()==null)
        {
            collectionList.getSelectionModel().selectLast();
        }
        else if(collectionList.getSelectionModel().getSelectedIndex() != 0)
        {
            collectionList.getSelectionModel().selectPrevious();
        }
    }

    private void handleKeyDown()
    {
        if(collectionList.getSelectionModel().getSelectedItem()==null)
        {
            collectionList.getSelectionModel().selectFirst();
        }
        else if(collectionList.getSelectionModel().getSelectedIndex() !=
                collectionList.getItems().size() - 1)
        {
            collectionList.getSelectionModel().selectNext();
        }
    }

    /**
     * Closes the window
     */
    public void closeWindow()
    {
        root.fireEvent(new WindowEvent(
                root.getScene().getWindow(),
                WindowEvent.WINDOW_CLOSE_REQUEST));
        mainCtrl.showBase();
    }

    /**
     * Updates the list of collections
     */
    public void updateList()
    {
        String search = collectionSearchTextField.getText();
        List<String> nicknames = collectionManager.getCollections()
                .stream()
                .map(ClientCollection::getNickname)
                .filter(x -> x.contains(search))
                .toList();
        collectionList.setItems(FXCollections.observableArrayList(nicknames));
        defaultCollectionChoiceBox.setItems(FXCollections.observableArrayList(nicknames));
        if(collection != null)
        {
            collectionList.getSelectionModel().select(collection.getNickname());
        }
        if(collectionManager.getCurrentDefault() != null)
        {
            defaultCollectionChoiceBox.setValue(
                    collectionManager.getCurrentDefault().getNickname()
            );
        }
    }

    /**
     * Does setup for cleared collection scene
     */
    public void clearSetup()
    {
        collection=null;
        collectionList.getSelectionModel().clearSelection();

        if(changeTimer != null)
        {
            changeTimer.cancel();
        }

        collectionSearchTextField.setText("");

        startAdditionButton.setDisable(false);

        serverAddressComboBox.setDisable(true);
        collectionTitleTextField.setDisable(true);
        localNicknameTextField.setDisable(true);
        serverAddressComboBox.setValue("");
        collectionTitleTextField.setText("");
        localNicknameTextField.setText("");

        circleEmpty();
        serverStatusText.textProperty().bind(languageManager.getProperty("no_collection_selected"));

        removeButton.setVisible(false);
        removeButton.setManaged(false);

        deleteButton.setVisible(false);
        deleteButton.setManaged(false);

        addButton.setVisible(false);
        addButton.setManaged(false);
    }

    /**
     * Updates scene to allow for collection creation
     */
    public void additionSetup()
    {
        startAdditionButton.setDisable(true);

        collectionList.getSelectionModel().clearSelection();

        serverAddressComboBox.setDisable(false);
        serverAddressComboBox.setValue("");
        collectionTitleTextField.setDisable(false);
        collectionTitleTextField.setText("");
        localNicknameTextField.setDisable(false);
        localNicknameTextField.setText("");

        removeButton.setVisible(false);
        removeButton.setManaged(false);

        deleteButton.setVisible(false);
        deleteButton.setManaged(false);

        addButton.setVisible(true);
        addButton.setManaged(true);

        updateServerList();
        updateAddition();
    }

    /**
     * Updates scene to allow for collection editing
     */
    public void editSetup()
    {
        String nickname = collectionList.getSelectionModel().getSelectedItem();
        if(nickname==null || (collection!=null && nickname.equals(collection.getNickname())))
        {
            return;
        }
        collection = collectionManager.getCollectionByNickName(nickname);

        startAdditionButton.setDisable(false);

        serverAddressComboBox.setDisable(true);
        serverAddressComboBox.setValue(collection.getServerURL());

        collectionTitleTextField.setText(collection.getTitle());

        localNicknameTextField.setDisable(false);
        localNicknameTextField.setText(collection.getNickname());

        removeButton.setVisible(true);
        removeButton.setManaged(true);

        deleteButton.setVisible(true);
        deleteButton.setManaged(true);

        addButton.setVisible(false);
        addButton.setManaged(false);

        editToggleDisable(! collection.isOnline());

        updateEdit();
    }

    /**
     * Toggles if delete and title are disabled for edit screen
     * @param disabled true to disable, false to enable
     */
    public void editToggleDisable(boolean disabled)
    {
        collectionTitleTextField.setDisable(disabled);
        deleteButton.setDisable(disabled);
    }

    /**
     * Updates the combo box with current server urls
     */
    public void updateServerList()
    {
        List<String> urls = collectionManager.getDistinctServerURLs();
        serverAddressComboBox.setItems(FXCollections.observableArrayList(urls));
    }

    /**
     * Start of event handling chain for all data fields
     * This method just sets a timer if there is none to activate statusUpdate
     */
    public void changeDelay()
    {
        // Cancel any previously scheduled update
        if (changeTimer != null)
        {
            changeTimer.cancel();
        }

        // Schedule a new update task
        changeTimer = new Timer();
        changeTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                Platform.runLater(() -> updateChanges());
            }
        }, SAVING_DELAY);
    }

    /**
     * Decides what kind of update is needed
     */
    public void updateChanges()
    {
        if(collectionList.getSelectionModel().getSelectedItem() != null)
        {
            updateEdit();
        }
        else if(startAdditionButton.isDisable())
        {
            updateAddition();
        }
        else
        {
            clearSetup();
        }
    }

    /**
     * Handles updates for adding a collection
     */
    public void updateAddition()
    {
        addButton.setVisible(false);
        addButton.setManaged(false);
        String address = serverAddressComboBox.getValue();
        String title = collectionTitleTextField.getText();
        String nickname = localNicknameTextField.getText();
        if(address == null ||
                address.isEmpty() ||
                title == null ||
                title.isEmpty() ||
                nickname == null ||
                nickname.isEmpty())
        {
            emptyFieldStatus();
            return;
        }
        if(collectionManager.checkNickname(nickname))
        {
            duplicateNicknameStatus();
            return;
        }
        try
        {
            Collection byTitle = serverUtils.getCollectionByTitle(address, title);
            if(byTitle != null)
            {
                if(collectionManager.collectionExists(byTitle, address))
                {
                    collectionAlreadySavedStatus();
                }
                else
                {
                    collectionFoundStatus();
                }
            }
            else
            {
                collectionCanBeAddedStatus();
            }
        }
        catch(Exception e)
        {
            serverOfflineStatus();
            changeTimer = new Timer();
            changeTimer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    Platform.runLater(() -> updateChanges());
                }
            }, SAVING_DELAY * 10);
        }
    }

    /**
     * Does final server calls to subscribe/create collection
     */
    public void addCollection()
    {
        String address = serverAddressComboBox.getValue();
        String title = collectionTitleTextField.getText();
        String nickname = localNicknameTextField.getText();

        if(!address.endsWith("/"))
        {
            address += "/";
        }
        address = address.replaceFirst("(?i)http", "http");

        try
        {
            Collection byTitle = serverUtils.getCollectionByTitle(address, title);
            ClientCollection res;
            if(byTitle != null)
            {
                res = collectionManager.addCollection(byTitle, nickname, address);
            }
            else
            {
                Collection newCol = serverUtils.createCollection(address, title);
                res = collectionManager.addCollection(newCol, nickname, address);
            }
            updateList();
            collectionList.getSelectionModel().select(res.getNickname());
            baseCtrl.refreshFilter();
            currentNoteManager.refreshCollectionSelect();
        }
        catch(Exception e)
        {
            updateAddition();
        }
    }

    /**
     * Handles updates for editing existing collections
     */
    public void updateEdit()
    {
        String nickname = localNicknameTextField.getText();
        String title = collectionTitleTextField.getText();
        if(title == null || title.isEmpty() || nickname == null || nickname.isEmpty())
        {
            emptyFieldStatus();
        }
        else if(!collection.getNickname().equals(localNicknameTextField.getText()))
        {
            updateEditNickname();
        }
        else if(!collection.getTitle().equals(collectionTitleTextField.getText()))
        {
            updateEditTitle();
        }
        else
        {
            updateServerStatusEdit();
        }
        if(changeTimer != null)
        {
            changeTimer.cancel();
        }
        changeTimer = new Timer();
        changeTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                Platform.runLater(() -> updateChanges());
            }
        }, SAVING_DELAY * 10);
    }

    /**
     * Handles updates for nicknames during edit
     */
    public void updateEditNickname()
    {
        if(collectionManager.checkNickname(localNicknameTextField.getText()))
        {
            collectionTitleTextField.setDisable(true);
            duplicateNicknameStatus();
        }
        else
        {
            collectionTitleTextField.setDisable(! collection.isOnline());
            collection.setNickname(localNicknameTextField.getText());
            nicknameSavedStatus();
            updateList();
            collectionList.getSelectionModel().select(collection.getNickname());
            baseCtrl.refreshFilter();
            currentNoteManager.refreshCollectionSelect();
        }
    }

    /**
     * Handles updates for title during edit
     */
    public void updateEditTitle()
    {
        try
        {
            if(serverUtils.getCollectionByTitle(
                    collection.getServerURL(),
                    collectionTitleTextField.getText()
            ) != null)
            {
                localNicknameTextField.setDisable(true);
                titleAlreadyUsedStatus();
            }
            else
            {
                Collection newData = collection.getCollectionData();
                newData.setTitle(collectionTitleTextField.getText());
                serverUtils.updateCollection(collection.getServerURL(), newData);
                collection.setCollectionData(newData);
                localNicknameTextField.setDisable(false);
                titleSavedStatus();
            }
        }
        catch (Exception e)
        {
            collectionTitleTextField.setText(collection.getTitle());
            updateEdit();
        }
    }

    /**
     * Handles server status updates during edit
     */
    public void updateServerStatusEdit()
    {
        if(collection.isOnline())
        {
            editToggleDisable(false);
            serverOnlineStatus();
        }
        else
        {
            editToggleDisable(true);
            serverOfflineStatus();
        }
    }

    /**
     * Method to handle removal
     */
    public void handleRemove()
    {
        collectionManager.remove(collection);
        clearSetup();
        updateList();
        baseCtrl.refreshFilter();
        currentNoteManager.refreshCollectionSelect();
    }

    /**
     * Method to handle deletions
     */
    public void handleDelete()
    {
        boolean confirmed = popUpManager.showCollectionDeleteConfirmation();

        if (!confirmed)
        {
            return;
        }

        try
        {
            serverUtils.deleteCollection(
                    collection.getServerURL(),
                    collection.getCollectionData().getId()
            );
            collectionManager.remove(collection);
            clearSetup();
            updateList();
            baseCtrl.refreshFilter();
            currentNoteManager.refreshCollectionSelect();
        }
        catch (Exception e)
        {
            updateEdit();
        }
    }

    /**
     * Handles updates to default collection choice box
     */
    public void handleDefault()
    {
        String nickname = defaultCollectionChoiceBox.getValue();
        ClientCollection res = collectionManager.getCollectionByNickName(nickname);
        if(res != null)
        {
            collectionManager.setCurrentDefault(res);
        }
    }

    /**
     * Updates status to display message about empty fields
     */
    public void emptyFieldStatus()
    {
        circleRed();
        serverStatusText.textProperty().bind(languageManager.getProperty("empty_field"));
    }

    /**
     * Updates status to display message about duplicate nicknames
     */
    public void duplicateNicknameStatus()
    {
        circleRed();
        serverStatusText.textProperty().bind(languageManager.getProperty("duplicate_nickname"));
    }

    /**
     * Updates status to display message about already saved collection
     */
    public void collectionAlreadySavedStatus()
    {
        circleRed();
        serverStatusText.textProperty().bind(languageManager.getProperty("collection_saved"));
    }

    /**
     * Updates status to display message about collection being found
     * and activates addButton
     */
    public void collectionFoundStatus()
    {
        circleBlue();
        serverStatusText.textProperty().bind(languageManager.getProperty("collection_found"));
        addButton.setVisible(true);
        addButton.setManaged(true);
        addButton.textProperty().bind(languageManager.getProperty("subscribe_collection"));
    }

    /**
     * Updates status to display message about being able to create a collection
     * and activates addButton
     */
    public void collectionCanBeAddedStatus()
    {
        circleBlue();
        serverStatusText.textProperty()
                .bind(languageManager.getProperty("collection_can_be_added"));
        addButton.setVisible(true);
        addButton.setManaged(true);
        addButton.textProperty().bind(languageManager.getProperty("create_collection"));
    }

    /**
     * Updates status to display message about server being offline/unreachable
     */
    public void serverOfflineStatus()
    {
        circleRed();
        serverStatusText.textProperty().bind(languageManager.getProperty("server_cant_be_reached"));
    }

    /**
     * Updates status to display message about server being offline/unreachable
     */
    public void serverOnlineStatus()
    {
        circleBlue();
        serverStatusText.textProperty().bind(languageManager.getProperty("server_online"));
    }

    /**
     * Updates status to display message about title already being used
     */
    public void titleAlreadyUsedStatus()
    {
        circleRed();
        serverStatusText.textProperty()
                .bind(languageManager.getProperty("collection_title_already_used"));
    }

    /**
     * Updates status to display message about title being saved
     */
    public void titleSavedStatus()
    {
        circleBlue();
        serverStatusText.textProperty().bind(languageManager.getProperty("title_saved"));
    }

    /**
     * Updates status to display message about nickname being saved
     */
    public void nicknameSavedStatus()
    {
        circleBlue();
        serverStatusText.textProperty().bind(languageManager.getProperty("nickname_saved"));
    }

    /**
     * Updates status circle to be red
     */
    public void circleRed()
    {
        statusCircle.getStyleClass().setAll("red-status-circle");
    }

    /**
     * Updates status circle to be blue
     */
    public void circleBlue()
    {
        statusCircle.getStyleClass().setAll("blue-status-circle");
    }

    /**
     * Updates status circle to be transparent
     */
    public void circleEmpty()
    {
        statusCircle.getStyleClass().setAll("empty-status-circle");
    }

    /**
     * Handles WS notifications about updates
     * @param notifCollection the updated collection
     */
    public void updateNotif(ClientCollection notifCollection)
    {
        if(notifCollection.equals(collection))
        {
            collectionTitleTextField.setText(collection.getTitle());
        }
    }

    /**
     * Handles WS notifications about deletion
     * @param notifCollection the deleted collection
     */
    public void deleteNotif(ClientCollection notifCollection)
    {
        if(notifCollection.equals(collection))
        {
            clearSetup();
        }
        updateList();
        baseCtrl.refreshFilter();
        currentNoteManager.refreshCollectionSelect();
    }
}
