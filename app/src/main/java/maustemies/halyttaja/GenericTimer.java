package maustemies.halyttaja;

import android.util.Log;

/**
 * Created by Maustemies on 17.12.2016.
 */

public class GenericTimer extends Thread {

    private int idCode;
    private final String LOG_TAG_GENERIC_TIMER = "GenericTimer("+idCode+")";

    private long delay;

    public interface GenericTimerInterface {
        void OnGenericTimerTick(int idCode);
    }
    private GenericTimerInterface mGenericTimerInterface;

    /**
     * Create a generic timer which takes an integer idCode and a long type delay in millis as parameters.
     * @param idCode
     * @param delay
     */
    public GenericTimer(int idCode, long delay, GenericTimerInterface genericTimerInterface) {
        Log.d(LOG_TAG_GENERIC_TIMER, "GenericTimer(int, long, GenericTimerInterface) with idCode = " + idCode + " and delay = " + delay);

        this.idCode = idCode;
        this.delay = delay;
        mGenericTimerInterface = genericTimerInterface;

        super.start();
    }

    private boolean threadRunning = false;
    /**
     * Custom method to start the thread. Calls the super.start() but sets an internal flag which is used to stop the thread.
     */
    public void Start() {
        Log.d(LOG_TAG_GENERIC_TIMER, "Start()");

        if(threadRunning) return;
        threadRunning = true;
    }
    /**
     * Changes the internal flag which is used to stop the thread.
     */
    public void Stop() {
        Log.d(LOG_TAG_GENERIC_TIMER, "Stop()");

        threadRunning = false;
    }

    @Override
    public void run() {
        Log.d(LOG_TAG_GENERIC_TIMER, "run()");

        while(true) {
            if (threadRunning) {
                try {
                    sleep(500);
                    mGenericTimerInterface.OnGenericTimerTick(idCode);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.w("BackgroundBlinker", "Error in BackgroundBlinker thread: " + e.toString());
                }
            }
        }
    }
}
