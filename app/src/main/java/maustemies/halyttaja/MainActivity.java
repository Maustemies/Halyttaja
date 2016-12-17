package maustemies.halyttaja;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements CustomSensorManager.CustomSensorManagerInterface, AlarmManager.AlarmManagerInterface, GenericTimer.GenericTimerInterface {

    private static final String LOG_TAG_MAIN_ACTIVITY = "MainActivity";

    private boolean userInitiatedDetection = false;

    private Button buttonStartStop;
    private TextView textViewStatus;
    private TextView textViewAdvice;
    private RelativeLayout relativeLayoutBackground;

    private static final int GENERIC_TIMER_CODE_BACKGROUND_BLINKER = 10;
    private boolean backgroundBlinkingOn = false;
    private boolean showNormalColor = false;

    private GenericTimer backgroundBlinker;
    private AlarmManager alarmManager;
    private CustomSensorManager customSensorManager;

    private static MainActivity mInstance = null;
    public static Context getContext() {
        return mInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG_MAIN_ACTIVITY, "onCreate(Bundle)");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitViews();

        alarmManager = new AlarmManager(getApplicationContext(), this);
        customSensorManager = new CustomSensorManager(getApplicationContext(), this);
        backgroundBlinker = new GenericTimer(GENERIC_TIMER_CODE_BACKGROUND_BLINKER, 500, this);

        UiUpdateAdvicePressStartToBegin();

        mInstance = this;
    }

    private void InitViews() {
        Log.d(LOG_TAG_MAIN_ACTIVITY, "InitViews()");

        relativeLayoutBackground = (RelativeLayout) findViewById(R.id.relativeLayoutBackground);
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
        Log.d(LOG_TAG_MAIN_ACTIVITY, "OnAccidentDetected()");

        if (alarmManager != null && !alarmManager.AlarmIsOn()) alarmManager.StartAlarm();
        if (backgroundBlinker != null) {
            showNormalColor = false;
            backgroundBlinkingOn = true;
            backgroundBlinker.Start();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UiOnAccidentDetected();
            }
        });
    }

    @Override
    public void OnAccidentDetectionStarted() {
        Log.d(LOG_TAG_MAIN_ACTIVITY, "OnAccidentDetectionStarted()");

        UiOnAccidentDetectionTurnedOn();
        UiUpdateAdvicePressStopToStop();

        userInitiatedDetection = true;
    }

    @Override
    public void OnAccidentDetectionStopped() {
        Log.d(LOG_TAG_MAIN_ACTIVITY, "OnAccidentDetectionStopped()");

        userInitiatedDetection = false;

        if(alarmManager != null) alarmManager.StopAlarm();
        if(backgroundBlinker != null) backgroundBlinker.Stop();
        showNormalColor = true;

        UiOnAccidentDetectionTurnedOff();
        UiUpdateAdvicePressStartToBegin();
    }

    private void OnButtonStartStopClicked() {
        Log.d(LOG_TAG_MAIN_ACTIVITY, "OnButtonStartStopClicked()");

        if(userInitiatedDetection) {
            if(customSensorManager != null) customSensorManager.StopAccidentDetection();
            else OnAccidentDetectionStopped();
        }
        else {
            if(customSensorManager != null) customSensorManager.StartAccidentDetection();
            else OnAccidentDetectionStarted();
        }
    }

    private void UiOnAccidentDetected() {
        Log.d(LOG_TAG_MAIN_ACTIVITY, "UiOnAccidentDetected()");

        // Start a thread that brings the app to the foreground so that the user won't miss the alarm going off
        new ApplicationEnforcer().start();

        textViewStatus.setText(R.string.textAccidentDetected);
        textViewAdvice.setText(R.string.textAdvicePressStopToStopAlarm);
    }

    private void UiOnAccidentReported() {
        Log.d(LOG_TAG_MAIN_ACTIVITY, "UiOnAccidentReported()");

        textViewStatus.setText(R.string.textAccidentReported);
    }

    private void UiOnAccidentDetectionTurnedOn() {
        Log.d(LOG_TAG_MAIN_ACTIVITY, "UiOnAccidentDetectionTurnedOn()");

        textViewStatus.setText(R.string.textAccidentDetectionOn);
        buttonStartStop.setBackgroundResource(R.drawable.button_stop_texture);
    }

    private void UiOnAccidentDetectionTurnedOff() {
        Log.d(LOG_TAG_MAIN_ACTIVITY, "UiOnAccidentDetectionTurnedOff()");

        textViewStatus.setText(R.string.textAccidentDetectionOff);
        buttonStartStop.setBackgroundResource(R.drawable.button_start_texture);
    }

    private void UiUpdateAlarmTimeLeft(int seconds) {
        Log.d(LOG_TAG_MAIN_ACTIVITY, "UiUpdateAlarmTimeLeft(int) with seconds = " + seconds);

        String timeLeftString = getString(R.string.textAlarmTimeLeft, seconds);
        textViewStatus.setText(timeLeftString);
    }

    private void UiOnAlarmExpired() {
        Log.d(LOG_TAG_MAIN_ACTIVITY, "UiOnAlarmExpired()");

        textViewStatus.setText(R.string.textAccidentUnderReporting);
    }

    private void UiUpdateAdvicePressStartToBegin() {
        Log.d(LOG_TAG_MAIN_ACTIVITY, "UiUpdateAdvicePressStartToBegin()");

        textViewAdvice.setText(R.string.textAdvicePressStartToBeginDetection);
    }

    private void UiUpdateAdvicePressStopToStop() {
        Log.d(LOG_TAG_MAIN_ACTIVITY, "UiUpdateAdvicePressStopToStop()");

        textViewAdvice.setText(R.string.textAdvicePressStopToStopDetection);
    }

    @Override
    public void OnAlarmExpired() {
        Log.d(LOG_TAG_MAIN_ACTIVITY, "OnAlarmExpired()");

        // TODO: Start accident reporting
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UiOnAlarmExpired();
            }
        });
    }

    @Override
    public void OnAlarmTick(final int secondsLeft) {
        Log.d(LOG_TAG_MAIN_ACTIVITY, "OnAlarmTick(final int) with secondsLeft = " + secondsLeft);

        if(!userInitiatedDetection) return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UiUpdateAlarmTimeLeft(secondsLeft);
            }
        });
    }

    @Override
    public void OnGenericTimerTick(int idCode) {
        Log.d(LOG_TAG_MAIN_ACTIVITY, "OnGenericTimerTick(int) with idCode = " + idCode);

        switch (idCode) {
            case GENERIC_TIMER_CODE_BACKGROUND_BLINKER:
            {
                if(!backgroundBlinkingOn) return;
                if(relativeLayoutBackground == null) return;

                final int colorCode = showNormalColor ? R.color.colorBlue : R.color.colorRed;
                showNormalColor = !showNormalColor;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        relativeLayoutBackground.setBackgroundColor(getResources().getColor(colorCode));
                    }
                });

                break;
            }
            default:
            {
                Log.w("MainActivity", "public void OnGenericTimerTick(int idCode) - Unhandled generic timer tick");
                break;
            }
        }
    }
}
