package edu.unh.cs.cs619.bulletzone.model;

import edu.unh.cs.cs619.bulletzone.datalayer.core.Entity;

public class Wall extends FieldEntity {
    public int destructValue, pos;
    public int tag;

    public Wall(){
        this.destructValue = 1000;
        this.tag = 0;
    }

    public Wall(int destructValue, int pos){
        this.destructValue = destructValue;
        this.pos = pos;
        this.tag = 1;
    }
    @Override
    public FieldEntity copy() {
        return new Wall();
    }

    @Override
    public int getIntValue() {
        return destructValue;
    }

    @Override
    public String toString() {
        return "W";
    }

    public int getPos(){
        return pos;
    }



    public void takeDamage(int othersArmor) {
        int damage = (int) Math.ceil(othersArmor * .1);
        this.destructValue = this.destructValue - damage;
    }
    public void takeDamagefromSoldier(int othersArmor) {
        int damage = (int) Math.ceil(othersArmor * .4);
        this.destructValue = this.destructValue - damage;
    }
}
