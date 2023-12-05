package edu.unh.cs.cs619.bulletzone;

public class ReplaySpeed {
    int delay_default;
    int delay_amount;

    public ReplaySpeed() {
        // in ms
        delay_default = 4000;
        // in ms
        delay_amount = delay_default;
    }

    public int getSpeed() {
        return delay_amount;
    }

    public void setSpeed(int new_speed) {
        // going to have to move this over
        delay_amount = delay_default / (new_speed+1);
    }
}
