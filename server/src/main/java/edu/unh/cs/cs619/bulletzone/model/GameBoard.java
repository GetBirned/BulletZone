package edu.unh.cs.cs619.bulletzone.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameBoard {
    private final int FIELD_DIM = 16;
    private final Object monitor = new Object();
    private final ArrayList<FieldHolder> holderGrid = new ArrayList<>();
    public GameBoard() {
        this.createFieldHolderGrid();
    }

    public void add(FieldHolder cell) {

    }

    public FieldHolder get(int x, int y) {
        return holderGrid.get(y * FIELD_DIM + x);
    }

    public void setEntity(int x, int y, FieldEntity f) {
        holderGrid.get(y * FIELD_DIM + x).setFieldEntity(f);
    }
    public ArrayList<FieldHolder> getHolderGrid() {
        return holderGrid;
    }

    private void createFieldHolderGrid() {
        synchronized (monitor) {
            holderGrid.clear();
            for (int i = 0; i < FIELD_DIM * FIELD_DIM; i++) {
                holderGrid.add(new FieldHolder());
            }

            FieldHolder targetHolder;
            FieldHolder rightHolder;
            FieldHolder downHolder;

            // Build connections
            for (int i = 0; i < FIELD_DIM; i++) {
                for (int j = 0; j < FIELD_DIM; j++) {
                    targetHolder = holderGrid.get(i * FIELD_DIM + j);
                    rightHolder = holderGrid.get(i * FIELD_DIM
                            + ((j + 1) % FIELD_DIM));
                    downHolder = holderGrid.get(((i + 1) % FIELD_DIM)
                            * FIELD_DIM + j);

                    targetHolder.addNeighbor(Direction.Right, rightHolder);
                    rightHolder.addNeighbor(Direction.Left, targetHolder);

                    targetHolder.addNeighbor(Direction.Down, downHolder);
                    downHolder.addNeighbor(Direction.Up, targetHolder);
                }
            }
        }
    }
}
