package edu.unh.cs.cs619.bulletzone.repository;

import edu.unh.cs.cs619.bulletzone.model.Game;
import edu.unh.cs.cs619.bulletzone.model.Wall;

/**
 * Method to break up InGameMemoryRepository
 */
public class FieldEntities {

    public Game set(Game game) {
        game.getHolderGrid().get(1).setFieldEntity(new Wall());
        game.getHolderGrid().get(2).setFieldEntity(new Wall());
        game.getHolderGrid().get(3).setFieldEntity(new Wall());

        game.getHolderGrid().get(17).setFieldEntity(new Wall());
        game.getHolderGrid().get(33).setFieldEntity(new Wall(1500, 33));
        game.getHolderGrid().get(49).setFieldEntity(new Wall(1500, 49));
        game.getHolderGrid().get(65).setFieldEntity(new Wall(1500, 65));

        game.getHolderGrid().get(34).setFieldEntity(new Wall());
        game.getHolderGrid().get(66).setFieldEntity(new Wall(1500, 66));

        game.getHolderGrid().get(35).setFieldEntity(new Wall());
        game.getHolderGrid().get(51).setFieldEntity(new Wall());
        game.getHolderGrid().get(67).setFieldEntity(new Wall(1500, 67));

        game.getHolderGrid().get(5).setFieldEntity(new Wall());
        game.getHolderGrid().get(21).setFieldEntity(new Wall());
        game.getHolderGrid().get(37).setFieldEntity(new Wall());
        game.getHolderGrid().get(53).setFieldEntity(new Wall());
        game.getHolderGrid().get(69).setFieldEntity(new Wall(1500, 69));

        game.getHolderGrid().get(7).setFieldEntity(new Wall());
        game.getHolderGrid().get(23).setFieldEntity(new Wall());
        game.getHolderGrid().get(39).setFieldEntity(new Wall());
        game.getHolderGrid().get(71).setFieldEntity(new Wall(1500, 71));

        game.getHolderGrid().get(8).setFieldEntity(new Wall());
        game.getHolderGrid().get(40).setFieldEntity(new Wall());
        game.getHolderGrid().get(72).setFieldEntity(new Wall(1500, 72));

        game.getHolderGrid().get(9).setFieldEntity(new Wall());
        game.getHolderGrid().get(25).setFieldEntity(new Wall());
        game.getHolderGrid().get(41).setFieldEntity(new Wall());
        game.getHolderGrid().get(57).setFieldEntity(new Wall());
        game.getHolderGrid().get(73).setFieldEntity(new Wall());
        return game;
    }
}
