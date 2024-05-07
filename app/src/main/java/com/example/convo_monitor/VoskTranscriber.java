package com.example.convo_monitor;

import android.media.AudioRecord;
import android.util.Log;
import android.widget.TextView;

/**
 * This class is responsible for managing audio.
 */
public class VoskTranscriber {
    private final AppUtils utils;
    // Threshold for silence detection. Adjust based on your environment.
    //private static final double SILENCE_THRESHOLD = -70.0;
    private final VoskProvider vosk;

    // Constructor for Transcriber class
    public VoskTranscriber(VoskProvider vosk, AppUtils utils) {
        this.vosk = vosk;
        this.utils = utils;
    }


    // Start recording audio
    public void startRecording(TextView textview) {
        if (this.vosk.getRecorder().getState() == AudioRecord.STATE_INITIALIZED && this.vosk.getRecognizer() != null) {
            AppUtils.isRecording = true;
            this.vosk.getRecorder().startRecording();
            Log.i("vt", "Started recording");
            boolean rl = false;
            if (!rl) {
                recordingLoopJ(textview);
            } else {
                recordingLoop(textview);
            }
        }
        else if (this.vosk.getRecorder().getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e("vt", "Recorder is not initialize!");
            Log.e("vosk", "Recorder is not initialize!");
        }
        else if (this.vosk.getRecognizer() == null) {
            Log.e("vt", "Recognizer is not initialize!");
            Log.e("vosk", "Recognizer is not initialize!");
        }
        else {
            Log.e("vt", "Recorder is not initialize!");
            Log.e("vosk", "Recorder is not initialize!");
        }
    }

    // Stop recording audio
    public void stopRecording() {
        if (this.vosk.getRecorder() != null && this.vosk.getRecorder().getState() == AudioRecord.STATE_INITIALIZED) {
            AppUtils.isRecording = false;
            this.vosk.getRecorder().stop();
            Log.i("vt", "Recording stopped");
            Log.i("vosk", "Recording stopped");
        }
    }

    // Method that runs in a separate thread to read audio data from the microphone
    private void recordingLoop(TextView textview) {
        new Thread(() -> {
            while (AppUtils.isRecording) {
                int readResult = this.vosk.getRecorder().read(utils.getAudioBuffer(), 0, utils.getBufferSize());
                if (readResult > 0 && this.vosk.getRecognizer() != null) {
                    if (this.vosk.isCogInit()) {
                        if (this.vosk.getRecognizer().acceptWaveForm(utils.getAudioBuffer(), readResult)) {
                            String result = this.vosk.getRecognizer().getResult();
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

    private void recordingLoopJ(TextView textview) {
        new Thread(() -> {
            while (AppUtils.isRecording) {
                int readResult = this.vosk.getRecorder().read(utils.getAudioBuffer(), 0, utils.getBufferSize());
                if (readResult > 0 && this.vosk.getRecognizer() != null) {
                    if (this.vosk.isCogInit()) {
                        if (this.vosk.getRecognizer().acceptWaveForm(utils.getAudioBuffer(), readResult)) {
                            String result = AppUtils.jsonSpkToString(this.vosk.getRecognizer().getResult());
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