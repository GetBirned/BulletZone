package edu.unh.cs.cs619.bulletzone.model;

public class Grass extends FieldEntity{
    int pos;

    public Grass(){
        pos = 2003;
    }

    public Grass(int pos){
        this.pos = pos;
    }

    @Override
    public int getIntValue() {
        return 0;
    }

    @Override
    public FieldEntity copy() {
        return new Grass();
    }


    @Override
    public String toString() {
        return "W";
    }

    public int getPos(){
        return pos;
    }
}
