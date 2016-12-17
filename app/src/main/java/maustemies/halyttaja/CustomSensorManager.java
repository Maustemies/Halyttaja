package maustemies.halyttaja;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by Maustemies on 17.12.2016.
 */

public class CustomSensorManager extends Thread implements SensorEventListener {

    private Context mContext;

    public interface CustomSensorManagerInterface {
        void OnAccidentDetected();
    }

    private CustomSensorManagerInterface mCustomSensorManagerInterface;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    private boolean newData = false;
    private final static int accelerationValuesArraySize = 10;
    private float[][] accelerationValues = new float[accelerationValuesArraySize][3];
    private int accelerationValuesIndex = 0;

    public CustomSensorManager(Context context, CustomSensorManagerInterface customSensorManagerInterface) {
        mContext = context;
        mCustomSensorManagerInterface = customSensorManagerInterface;
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void DisableSensorManagerListener() {
        mSensorManager.unregisterListener(this);
    }

    public void EnableSensorManagerListener() {
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void run() {

        boolean accidentDetected = false;

        while(true) {
            if(newData) {
                // TODO: Iterate through the accelerationValues to see if there is an accident
                if(accidentDetected) {
                    mCustomSensorManagerInterface.OnAccidentDetected();
                    accidentDetected = false;
                }
                newData = false;
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        /**
         * Algorithm taken from https://developer.android.com/guide/topics/sensors/sensors_motion.html
         */
        // In this example, alpha is calculated as t / (t + dT),
        // where t is the low-pass filter's time-constant and
        // dT is the event delivery rate.

        final float alpha = 0.8f;
        float[] gravity = new float[3];

        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        // Remove the gravity contribution with the high-pass filter.
        for(int i = 0; i < 3; i++) {
            accelerationValues[accelerationValuesIndex][i] = event.values[i] - gravity[i];
        }

        accelerationValuesIndex++;
        if(accelerationValuesIndex == accelerationValuesArraySize) accelerationValuesIndex = 0;
        newData = true;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
