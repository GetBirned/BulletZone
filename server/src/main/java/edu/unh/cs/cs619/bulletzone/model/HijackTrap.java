package edu.unh.cs.cs619.bulletzone.model;

import java.util.Timer;

public class HijackTrap extends FieldEntity{
    int pos;

    public HijackTrap(){
        pos = 2345;
    }

    public HijackTrap(int userID){
        this.pos = Integer.parseInt("2345" + String.valueOf(userID));
    }

    @Override
    public int getIntValue() {
        return this.pos;
    }

    @Override
    public FieldEntity copy() {
        return new HijackTrap();
    }


    @Override
    public String toString() {
        return "HijackTrap";
    }

    public int getPos(){
        return pos;
    }


}
