package edu.unh.cs.cs619.bulletzone.model;
public class GameBoardBuilder implements BoardBuilder {

    private GameBoard gameBoard = null;
    private final Object monitor = new Object();

    public GameBoardBuilder() {}

    public GameBoard build() {
        synchronized (this.monitor) {
            gameBoard = new GameBoard();
            this.set(gameBoard);
            return gameBoard;
        }
    }

    public GameBoard build(FieldEntity terrain) {
        synchronized (this.monitor) {
            gameBoard = new GameBoard();
            this.set(gameBoard);
            return gameBoard;
        }
    }

    public GameBoard getBoard() {
        return gameBoard;
    }

    private GameBoard set(GameBoard gb, FieldEntity terrain) {
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                gb.setEntity(j, i, terrain);
            }
        }
        return gb;
    }

    private GameBoard set(GameBoard gb) {

        gb.setEntity(3, 2, new Hill());
        gb.setEntity(4, 2, new Hill());
        gb.setEntity(5, 2, new Hill());
        gb.setEntity(6, 2, new Hill());

        gb.setEntity(3, 3, new Hill());
        gb.setEntity(4, 3, new Hill());
        gb.setEntity(5, 3, new Hill());
        gb.setEntity(6, 3, new Hill());

        gb.setEntity(7, 4, new Wall());
        gb.setEntity(8, 4, new Wall());
        gb.setEntity(9, 4, new Wall());
        gb.setEntity(10, 4, new Wall());
        gb.setEntity(11, 4, new Wall());

        gb.setEntity(7, 5, new Wall());

        gb.setEntity(3, 7, new Wall());
        gb.setEntity(4, 7, new Wall());

        gb.setEntity(9, 5, new Rocky());
        gb.setEntity(10, 5, new Rocky());
        gb.setEntity(11, 5, new Rocky());
        gb.setEntity(12, 5, new Rocky());

        gb.setEntity(9, 6, new Rocky());
        gb.setEntity(10, 6, new Rocky());
        gb.setEntity(11, 6, new Rocky());
        gb.setEntity(12, 6, new Rocky());

        gb.setEntity(10, 9, new Wall());
        gb.setEntity(10, 10, new Wall());
        gb.setEntity(10, 11, new Wall());
        gb.setEntity(10, 12, new Wall());

        gb.setEntity(12, 10, new Hill());
        gb.setEntity(13, 10, new Hill());
        gb.setEntity(12, 11, new Hill());
        gb.setEntity(13, 11, new Hill());

        gb.setEntity(3, 10, new Forest());
        gb.setEntity(4, 10, new Forest());
        gb.setEntity(5, 10, new Forest());
        gb.setEntity(6, 10, new Forest());

        gb.setEntity(3, 11, new Forest());
        gb.setEntity(4, 11, new Forest());
        gb.setEntity(5, 11, new Forest());
        gb.setEntity(6, 11, new Forest());

        gb.setEntity(6, 9, new HealthKit());
        gb.setEntity(13, 11, new Shield());
        gb.setEntity(3, 8, new Thingamajig());
        gb.setEntity(3, 12, new nukePowerUp());
        gb.setEntity(4, 4, new applePowerUp());

        gb.setEntity(1, 13, new Water());
        gb.setEntity(2, 13, new Water());
        gb.setEntity(3, 13, new Water());
        gb.setEntity(4, 13, new Water());
        gb.setEntity(5, 13, new Water());

        gb.setEntity(1, 14, new Water());
        gb.setEntity(2, 14, new Water());
        gb.setEntity(3, 14, new Water());
        gb.setEntity(4, 14, new Water());
        gb.setEntity(5, 14, new Water());

        gb.setEntity(11, 1, new Water());
        gb.setEntity(12, 1, new Water());
        gb.setEntity(13, 1, new Water());

        return gb;
    }
}
