/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.scenes;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

public class MainCtrl
{
    private Stage primaryStage;
    private Stage secondaryStage;

    private BaseCtrl baseCtrl;
    private Scene base;

    private CollectionCtrl collectionCtrl;
    private Scene collections;


    /**
     * Initializes the primary stage
     * with the base scene and loads the css files
     * @param primaryStage The primary stage to load
     * @param base The base FXML scene
     * @param collections The collections FXML scene
     */
    public void initialize(
            Stage primaryStage,
            Pair<BaseCtrl, Parent> base,
            Pair<CollectionCtrl, Parent> collections)
    {
        this.primaryStage = primaryStage;
        this.secondaryStage = new Stage();

        this.baseCtrl = base.getKey();
        this.base = new Scene(base.getValue());

        this.collectionCtrl = collections.getKey();
        this.collections = new Scene(collections.getValue());

        this.base.getStylesheets().add("client/styles/NetNote.css");
        this.collections.getStylesheets().add("client/styles/NetNote.css");

        showBase();
        primaryStage.setOnCloseRequest(e ->
        {
            baseCtrl.close();
            Platform.exit();
            System.exit(0);
        });
    }

    /**
     * Show base scene
     */
    public void showBase()
    {
        primaryStage.setTitle("NetNote");
        primaryStage.setScene(base);
        primaryStage.show();
        primaryStage.toFront();
    }

    /**
     * Show collections scene in a new window
     */
    public void showCollections()
    {
        collectionCtrl.clearSetup();
        collectionCtrl.updateList();
        secondaryStage.setScene(collections);
        secondaryStage.setTitle("NetNote");
        secondaryStage.show();
        secondaryStage.toFront();
    }

    /**
     * Close the collections scene
     */
    public void closeCollections()
    {
        secondaryStage.close();
    }
}