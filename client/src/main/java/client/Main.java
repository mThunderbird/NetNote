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
package client;

import client.scenes.BaseCtrl;
import client.scenes.CollectionCtrl;
import client.scenes.MainCtrl;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.google.inject.Guice.createInjector;

public class Main extends Application
{

    private static final Injector INJECTOR = createInjector(new MyModule());
    private static final MyFXML FXML = new MyFXML(INJECTOR);

    /**
     * Main method of client
     * @param args command line arguments
     * @throws URISyntaxException URI syntax exception
     * @throws IOException IO exception
     */
    public static void main(String[] args) throws URISyntaxException, IOException
    {
        launch();
    }

    /**
     * Starts the javafx application
     * @param primaryStage the primary stage of the application
     * @throws Exception multiple possible exceptions
     */
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        var base = FXML.load(BaseCtrl.class, "client", "scenes", "Base.fxml");
        var collections = FXML.load(CollectionCtrl.class, "client", "scenes", "Collections.fxml");

        var mainCtrl = INJECTOR.getInstance(MainCtrl.class);
        mainCtrl.initialize(primaryStage, base, collections);

        ResourceBundle bundle = ResourceBundle.getBundle("lang", Locale.ENGLISH);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/scenes/Base.fxml"));
        loader.setResources(bundle);
    }
}