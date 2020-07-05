# Game Of Three

## Requirements
To run this application, you'll need:
- Java 11 SDK
- A modern web browser with JavaScript enabled.

## Running the Application
The application can be run from within a modern Java IDE with the entry class being `GameOfThreeApplication.java`.
The application can also be run using Maven by executing the command: `mvn spring-boot:run` in the root folder of the application.

## Using the Application
The application starts up on port 8080. Pointing your web browser to http://localhost:8080 should present
the application's landing page.

To connect to the _game engine_, a **username** is required to identify each connected player. Feel free to type
any username of your choosing. Please note that no two players can have the same username.

The game/application can be played in both _automatic_ and _manual_ modes. Use the **Game Mode** dropdown to
specify your preferred mode. Please note that this value can be updated at any point during game play.

After specifying both username and game mode, clicking **Connect** button connects you to the game engine (via a websocket).
This action will attempt to automatically pair you with a player if another connected player is available. If not, you will be presented
with a message stating that you will have to wait for another player to become available. Once you have been paired,a game play session will commence.

In automatic game mode, a random number is generated on behalf of the 1st player for the game to commence. In manual mode
the player is required to specify the random number (captured through an input field).

After the random number has been generated, both players can go ahead with adding (either automatically or manually) the required number (-1, 0 or 1) 
to the values they receive on subsequent instructions. This exchange continues until there is a winner.
Players can choose to rematch after a game session has been concluded.

Messages during game play are displayed in a _game board_. 