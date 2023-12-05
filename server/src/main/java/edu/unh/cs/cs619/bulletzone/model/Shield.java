package edu.unh.cs.cs619.bulletzone.model;

public class Shield extends FieldEntity{

    int pos;

    public Shield(){
        pos = 2003;
    }

    public Shield(int pos){
        this.pos = pos;
    }

    @Override
    public int getIntValue() {
        return 3131;
    }

    @Override
    public FieldEntity copy() {
        return new Shield();
    }


    @Override
    public String toString() {
        return "W";
    }

    public int getPos(){
        return pos;
    }

    public void giveEffects(FieldEntity curr){
        //Do timer shit here
    }
}
