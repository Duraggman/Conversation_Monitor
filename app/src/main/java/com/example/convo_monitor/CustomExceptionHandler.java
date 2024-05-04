package com.example.convo_monitor;

import android.util.Log;

public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler defaultUEH;

    // Constructor where you can pass the default exception handler
    public CustomExceptionHandler() {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        // Log the stack trace to Logcat
        Log.e("ac", "Uncaught exception is: ", ex);

        // You can also save the report to a file or send it to a server here

        // Call the default exception handler as well (very important)
        if (defaultUEH != null) {
            defaultUEH.uncaughtException(thread, ex);
        } else {
            // Kill off the crashed app
            System.exit(2);
        }
    }
}
