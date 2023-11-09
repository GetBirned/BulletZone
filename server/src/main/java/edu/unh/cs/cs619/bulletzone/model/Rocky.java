package edu.unh.cs.cs619.bulletzone.model;

public class Rocky extends FieldEntity {
    int pos;
    int destructValue;

    public Rocky(){
        pos = 1;
    }

    public Rocky(int pos){
        this.pos = pos;
    }

    @Override
    public int getIntValue() {
        return 1;
    }

    @Override
    public FieldEntity copy() {
        return new Hill();
    }


    @Override
    public String toString() {
        return "W";
    }

    public int getPos(){
        return pos;
    }
}

