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

    public static byte opposite(Direction direction) {
        switch (direction) {
            case Down:
                return 0;
            case Left:
                return 2;
            case Right:
                return 6;
            case Up:
                return 4;
            default:
                return -1;
        }
    }
}
