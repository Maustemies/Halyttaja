package maustemies.halyttaja;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private class AlarmManager {
        public boolean alarmOn = false;

        public void StartAlarm() {
            // TODO: Start the alarm
            alarmOn = true;
        }

        public void StopAlarm() {
            // TODO: Stop the alarm
            alarmOn = false;
        }
    }
    private AlarmManager alarmManager;

    private class CustomSensorManager extends Thread implements SensorEventListener {

        private SensorManager sensorManager;
        private Sensor sensor;

        private boolean newData = false;

        private final static int accelerationValuesArraySize = 10;
        private float[][] accelerationValues = new float[accelerationValuesArraySize][3];
        private int accelerationValuesIndex = 0;

        public CustomSensorManager() {
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }

        public void DisableSensorManagerListener() {
            sensorManager.unregisterListener(this);
        }

        public void EnableSensorManagerListener() {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }

        @Override
        public void run() {

            boolean accidentDetected = false;

            while(true) {
                if(newData) {
                    // TODO: Iterate through the accelerationValues to see if there is an accident
                    if(accidentDetected) {
                        OnAccidentDetected();
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
    private CustomSensorManager customSensorManager;

    private RelativeLayout relativeLayout;
    private Button buttonStartStop;
    private TextView textViewStatus;
    private TextView textViewAdvice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitViews();
        alarmManager = new AlarmManager();
        customSensorManager = new CustomSensorManager();
    }

    private void InitViews() {
        relativeLayout = (RelativeLayout) findViewById(R.id.activity_main);

        textViewStatus = (TextView) findViewById(R.id.textViewStatus);
        textViewAdvice = (TextView) findViewById(R.id.textViewAdvice);

        buttonStartStop = (Button) findViewById(R.id.buttonStartStop);
        buttonStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnButtonStartStopClicked();
            }
        });
    }

    private void OnAccidentDetected() {
        if(!alarmManager.alarmOn) alarmManager.StartAlarm();
        UiOnAccidentDetected();
    }

    private void OnButtonStartStopClicked() {
        if(alarmManager.alarmOn) {
            customSensorManager.DisableSensorManagerListener();
            UiOnAccidentDetectionTurnedOff();
        }
        else {
            customSensorManager.EnableSensorManagerListener();
            UiOnAccidentDetectionTurnedOn();
        }
    }

    private void UiOnAccidentDetected() {
        textViewStatus.setText(R.string.textAccidentDetected);
    }

    private void UiOnAccidentReported() {
        textViewStatus.setText(R.string.textAccidentReported);
    }

    private void UiOnAccidentDetectionTurnedOn() {
        textViewStatus.setText(R.string.textAccidentDetectionOn);
        buttonStartStop.setText(R.string.buttonStop);
    }

    private void UiOnAccidentDetectionTurnedOff() {
        textViewStatus.setText(R.string.textAccidentDetectionOff);
        buttonStartStop.setText(R.string.buttonStart);
    }

    private void UiUpdateAlarmTimeLeft(int seconds) {
        String timeLeftString = getString(R.string.textAlarmTimeLeft, seconds);
        textViewStatus.setText(timeLeftString);
    }

    private void UiUpdateAdvicePressStartToBegin() {
        textViewAdvice.setText(R.string.textAdvicePressStartToBeginDetection);
    }

    private void UiUpdateAdvicePressStopToStop() {
        textViewAdvice.setText(R.string.textAdvicePressStopToStopDetection);
    }

    @Override
    protected void onResume() {
        if(customSensorManager == null) return;

        customSensorManager.EnableSensorManagerListener();
    }

    @Override
    protected void onPause() {
        if(customSensorManager == null) return;

        customSensorManager.DisableSensorManagerListener();
    }
}
