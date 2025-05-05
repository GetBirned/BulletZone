BulletZone is a real-time multiplayer strategy game developed for Android devices where players control tanks, soldiers, and builders on a dynamically changing battlefield. Players can collect powerups, build terrain, and compete for dominance on a 16x16 grid-based map.

Platform:
Android (Java)

AndroidAnnotations for DI and boilerplate reduction

RESTful communication with server backend (Spring REST client)

Gameplay Features:
Control different unit types: tanks, soldiers, builders

Dynamic terrain effects (hills, forests, water, etc.)

Powerups including nukes, shields, and tools

Currency system and powerup trading

Replay system for reviewing past matches

Major Components:
GridAdapter.java
Handles rendering the game grid, syncing entity positions, and managing interactions between players and the game world.

GridEventHandler.java
Listens to grid update events from the server and triggers UI updates.

ReplayGridAdapter.java
Displays saved replays of previous games, preserving the original grid state and unit positions.

TerrainUI.java
Manages the visual representation of units and terrain tiles depending on direction and type.

Authentication
AuthenticateActivity and AuthenticationController handle user login and session management.

BuilderButtonController
Provides UI controls for builder-specific actions like constructing bridges, roads, and walls.

Networking:
Uses a REST client (BulletZoneRestClient) to sync game state and player actions with the central server.

Player actions (movement, building, powerup collection) are communicated to the server for validation and propagation.

Notable Features:
File-based local storage of player IDs and replay data

Asynchronous communication to update player balance and powerup assignments

Modular design supporting extensibility for new units and terrain types

Development Notes:
Designed and implemented in an academic setting (CS619: Software Development at UNH)

Prioritized modularity, scalability, and efficient UI rendering

Extensively used AndroidAnnotations and Square's Otto event bus for decoupled architecture

How to Build & Run:
Clone the repository:

bash
Copy
Edit
git clone https://github.com/GetBirned/BulletZone.git
Open the project in Android Studio.

Sync Gradle dependencies.

Build and run on an Android device or emulator.
