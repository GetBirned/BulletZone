package edu.unh.cs.cs619.bulletzone.model;

public class Hill extends FieldEntity {
    int pos;
    int destructValue;

    public Hill(){
        pos = 2;
    }

    public Hill(int pos){
        this.pos = pos;
    }

    @Override
    public int getIntValue() {
        return 2;
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

