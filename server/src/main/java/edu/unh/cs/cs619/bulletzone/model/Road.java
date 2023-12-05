package edu.unh.cs.cs619.bulletzone.model;

public class Road extends FieldEntity {
    int pos;
    int destructValue;

    public Road(){
        pos = 2;
    }

    public Road(int pos){
        this.pos = pos;
    }

    @Override
    public int getIntValue() {
        return 70;
    }

    @Override
    public FieldEntity copy() {
        return new Road();
    }


    @Override
    public String toString() {
        return "R";
    }

    public int getPos(){
        return pos;
    }
}

