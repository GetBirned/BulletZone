package edu.unh.cs.cs619.bulletzone.model;
public class GameBoardBuilder {

    public GameBoard gameBoard = null;
    private final Object monitor = new Object();

    public GameBoardBuilder(GameBoard gameBoard) {
        this.create(gameBoard);
    }

    public GameBoard create(GameBoard gb) {
        if (gb != null) {
            gameBoard = gb;
            return gb;
        }
        synchronized (this.monitor) {
            gb = new GameBoard();
            this.set(gb);
            gameBoard = gb;
            return gb;
        }
    }

    public GameBoard getBoard() {
        return gameBoard;
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

        gb.setEntity(6, 9, new nukePowerUp());
        gb.setEntity(13, 11, new applePowerUp());
        gb.setEntity(3, 8, new Thingamajig());

        return gb;
    }
}
