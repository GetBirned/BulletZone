package edu.unh.cs.cs619.bulletzone.model;

public class Thingamajig extends FieldEntity{
    int pos;
    int destructValue;

    public Thingamajig(){
        pos = 7;
    }

    public Thingamajig(int pos){
        this.pos = pos;
    }

    @Override
    public int getIntValue() {
        return 7;
    }

    @Override
    public FieldEntity copy() {
        return new Hill();
    }


    @Override
    public String toString() {
        return "Thingamajig";
    }

    public int getPos(){
        return pos;
    }
}
