package client.src;

import client.utils.LanguageManager;
import jakarta.inject.Inject;
import javafx.animation.FadeTransition;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Optional;

public class PopUpManager
{
    private VBox errorPane;
    public static final int ERROR = 1;
    public static final int RESOLVED = 2;

    public static final int DURATION = 6;

    @Inject
    private LanguageManager languageManager;

    /**
     * Show a confirmation dialog with the given title, header, and content.
     * @param title The title of the dialog
     * @param header The header of the dialog
     * @param content The content of the dialog
     * @return True if the user clicked "OK", false otherwise
     */
    private boolean showConfirmation(String title, String header, String content)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        ((Button) (alert.getDialogPane().lookupButton(ButtonType.OK))).setText(
                languageManager.getProperty("ok").getValue());
        ((Button) (alert.getDialogPane().lookupButton(ButtonType.CANCEL))).setText(
                languageManager.getProperty("cancel").getValue());

        // Show the dialog and capture the result
        Optional<ButtonType> result = alert.showAndWait();

        // Return true if the user clicked "OK"
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Show an error/resolve notification message with the message and linger for a few seconds
     * @param message The message to display
     * @param durationInSeconds The duration to show the notification
     * @param status Use PopUpManager constants ERROR or RESOLVED
     */
    private void showSnackNotification
    (String message, int durationInSeconds, int status)
    {
        VBox notificationBox = new VBox();

        if (message.length() > 150)
        {
            message = message.substring(0, 150) + "...";
        }
        Text text = new Text(message);

        notificationBox.getChildren().add(text);

        if (status == ERROR)
            notificationBox.getStyleClass().add("error-snack-notification");
        else if (status == RESOLVED)
            notificationBox.getStyleClass().add("resolved-snack-notification");

        notificationBox.setMaxWidth(text.getWrappingWidth());

        // Add the notification box to the parent pane
        errorPane.getChildren().add(notificationBox);

        // Fade in transition
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), notificationBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Fade out transition after the duration
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), notificationBox);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.seconds(durationInSeconds));

        // Remove the notification after fade-out
        fadeOut.setOnFinished(event -> errorPane.getChildren().remove(notificationBox));

        // Play both transitions
        fadeIn.setOnFinished(event -> fadeOut.play());
        fadeIn.play();
    }

    /**
     * Show a prepared notification that a server is offline on startup / went offline
     * @param serverURL The URL of the server
     */
    public void serverOfflineNotification(String serverURL)
    {
        String msg = serverURL + " " +
                languageManager.getProperty("notif_server_offline").getValue();
        showSnackNotification(msg, DURATION, ERROR);
    }

    /**
     * Show a prepared notification that a server has come online
     * @param serverURL The URL of the server
     */
    public void serverOnlineNotification(String serverURL)
    {
        String msg = serverURL + " " +
                languageManager.getProperty("notif_server_online").getValue();
        showSnackNotification(msg, DURATION, RESOLVED);
    }

    /**
     * Show a prepared notification that a note has failed to move
     */
    public void cannotMoveNoteErrorNotification()
    {
        showSnackNotification(languageManager.getProperty("notif_cannot_move_note").getValue(),
                DURATION, ERROR);
    }

    /**
     * Show a prepared notification that a note cannot be
     * added because the collection to add into is not available
     */
    public void noCollectionAvailableNotification()
    {
        showSnackNotification(languageManager
                        .getProperty("notif_collection_unavailable").getValue(),
                DURATION ,PopUpManager.ERROR);
    }

    public void setErrorPane(VBox errorPane)
    {
        this.errorPane = errorPane;
    }

    /**
     * Show a confirmation dialog for deleting a note
     * @return True if the user clicked "OK", false otherwise
     */
    public boolean showNoteDeleteConfirmation()
    {
        return showConfirmation(languageManager.getProperty("delete_action").getValue(),
                languageManager.getProperty("delete_note_confirm").getValue(),
                languageManager.getProperty("undoable_action").getValue());
    }

    /**
     * Show a confirmation dialog for deleting a collection
     * @return True if the user clicked "OK", false otherwise
     */
    public boolean showCollectionDeleteConfirmation()
    {
        return showConfirmation(languageManager.getProperty("delete_action").getValue(),
                languageManager.getProperty("delete_collection_confirm").getValue(),
                languageManager.getProperty("undoable_action").getValue());
    }

    /**
     * Show a prepared notification for refreshing the application
     */
    public void refreshNotification()
    {
        showSnackNotification(languageManager.getProperty("notif_refresh").getValue(),
                DURATION/3, RESOLVED);
    }
}
