package org.akvo.akvoqr.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.akvo.akvoqr.util.App;

import java.util.List;

/**
 * Created by linda on 8/31/15.
 */
public class LightSensor implements SensorEventListener
{
    private SensorManager mSensorManager;
    private Sensor lightSensor;
    private float lux = -1;


    public LightSensor() {

        mSensorManager = (SensorManager) App.getMyApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        lightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_LIGHT);
        for(Sensor sensor: deviceSensors)
            System.out.println("*** sensor available: " + sensor.getName() + ", " + sensor.getType() + ", " + sensor.toString());
    }

    public void start()
    {
        mSensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    public void stop()
    {
        mSensorManager.unregisterListener(this);

    }

    public float getLux()
    {
       return lux;
    }
    @Override
    public void onSensorChanged(SensorEvent event) {

        System.out.println("***Light sensor changed");
        if(event.sensor.getType()==Sensor.TYPE_LIGHT) {
            lux = event.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
