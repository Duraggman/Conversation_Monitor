package com.example.convo_monitor;
import static com.example.convo_monitor.AppUtils.newPCount;

import android.media.AudioRecord;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;

import org.vosk.Recognizer;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class ParticipantManager {
    Thread cogThread; // Thread for the audio processing loop
    public int pCount;
    public int iPC;
    private final Recognizer recognizer;
    private final AudioRecord recorder;
    private final UiController ui;
    private final AppUtils utils;
    // Participant record
    public record Participant( String tagText, double[] refVector) {}
    // Participants array
    public Participant[] participants = new Participant[4];


    // Constructor
    public ParticipantManager(int initialPCount, VoskProvider vosk, UiController ui, AppUtils utils) {
        this.iPC = initialPCount;
        this.ui = ui;
        pCount = 0;
        this.recognizer = vosk.getRecognizer();
        this.recorder = vosk.getRecorder();
        this.utils = utils;
    }

    public void getSpkVector( ResultCallback callback) {
        final long RECORDING_DURATION_MS = 5000; // 5 seconds
        final long UPDATE_INTERVAL_MS = 1000;    // Update every second
        final Button recordingButton = this.ui.getRecordIdButton(); // Assumed to get the button

        recognizer.reset(); // Reset the recognizer before starting a new recording

        recorder.startRecording();
        AppUtils.isRecording = true;
        recordingButton.setText("Recording ID (5)");

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

        // Timer to stop recording after 5 seconds
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // Stop the recorder
                recorder.stop();
                AppUtils.isRecording = false;

                // Run the UI updates on the main thread
                handler.post(() -> {
                    // Remove the updateRunnable callbacks to stop the periodic updates
                    handler.removeCallbacks(updateRunnable);
                    // Set the button text to "Recording Complete"
                    recordingButton.setText("Recording Complete");
                    Log.d("pm", "Recording complete");
                    // Interrupt the audio processing thread to stop processing audio
                    cogThread.interrupt();
                });
            }
        }, RECORDING_DURATION_MS);

        // Start audio processing in parallel with recording
        cogThread = new Thread (() -> {
            Log.d("pm", "Starting audio processing");
            byte[] audioBuffer = utils.getAudioBuffer();
            int bufferSize = utils.getBufferSize();
            while (!cogThread.isInterrupted()) {
                int readResult = recorder.read(audioBuffer, 0, bufferSize);
                if (readResult > 0) {
                    if (recognizer.acceptWaveForm(audioBuffer, readResult)) {
                        String result = AppUtils.jsonPartialToString(recognizer.getPartialResult());
                        Log.d("pm", "Partial Result - " + result);
                    }
                }
            }
            String finalResult = recognizer.getFinalResult();
            handler.post(() -> callback.onResultAvailable(finalResult));
            Log.i("pm", "Final Result - " + finalResult);
        }); cogThread.start();
    }

    public void recordParticipantId(){
        getSpkVector(result -> {
            // After recording is complete, add the participant to the participants array
            preConvoAddParticipant(ui.getTagInputView().getText().toString(), extractIdVector(result));
            Log.d("pm", "Current pCount: " + pCount);
        });
    }

    private void preConvoAddParticipant(String TagText, double[] refVector) {
        //checks that pCount isn't higher than initialPCount
        if (AppUtils.isFirstTime) {
            Log.d("pm", "First Time");
            if (pCount < iPC) {
                participants[pCount] = new Participant(TagText, refVector);
                pCount++;
            }
            if (pCount == iPC) {
                Log.d("pm", "Participant 1 ID: " + TagText + " Vector: " + Arrays.toString(refVector));
                this.ui.mainConvoUi();
                AppUtils.isFirstTime = false;
            } else {
                ui.addNextParticipant();
                Log.d("pm", "Participant " + (pCount) + " ID: " + TagText + " Vector: " + Arrays.toString(refVector));
            }
        }
        else {
            Log.d("pm", "Not First Time");
            if (pCount < iPC+newPCount) {
                participants[pCount] = new Participant(TagText, refVector);
                pCount++;
            }
            else if (pCount == iPC+newPCount) {
                Log.d("pm", "Participant 1 ID: " + TagText + " Vector: " + Arrays.toString(refVector));
                this.ui.mainConvoUi();
            } else {
                ui.addNextParticipant();
                Log.d("pm", "Participant " + (pCount) + " ID: " + TagText + " Vector: " + Arrays.toString(refVector));
            }
        }
    }

    private double[] extractIdVector(String result) {
        Log.d("pm", "Final Result: " + result);
        return AppUtils.jsonSpkVectorExtract(result);
    }

    public interface ResultCallback { // Callback interface
        void onResultAvailable(String result);
    }
}