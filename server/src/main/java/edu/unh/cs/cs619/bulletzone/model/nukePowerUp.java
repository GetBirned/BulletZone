package edu.unh.cs.cs619.bulletzone.model;
//FUSION POWERUP
public class nukePowerUp extends FieldEntity{
    int pos;
    int destructValue;

    public nukePowerUp(){
        pos = 2003;
    }

    public nukePowerUp(int pos){
        this.pos = pos;
    }

    @Override
    public int getIntValue() {
        return 2003;
    }

    @Override
    public FieldEntity copy() {
        return new Hill();
    }


    @Override
    public String toString() {
        return "nuke";
    }

    public int getPos(){
        return pos;
    }
}
