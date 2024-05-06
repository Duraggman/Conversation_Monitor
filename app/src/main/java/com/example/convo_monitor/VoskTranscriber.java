package com.example.convo_monitor;

import android.media.AudioRecord;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Locale;

/**
 * This class is responsible for managing audio.
 */
public class VoskTranscriber {
    // Using 16 kHz sample rate for audio recording for compatibility with speech recognition APIs
    private final AppUtils utils;

    // AudioRecord object for managing audio recording
    // Boolean to check if recording is currently active
    private boolean isRecording = false;
    // Buffer to hold the audio data during each read operation

    // Vosk variables
    private final TextView textview;
    private final Button recBtm;

    // Threshold for silence detection. Adjust based on your environment.
    //private static final double SILENCE_THRESHOLD = -70.0;
    private final VoskProvider vosk;

    // Constructor for Transcriber class
    public VoskTranscriber(VoskProvider vosk, AppUtils utils, UiController ui) {
        this.vosk = vosk;
        Log.i("vt", "vosk: " + this.vosk);
        this.textview = ui.getTransTextView();
        this.utils = utils;
        this.recBtm = ui.getRecButton();
    }

    // Start recording audio
    public void startRecording() {
        if (this.vosk.getRecorder().getState() == AudioRecord.STATE_INITIALIZED && this.vosk.getRecognizer() != null) {
            Log.i("vt", "Audio Record initialized");
            isRecording = true;
            recBtm.setText(R.string.stopRecording);
            this.vosk.getRecorder().startRecording();
            boolean rl = false;
            if (!rl) {
                recordingLoopJ();
            } else {
                recordingLoop();
            }
        } else {
            Log.e("vt", "Audio Record can't initialize!");
            Log.e("vosk", "Audio Record can't initialize!");
        }
    }

    // Stop recording audio
    public void stopRecording() {
        if (this.vosk.getRecorder() != null && this.vosk.getRecorder().getState() == AudioRecord.STATE_INITIALIZED) {
            isRecording = false;
            recBtm.setText(R.string.startRecording);
            this.vosk.getRecorder().stop();
            Log.i("vt", "Recording stopped");
            Log.i("vosk", "Recording stopped");
        }
    }

    // Method that runs in a separate thread to read audio data from the microphone
    private void recordingLoop() {
        new Thread(() -> {
            while (isRecording) {
                int readResult = this.vosk.getRecorder().read(utils.getAudioBuffer(), 0, utils.getBufferSize());
                if (readResult > 0 && this.vosk.getRecognizer() != null) {
                    if (this.vosk.isCogInit()) {
                        if (this.vosk.getRecognizer().acceptWaveForm(utils.getAudioBuffer(), readResult)) {
                            String result = this.vosk.getRecognizer().getResult();
                            Log.i("vt", "RT result: - " + result);
                            textview.post(() -> textview.setText(result));
                        }
                    }
                }
            }
            if (this.vosk.getRecognizer() != null) {
                String finalResult = this.vosk.getRecognizer().getFinalResult();
                Log.i("vt", "Final Result - " + finalResult);
                Log.i("vosk", "Final Result - " + finalResult);
            }
        }).start();
    }

    private void recordingLoopJ() {
        new Thread(() -> {
            while (isRecording) {
                int readResult = this.vosk.getRecorder().read(utils.getAudioBuffer(), 0, utils.getBufferSize());
                if (readResult > 0 && this.vosk.getRecognizer() != null) {
                    if (this.vosk.isCogInit()) {
                        if (this.vosk.getRecognizer().acceptWaveForm(utils.getAudioBuffer(), readResult)) {
                            String result = AppUtils.jsonPartialToString(this.vosk.getRecognizer().getPartialResult());
                            textview.post(() -> textview.setText(result));
                        }
                    }
                }
            }
            if (this.vosk.getRecognizer() != null) {
                String finalResult = this.vosk.getRecognizer().getFinalResult();
                Log.i("vt", "Final Result - " + finalResult);
                Log.i("vosk", "Final Result - " + finalResult);
            }
        }).start();
    }


}