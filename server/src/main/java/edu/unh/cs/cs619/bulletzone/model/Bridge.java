package edu.unh.cs.cs619.bulletzone.model;

public class Bridge extends FieldEntity {
    int pos;
    int destructValue;

    public Bridge(){
        pos = 2;
    }

    public Bridge(int pos){
        this.pos = pos;
    }

    @Override
    public int getIntValue() {
        return 60;
    }

    @Override
    public FieldEntity copy() {
        return new Road();
    }


    @Override
    public String toString() {
        return "B";
    }

    public int getPos(){
        return pos;
    }
}
