package client.utils;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.util.Map;

public class FlagCellFactory implements Callback<ListView<String>, ListCell<String>>
{
    private final Map<String, String> languages;

    /**
     * Initializes the languages field
     * @param languages - parameter for this method
     */
    public FlagCellFactory(Map<String, String> languages)
    {
        this.languages = languages;
    }

    /**
     * Binds the flag image with the language selected
     * @param param a List View of Strings
     * @return the list cell
     */
    @Override
    public ListCell<String> call(ListView<String> param)
    {
        return new ListCell<>()
        {
            @Override
            protected void updateItem(String languageCode, boolean empty)
            {
                super.updateItem(languageCode, empty);
                if (languageCode == null || empty)
                {
                    setGraphic(null);
                    setText(null);
                } else
                {
                    // Display flag and language name
                    String languageName = languages.getOrDefault(languageCode, languageCode);

                    HBox hbox = new HBox(5); // Spacing of 5 pixels
                    ImageView flagImageView = new ImageView();

                    // Load the flag image
                    try
                    {
                        Image flagImage = new Image(getClass().
                                getResourceAsStream("/flags/" + languageCode + ".png"));
                        flagImageView.setImage(flagImage);
                        flagImageView.setFitHeight(16); // Adjust flag height
                        flagImageView.setFitWidth(24);  // Adjust flag width
                    }
                    catch (Exception e)
                    {
                        System.err.println("Error loading flag image for " + languageCode);
                        flagImageView.setImage(null); // Fallback in case of error
                    }

                    Text text = new Text(languageName);
                    hbox.getChildren().addAll(flagImageView, text);

                    setGraphic(hbox);
                    setText(null); // Don't show default text
                    setStyle ("""
                        -fx-border-width: 0;
                        -fx-background-color: -bg-primary;
                        """);
                }
            }
        };
    }
}
