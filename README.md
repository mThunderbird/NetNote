# NetNote by CSEP-90

# Important disclaimer !!!
This project was a university project which was done in a monitored GitLab repository, which included task planning (Agile style), testing and code style pipeline.


However, I cannot import all of this into GitHub, so I have uploaded only the code as a showcase.


I have left all the code we created in the span of 2 months, as it was when we submitted the project, which earned an almost perfect grade of 9.5


It was done in collaboration with 3 other colleagues - Sandijs Rakstiņš, Paula Darii, and Miruna Toşa

## Installing and launching NetNote
To run NetNote from the command line,
you either need to have [Maven](https://maven.apache.org/install.html) installed on your local system (`mvn`)
or you need to use the Maven wrapper (`mvnw`).

You can then execute

	mvn clean install

to package and install the artifacts for the three subprojects.

Afterwards, you can run ...

	cd server
	mvn spring-boot:run

to start the server or ...

	cd client
	mvn javafx:run

to run the client.

## CSS files

The CSS file to edit the WebView is found at `client/src/main/resources/client/styles/WebView.css`

In the same folder there is also `NetNote.css` where the colors for the rest of the app can be adjusted

## Configuration file

The configuration file is located at `client/src/main/resources/config.json`

For the **first launch** of the app the configuration file will not exist unless added manually

## Other files

The structure given to us by the template project hasn't been altered  
as the team did not see a good reason for it

Therefore - **used files from the template project are still at the same locations**

## Added extra features

- Multi-Collection
- Automated Change Synchronization
- Live Language Switch

### Multi-Collection

**WE SUPPORT MULTIPLE SERVERS**

As mentioned before, on a clean launch of the client app there is no config file and so no collection by default

The app supports both creating and subscribing to collections done automatically
when user provides server url and title in collection management screen

Only one major limitation: **once created the collection can not change servers while individual notes can**

### Automated Change Synchronization

The app supports websockets with multiple servers

All changes that are not stored locally are transmitted to other connected clients

The changes transmitted are:
- Collection title updated (half a second after user finished writing)
- Collection deleted
- New note in subscribed collection
- Note title update (goes half a second after user finished writing)
- Note body update (goes half a second after user finished writing or changes length by 15 characters)
- Note deleted
- Note moved collection

The log of all attempted connections can be seen in the console window

The user will receive a pop-up in main window when server disconnects / reconnects

### Live Language Switch

For live language switch we have added these languages:
- English
- Dutch
- German
- Romanian

We have translated everything we could think of except default note titles as they are server generated

This also means all possible errors we expect and have handled are also translated

## Usability/Accessibility

### Keyboard shortcuts

#### In main page:
|Shortcut | Action |
| ------ | ------ |
| ESC | focus on search bar |
| CTRL + N | create new note |
| CTRL + UP/DOWN | cycle trough note list (chosen like this because the list is vertical) |
| CTRL + LEFT/RIGHT | cycle through collection filter |
| CTRL + M | open the collection management page|
| CTRL + Q | close the app |

#### In collection management page:
|Shortcut | Action |
| ------ | ------ |
| ESC | focus on search bar |
| CTRL + N | open the add collection setup |
| CTRL + UP/DOWN | cycle through collection list (chosen like this because the list is vertical) |
| CTRL + M | close the collection management page |
| CTRL + Q | close the collection management page |

### Keyboard navigation

Additionally, to the keyboard shortcuts mentioned before, the navigation can be done with these keys:
- TAB - moves focus forward
- SHIFT + TAB - moves focus backward
- ENTER - confirm actions
- SPACE - used to open dropdowns and confirm actions
- UP/DOWN - select entry in dropdown or collection list

The focus is shown by the dashed borders

### Undo actions

Not supported

## Comments

If you are encountering issues launching the client on linux you can try changing
the 2 mentions of `client/src/main/resources/config.json` to `src/main/resources/config.json`
in `client/src/main/java/client/utils/Configuration.java`
