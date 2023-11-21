package edu.unh.cs.cs619.bulletzone.model;
//ANTI-GRAV POWERUP
public class applePowerUp extends FieldEntity {

    int pos;
    int destructValue;

    public applePowerUp(){
        pos = 2002;
    }

    public applePowerUp(int pos){
        this.pos = pos;
    }

    @Override
    public int getIntValue() {
        return 2002;
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
