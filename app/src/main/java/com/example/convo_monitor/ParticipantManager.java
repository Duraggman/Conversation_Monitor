package com.example.convo_monitor;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.ArrayList;
public class ParticipantManager {
    private int pCount;
    private VoskProvider vosk;
    private UiController ui;
    // Participant record
    private record participant( String TagText, Vector refVector) {}
    // Participants array
    participant[] participants = new participant[4];

    // Constructor
    public ParticipantManager(int pCount, VoskProvider vosk, UiController ui) {
        this.pCount = pCount;
        this.vosk = vosk;
        this.ui = ui;
    }

    private void addParticipant(String TagText, Vector refVector) {
        // Add participant to the participants array
        participants[pCount] = new participant(TagText, refVector);
        pCount++;
    }



    public void recordParticipantID() {
        final long RECORDING_DURATION_MS = 20000; // 20 seconds
        final long UPDATE_INTERVAL_MS = 1000;     // Update every second
        final Button recordingButton = this.ui.getRecordIdButton(); // Assumed to get the button

        this.vosk.getRecorder().startRecording();
        recordingButton.setText("Recording ID (20)");

        Handler handler = new Handler(Looper.getMainLooper()); // Handler associated with the UI thread
        Runnable updateRunnable = new Runnable() {
            long timeLeft = RECORDING_DURATION_MS;

            @Override
            public void run() {
                timeLeft -= UPDATE_INTERVAL_MS;
                if (timeLeft > 0) {
                    // Update the button text with the time left
                    recordingButton.setText("Recording ID (" + (timeLeft / 1000) + ")");
                    // Post the next update after 1 second
                    handler.postDelayed(this, UPDATE_INTERVAL_MS);
                } else {
                    // Update for the last second as timer task may stop slightly later
                    recordingButton.setText("Recording ID (0)");
                }
            }
        };

        // Start the periodic update
        handler.postDelayed(updateRunnable, UPDATE_INTERVAL_MS);

        // Timer to stop recording after 20 seconds
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                vosk.getRecorder().stop();
                handler.removeCallbacks(updateRunnable); // Stop updates when recording stops
                recordingButton.setText("Recording Complete");
            }
        }, RECORDING_DURATION_MS);
    }

    private Vector getIdVector() {
        Vector idVector = new Vector();
        double[] s = extractIDVector();
        return idVector;
    }

    private double[] extractIDVector() {
        return AppUtils.jsonSpkVectorExtract(this.vosk.getRecognizer().getFinalResult());
    }

}
