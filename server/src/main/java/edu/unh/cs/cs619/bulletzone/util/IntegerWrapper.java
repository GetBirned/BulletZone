package edu.unh.cs.cs619.bulletzone.util;

import java.io.Serializable;
public class IntegerWrapper{

    private int value;

    // Default constructor for Jackson
    public IntegerWrapper() {
    }

    public IntegerWrapper(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}