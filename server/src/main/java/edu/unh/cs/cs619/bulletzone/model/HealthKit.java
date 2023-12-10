package edu.unh.cs.cs619.bulletzone.model;

import java.util.Timer;

public class HealthKit extends FieldEntity{
    int pos;

    public HealthKit(){
        pos = 2003;
    }

    public HealthKit(int pos){
        this.pos = pos;
    }

    @Override
    public int getIntValue() {
        return 3141;
    }

    @Override
    public FieldEntity copy() {
        return new HealthKit();
    }


    @Override
    public String toString() {
        return "HealthKit";
    }

    public int getPos(){
        return pos;
    }


}
