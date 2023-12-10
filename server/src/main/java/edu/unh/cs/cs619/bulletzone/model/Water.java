package edu.unh.cs.cs619.bulletzone.model;

public class Water extends FieldEntity{
    int pos;

    public Water(){
        pos = 2;
    }

    public Water(int pos){
        this.pos = pos;
    }

    @Override
    public int getIntValue() {
        return 50;
    }

    @Override
    public FieldEntity copy() {
        return new Water();
    }


    @Override
    public String toString() {
        return "Water";
    }

    public int getPos(){
        return pos;
    }
}
