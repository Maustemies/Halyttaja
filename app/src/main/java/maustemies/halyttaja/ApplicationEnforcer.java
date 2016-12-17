package maustemies.halyttaja;

import android.content.Intent;
import android.util.Log;

/**
 * Created by Maustemies on 17.12.2016.
 * Modified code of http://stackoverflow.com/a/19249505
 */

public class ApplicationEnforcer extends Thread {

    private static final String LOG_TAG_APPLICATION_ENFORCER = "ApplicationEnforcer";

    public ApplicationEnforcer() {
        super("ApplicationEnforcerThread");
    }

    @Override
    public void run() {
        Log.d(LOG_TAG_APPLICATION_ENFORCER, "Started thread and building the intent");

        Intent intent = new Intent(MainActivity.getContext(), MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MainActivity.getContext().getApplicationContext().startActivity(intent);
    }
}
