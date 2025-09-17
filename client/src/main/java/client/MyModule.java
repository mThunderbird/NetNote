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
import client.src.CollectionManager;
import client.src.CurrentNoteManager;
import client.src.NoteManager;
import client.src.PopUpManager;
import client.utils.Configuration;
import client.utils.LanguageManager;
import client.utils.MyWebSocketClient;
import client.utils.ServerUtils;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;

import client.scenes.MainCtrl;

public class MyModule implements Module
{
    /**
     * Configure method for client
     * @param binder the binder used for defining scopes
     */
    @Override
    public void configure(Binder binder)
    {
        binder.bind(MainCtrl.class).in(Scopes.SINGLETON);
        binder.bind(BaseCtrl.class).in(Scopes.SINGLETON);
        binder.bind(CollectionCtrl.class).in(Scopes.SINGLETON);
        binder.bind(NoteManager.class).in(Scopes.SINGLETON);
        binder.bind(CurrentNoteManager.class).in(Scopes.SINGLETON);
        binder.bind(MyWebSocketClient.class).in(Scopes.SINGLETON);
        binder.bind(ServerUtils.class).in(Scopes.SINGLETON);
        binder.bind(CollectionManager.class).in(Scopes.SINGLETON);
        binder.bind(Configuration.class).in(Scopes.SINGLETON);
        binder.bind(LanguageManager.class).in(Scopes.SINGLETON);
        binder.bind(PopUpManager.class).in(Scopes.SINGLETON);
    }
}