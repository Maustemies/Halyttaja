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

public class MainActivity extends AppCompatActivity implements CustomSensorManager.CustomSensorManagerInterface, AlarmManager.AlarmManagerInterface {

    private AlarmManager alarmManager;
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
        alarmManager = new AlarmManager(getApplicationContext(), this);
        customSensorManager = new CustomSensorManager(getApplicationContext(), this);

        UiUpdateAdvicePressStartToBegin();
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

    @Override
    public void OnAccidentDetected() {
        if(!alarmManager.AlarmIsOn()) alarmManager.StartAlarm();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UiOnAccidentDetected();
            }
        });
    }

    private void OnButtonStartStopClicked() {
        if(alarmManager.AlarmIsOn()) {
            customSensorManager.DisableSensorManagerListener();
            alarmManager.StopAlarm();
            UiOnAccidentDetectionTurnedOff();
            UiUpdateAdvicePressStartToBegin();
        }
        else {
            customSensorManager.EnableSensorManagerListener();
            UiOnAccidentDetectionTurnedOn();
            UiUpdateAdvicePressStopToStop();
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

    @Override
    public void OnAlarmExpired() {
        // TODO: Start accident reporting
    }

    @Override
    public void OnAlarmTick(final int secondsLeft) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UiUpdateAlarmTimeLeft(secondsLeft);
            }
        });
    }
}
