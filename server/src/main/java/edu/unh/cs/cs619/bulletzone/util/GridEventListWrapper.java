package edu.unh.cs.cs619.bulletzone.util;

import java.util.LinkedList;

import edu.unh.cs.cs619.bulletzone.repository.GridEvent;

public class GridEventListWrapper {
    private LinkedList<GridEvent> collection;

    public GridEventListWrapper(LinkedList<GridEvent>  input) {
        this.collection = input;
    }

    public LinkedList<GridEvent>  getGrid() {
        return this.collection;
    }

    public void setGrid(LinkedList<GridEvent>  set) {
        this.collection = set;
    }
}
