package edu.unh.cs.cs619.bulletzone.util;

import java.util.ArrayList;

public class ArrayListWrapper {
    private ArrayList<Integer> result;


    public ArrayListWrapper() {

    }

    public ArrayListWrapper(ArrayList<Integer> result) {
        this.result = result;
    }

    public ArrayList<Integer> isResult() {
        return result;
    }

    public void setResult(ArrayList<Integer> result) {
        this.result = result;
    }

}
