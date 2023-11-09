package edu.unh.cs.cs619.bulletzone.model;

public class Forest extends FieldEntity {
    int pos;
    int destructValue;

    public Forest(){
        pos = 3;
    }

    public Forest(int pos){
        this.pos = pos;
    }

    @Override
    public int getIntValue() {
        return 3;
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

