package edu.unh.cs.cs619.bulletzone.model;

import java.util.Timer;

public class Mine extends FieldEntity{
    int pos;

    public Mine(){
        pos = 83030;
    }

    public Mine(int pos){
        this.pos = pos;
    }

    @Override
    public int getIntValue() {
        return 83030;
    }

    @Override
    public FieldEntity copy() {
        return new Mine();
    }


    @Override
    public String toString() {
        return "Mine";
    }

    public int getPos(){
        return pos;
    }


}
