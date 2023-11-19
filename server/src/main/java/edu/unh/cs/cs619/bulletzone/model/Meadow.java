package edu.unh.cs.cs619.bulletzone.model;

public class Meadow extends FieldEntity{
        int pos;
        public Meadow(){
            pos = 0;
        }

        public Meadow(int pos){
            this.pos = pos;
        }

        @Override
        public int getIntValue() {
            return 0;
        }

        @Override
        public FieldEntity copy() {
            return new Meadow();
        }

        @Override
        public String toString() {
            return "M";
        }

        public int getPos(){
            return pos;
        }
    }


