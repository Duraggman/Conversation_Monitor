package com.example.convo_monitor;

import static com.example.convo_monitor.AppUtils.isRecording;

import android.media.AudioRecord;
import android.util.Log;
import android.widget.TextView;


public class VoskTranscriber {
    // Threshold for silence detection. Adjust based on your environment.
    //private static final double SILENCE_THRESHOLD = -70.0;
    private final VoskProvider vosk;

    // Constructor for Transcriber class
    public VoskTranscriber(VoskProvider vosk) {
        this.vosk = vosk;

    }


    // Start recording audio
    public void startRecording(TextView textview) {
            if (this.vosk.getRecorder().getState() == AudioRecord.STATE_INITIALIZED && this.vosk.getRecognizer() != null) {
                isRecording = true;
                    this.vosk.getRecorder().startRecording();
                    Log.i("vt", "Started recording");
                    recordingLoop(textview);
                } else if (this.vosk.getRecorder().getState() != AudioRecord.STATE_INITIALIZED) {
                    Log.e("vt", "Recorder is not initialize!");
                    Log.e("vosk", "Recorder is not initialize!");
                } else if (this.vosk.getRecognizer() == null) {
                    Log.e("vt", "Recognizer is not initialize!");
                    Log.e("vosk", "Recognizer is not initialize!");
                } else {
                    Log.e("vt", "Recorder is not initialize!");
                }
    }

    // Stop recording audio
    public void stopRecording() {
            if (this.vosk.getRecorder() != null && this.vosk.getRecorder().getState() == AudioRecord.STATE_INITIALIZED) {
                isRecording = false;
                    this.vosk.getRecorder().stop();
                    Log.i("vt", "Recording stopped");
                    Log.i("vosk", "Recording stopped");
            }

    }

    // Method that runs in a separate thread to read audio data from the microphone
    private void recordingLoop(TextView textview) {
        new Thread(() -> {
            while (isRecording) {
                int readResult = this.vosk.getRecorder().read(AppUtils.AUDIO_BUFFER, 0, AppUtils.BUFFER_SIZE);
                if (readResult > 0 && this.vosk.getRecognizer() != null) {
                    if (this.vosk.isCogInit()) {
                        if (this.vosk.getRecognizer().acceptWaveForm(AppUtils.AUDIO_BUFFER, readResult)) {
                            // Using log to see both result types and compare them.
                            Log.i("vt", "partial Result - " + this.vosk.getUtils().jsonSpkToString(this.vosk.getRecognizer().getPartialResult(), true));
                            String result = this.vosk.getUtils().jsonSpkToString(this.vosk.getRecognizer().getPartialResult(), true);

                            textview.post(() -> textview.setText(result));
                        }
                    }
                }
            }

            if (this.vosk.getRecognizer() != null) {
                String finalResult = this.vosk.getUtils().jsonSpkToString(this.vosk.getRecognizer().getFinalResult(), false);
                Log.i("vt", "Final Result - " + finalResult);
                Log.i("vosk", "Final Result - " + finalResult);
                textview.post(() -> textview.setText(finalResult));
            }
        }).start();
    }
}