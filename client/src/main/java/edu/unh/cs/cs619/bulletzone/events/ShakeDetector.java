package edu.unh.cs.cs619.bulletzone.events;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

//HELP WITH SHAKE: https://jasonmcreynolds.com/?p=388
public class ShakeDetector implements SensorEventListener {
    private static final float gravity = 1.0004F;
    private static final int waitTime = 500;

    private OnShakeListener listener;
    private long lastShakeTime;
    //Constructor
    public void setOnShakeListener(OnShakeListener listener) {
        this.listener = listener;
    }

    public interface OnShakeListener {
        public void onShake();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Nothing needs to be done here
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //NullCheck
        if (listener != null) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            //Find the g-Force of all dimensions
            float gX = x / SensorManager.GRAVITY_EARTH;
            float gY = y / SensorManager.GRAVITY_EARTH;
            float gZ = z / SensorManager.GRAVITY_EARTH;

            //Calculate the actual g-Force
            float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            //If the gforce is close to 1, there is no movement. Else, there is a shake
            if (gForce > gravity) {
                final long now = System.currentTimeMillis();
                if (lastShakeTime + waitTime > now) {
                    //Make sure the fire time is right.
                    listener.onShake();
                    return;
                }

                lastShakeTime = now;

            }
        }
    }

}