package maustemies.halyttaja;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Debug;
import android.util.Log;

/**
 * Created by Maustemies on 17.12.2016.
 */

public class CustomSensorManager extends Thread implements SensorEventListener {

    private static final String LOG_TAG_CUSTOM_SENSOR_MANAGER = "CustomSensorManager";

    private Context mContext;

    public interface CustomSensorManagerInterface {
        void OnAccidentDetected();
        void OnAccidentDetectionStarted();
        void OnAccidentDetectionStopped();
    }

    private CustomSensorManagerInterface mCustomSensorManagerInterface;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    private boolean newData = false;
    private boolean accidentDetected = false;
    private final static int accelerationValuesArraySize = 5;
    private float[][] accelerationValues = new float[accelerationValuesArraySize][3];
    private int accelerationValuesIndex = 0;
    private static final int ACCELERATION_VALUES_X_INDEX = 0;
    private static final int ACCELERATION_VALUES_Y_INDEX = 1;
    private static final int ACCELERATION_VALUES_Z_INDEX = 1;

    private static final float TRIGGER_VALUE_ACCELERATION_X = 18.0f;
    private static final float TRIGGER_VALUE_ACCELERATION_Y = 18.0f;
    private static final float TRIGGER_VALUE_ACCELERATION_Z = 18.0f;

    public CustomSensorManager(Context context, CustomSensorManagerInterface customSensorManagerInterface) {
        Log.d(LOG_TAG_CUSTOM_SENSOR_MANAGER, "CustomSensorManager(Context, CustomSensorManagerInterface)");

        mContext = context;
        mCustomSensorManagerInterface = customSensorManagerInterface;
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);

        super.start();
    }

    public void StopAccidentDetection() {
        Log.d(LOG_TAG_CUSTOM_SENSOR_MANAGER, "StopAccidentDetection()");

        Stop();
        mSensorManager.unregisterListener(this);
        mCustomSensorManagerInterface.OnAccidentDetectionStopped();
    }

    public void StartAccidentDetection() {
        Log.d(LOG_TAG_CUSTOM_SENSOR_MANAGER, "StartAccidentDetection()");

        Start();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
        mCustomSensorManagerInterface.OnAccidentDetectionStarted();
    }

    private boolean functionalityOn = false;
    /**
     * Custom method to start the thread. Calls the super.start() but sets an internal flag which is used to stop the thread.
     */
    private void Start() {
        Log.d(LOG_TAG_CUSTOM_SENSOR_MANAGER, "Start()");

        if(functionalityOn) return;
        functionalityOn = true;
    }
    /**
     * Changes the internal flag which is used to stop the thread.
     */
    private void Stop() {
        Log.d(LOG_TAG_CUSTOM_SENSOR_MANAGER, "Stop()");

        functionalityOn = false;
    }

    @Override
    public void run() {
        Log.d(LOG_TAG_CUSTOM_SENSOR_MANAGER, "run()");

        while(true) {
            if(newData && functionalityOn) {
                // Iterate through the accelerationValues to see if there is an accident
                // TODO: Think of/implement the real, applicable, algorithm
                float changesX = 0.0f;
                float changesY = 0.0f;
                float changesZ = 0.0f;

                for(int i = 0; i < accelerationValuesArraySize; i++) {
                    changesX += Math.abs(accelerationValues[i][ACCELERATION_VALUES_X_INDEX]);
                    changesY += Math.abs(accelerationValues[i][ACCELERATION_VALUES_Y_INDEX]);
                    changesZ += Math.abs(accelerationValues[i][ACCELERATION_VALUES_Z_INDEX]);
                }

                changesX = changesX/accelerationValuesArraySize;
                changesY = changesY/accelerationValuesArraySize;
                changesZ = changesZ/accelerationValuesArraySize;

                if( (changesX >= TRIGGER_VALUE_ACCELERATION_X) || (changesY >= TRIGGER_VALUE_ACCELERATION_Y) || (changesZ >= TRIGGER_VALUE_ACCELERATION_Z))
                    accidentDetected = true;

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
        //Log.d(LOG_TAG_CUSTOM_SENSOR_MANAGER, "onSensorChanged(SensorEvent) with new values: " + event.values[0] + ", " + event.values[1] + ", " + event.values[2]);

        for(int i = 0; i < 3; i++) {
            accelerationValues[accelerationValuesIndex][i] = event.values[i];
        }

        accelerationValuesIndex++;
        if(accelerationValuesIndex == accelerationValuesArraySize) accelerationValuesIndex = 0;
        newData = true;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(LOG_TAG_CUSTOM_SENSOR_MANAGER, "onAccuracyChanged(Sensor, int)");

    }
}
