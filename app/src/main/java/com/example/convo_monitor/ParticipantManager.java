package com.example.convo_monitor;
import static com.example.convo_monitor.AppUtils.AUDIO_BUFFER;
import static com.example.convo_monitor.AppUtils.BUFFER_SIZE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioRecord;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import org.vosk.Recognizer;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/** @noinspection CanBeFinal, FieldMayBeFinal */
@SuppressLint("SetTextI18n")
public class ParticipantManager {
    Thread cogThread; // Thread for the audio processing loop
    public int pCount;
    public int iPC;
    private Recognizer recognizer;
    private AudioRecord recorder;
    private UiController ui;
    public int newPCount;
    public Context context;
    // Participant record;
    public record Participant( String tagText, double[] refVector) {
        public String tagText() {
            return tagText;
        }
        public double[] refVector() {
            return refVector;
        }
    }
    // Participants array
    public Participant[] participants = new Participant[4];


    // Constructor
    public ParticipantManager(int initialPCount, VoskProvider vosk, UiController ui) {
        this.iPC = initialPCount;
        this.ui = ui;
        this.pCount = 0;
        this.newPCount = 0;
        this.recognizer = vosk.getRecognizer();
        this.recorder = vosk.getRecorder();
        context = vosk.context;
        vosk.getUtils().setPM(this);
    }


    public void getSpkVector(ResultCallback callback) {
            final long RECORDING_DURATION_MS = 5000; // 5 seconds
            final long UPDATE_INTERVAL_MS = 1000;    // Update every second
            final Button recordingButton = this.ui.getRecordIdButton(); // Assumed to get the button

            recognizer.reset(); // Reset the recognizer before starting a new recording

            recorder.startRecording();

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
            cogThread = new Thread(() -> {
                Log.d("pm", "Starting audio processing");
                while (!cogThread.isInterrupted()) {
                    int readResult = recorder.read(AUDIO_BUFFER, 0, BUFFER_SIZE);
                    if (readResult > 0) {
                        recognizer.acceptWaveForm(AUDIO_BUFFER, readResult);
                    }
                }
                String finalResult = recognizer.getFinalResult();
                handler.post(() -> callback.onResultAvailable(finalResult));
            });
            cogThread.start();
    }

    public void recordParticipantId(){
            getSpkVector(result -> {
                // After recording is complete, add the participant to the participants array
                double[] refVec = extractIdVector(result);
                if(refVec.length == 0){
                    Toast.makeText( this.context, "Reference vector not found, try again", Toast.LENGTH_SHORT).show();
                    this.ui.addParticipant();
                    return;
                }
                preConvoAddParticipant(this.ui.getTagInputView().getText().toString(), extractIdVector(result));
                Log.i("pm", "Current pCount: " + this.pCount);
            });
    }

    private void preConvoAddParticipant(String TagText, double[] refVector) {
        if (AppUtils.isFirstTime) { //checks if it's the first time
            Log.i("pm", "First Time");
            if (this.pCount < this.iPC) {      //checks that pCount isn't higher than initialPCount
                this.participants[pCount] = new Participant(TagText, refVector);
                this.pCount++;
            }
            if (this.pCount == this.iPC) {
                Log.i("pm", "Participant 1 ID: " + TagText + " Vector: " + Arrays.toString(refVector));
                this.ui.mainConvoUi();
                AppUtils.isFirstTime = false;
            } else {
                this.ui.addNextParticipant();
                Log.i("pm", "Participant " + (this.pCount) + " ID: " + TagText + " Vector: " + Arrays.toString(refVector));
            }
        }
        else {
            Log.i("pm", "Not First Time");
            if (this.pCount < (this.iPC+this.newPCount) ) {
                this.participants[this.pCount] = new Participant(TagText, refVector);
                this.pCount++;
            }

            if (this.pCount == this.iPC+newPCount) {
                Log.i("pm", "Participant 1 ID: " + TagText + " Vector: " + Arrays.toString(refVector));
                this.ui.mainConvoUi();
            } else {
                this.ui.addNextParticipant();
                Log.i("pm", "Participant " + (this.pCount) + " ID: " + TagText + " Vector: " + Arrays.toString(refVector));
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