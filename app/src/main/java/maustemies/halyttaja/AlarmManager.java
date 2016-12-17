package maustemies.halyttaja;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Maustemies on 17.12.2016.
 * Alarm usage applied from http://stackoverflow.com/a/29672065
 */

public class AlarmManager extends Thread {

    private static final String LOG_TAG_ALARM_MANAGER = "AlarmManager";

    private static final int SECONDS_TO_WAIT_FOR_CANCELLATION = 60;

    public interface AlarmManagerInterface {
        void OnAlarmExpired();
        void OnAlarmTick(int secondsLeft);
    }
    AlarmManagerInterface mAlarmManagerInterface;

    private enum AlarmStatus {
        IDLE, TIMER_TO_BE_STARTED, TIMER_TO_BE_STOPPED, TIMER_RUNNING, TIMER_EXPIRED
    }
    private AlarmStatus alarmStatus;
    private int secondsToWait = SECONDS_TO_WAIT_FOR_CANCELLATION;
    public boolean AlarmIsOn() {
        Log.d(LOG_TAG_ALARM_MANAGER, "AlarmIsOn() returning: " + (alarmStatus == AlarmStatus.TIMER_RUNNING));

        return (alarmStatus == AlarmStatus.TIMER_RUNNING);
    }

    private Uri alarmUri;
    private Ringtone ringtone;
    private Context mContext;

    public AlarmManager(Context context, AlarmManagerInterface alarmManagerInterface) {
        Log.d(LOG_TAG_ALARM_MANAGER, "AlarmManager(Context, AlarmManagerInterface)");

        mContext = context;
        mAlarmManagerInterface = alarmManagerInterface;
        alarmStatus = AlarmStatus.IDLE;

        alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if(alarmUri == null) alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        ringtone = RingtoneManager.getRingtone(mContext, alarmUri);
    }

    private boolean threadRunning = false;
    /**
     * Custom method to start the thread. Calls the super.start() but sets an internal flag which is used to stop the thread.
     */
    public void Start() {
        Log.d(LOG_TAG_ALARM_MANAGER, "Start()");

        if(threadRunning) return;
        threadRunning = true;
        super.start();
    }
    /**
     * Changes the internal flag which is used to stop the thread.
     */
    public void Stop() {
        Log.d(LOG_TAG_ALARM_MANAGER, "Stop()");

        threadRunning = false;
    }

    @Override
    public void run() {
        Log.d(LOG_TAG_ALARM_MANAGER, "run()");

        while(threadRunning) {
            switch (alarmStatus) {
                case IDLE:
                {
                    try {
                        sleep(500);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        Log.w("AlarmManager", "Error in AlarmManager thread: " + e.toString());
                    }
                    break;
                }
                case TIMER_TO_BE_STARTED:
                {
                    ringtone.play();
                    alarmStatus = AlarmStatus.TIMER_RUNNING;
                    break;
                }
                case TIMER_TO_BE_STOPPED:
                {
                    ringtone.stop();
                    alarmStatus = AlarmStatus.IDLE;
                    break;
                }
                case TIMER_RUNNING:
                {
                    try {
                        sleep(1000);
                        secondsToWait--;
                        if(secondsToWait <= 0) {
                            secondsToWait = SECONDS_TO_WAIT_FOR_CANCELLATION;
                            alarmStatus = AlarmStatus.TIMER_EXPIRED;
                        }
                        else {
                            mAlarmManagerInterface.OnAlarmTick(secondsToWait);
                        }
                    }
                    catch (Exception e)  {
                        e.printStackTrace();
                        Log.w("AlarmManager", "Error in AlarmManager thread: " + e.toString());
                    }
                    break;
                }
                case TIMER_EXPIRED:
                {
                    ringtone.stop();
                    alarmStatus = AlarmStatus.IDLE;
                    mAlarmManagerInterface.OnAlarmExpired();
                    break;
                }
                default: break;
            }
        }
    }

    public void StartAlarm() {
        Log.d(LOG_TAG_ALARM_MANAGER, "StartAlarm()");

        alarmStatus = AlarmStatus.TIMER_TO_BE_STARTED;
    }

    public void StopAlarm() {
        Log.d(LOG_TAG_ALARM_MANAGER, "StopAlarm()");

        alarmStatus = AlarmStatus.TIMER_TO_BE_STOPPED;
    }
}
