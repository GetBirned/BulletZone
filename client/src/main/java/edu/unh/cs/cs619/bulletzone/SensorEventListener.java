package edu.unh.cs.cs619.bulletzone;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

public interface SensorEventListener {

    public void onSensorChanged(SensorEvent event);
}
