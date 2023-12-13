package edu.unh.cs.cs619.bulletzone.model;

import java.awt.dnd.DragGestureEvent;

public enum Direction {
    Up, Down, Left, Right;

    public static Direction fromByte(byte directionByte) {
        Direction direction = null;

        switch (directionByte) {
            case 0:
                direction = Up;
                break;
            case 2:
                direction = Right;

                break;
            case 4:
                direction = Down;

                break;
            case 6:
                direction = Left;

                break;

            default:
                // TODO Log unknown direction
                break;
        }

        return direction;
    }

    public static byte toByte(Direction direction) {

        switch (direction) {
            case Down:
                return 4;
            case Left:
                return 6;
            case Right:
                return 2;
            case Up:
                return 0;
            default:
                return -1;
        }
    }

    public static byte opposite(byte direction) {
        switch (direction) {
            case 4:
                return 0;
            case 6:
                return 2;
            case 2:
                return 6;
            case 0:
                return 4;
            default:
                return -1;
        }
    }

    public static Direction opposite(Direction direction) {
        Direction d = null;

        switch (direction) {
            case Up:
                d = Down;
            case Right:
                d = Left;
            case Down:
                d = Down;
            case Left:
                d = Right;
            default:
                break;
        }
        return d;
    }
}
