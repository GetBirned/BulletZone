package edu.unh.cs.cs619.bulletzone.model;

public class BuilderWall extends FieldEntity {
    public int destructValue;
    int pos;

    public BuilderWall(){
        this.destructValue = 100;
    }

    public BuilderWall(int destructValue, int pos){
        this.destructValue = destructValue;
        this.pos = pos;
    }

    public void takeDamage(int othersArmor) {
        this.destructValue = this.destructValue - othersArmor;
    }
    public void takeDamagefromSoldier(int othersArmor) {
        int damage = (int) Math.ceil(othersArmor * .4);
        this.destructValue = this.destructValue - damage;
    }

    @Override
    public FieldEntity copy() {
        return new BuilderWall();
    }

    @Override
    public int getIntValue() {
        return 14;
    }


    @Override
    public String toString() {
        return "BuilderWall";
    }

    public int getPos(){
        return pos;
    }
}
