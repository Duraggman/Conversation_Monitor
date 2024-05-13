package com.example.convo_monitor;

import static com.example.convo_monitor.AppUtils.isRecording;
import static com.example.convo_monitor.AppUtils.setDuration;

import android.media.AudioRecord;
import android.util.Log;
import android.widget.TextView;

public class VoskTranscriber {
    private final VoskProvider vosk;
    public StringBuilder conversationLog;

    // Constructor for Transcriber class
    public VoskTranscriber(VoskProvider vosk) {
        this.vosk = vosk;
        this.conversationLog = new StringBuilder();
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
        AppUtils.spkVolume = 0;
        AppUtils.currentSpeaker = "";
        new Thread(() -> {
            double startTime = System.currentTimeMillis();
            while (isRecording) {
                double loopStart = System.currentTimeMillis();
                int readResult = this.vosk.getRecorder().read(AppUtils.AUDIO_BUFFER, 0, AppUtils.BUFFER_SIZE); // Read audio data
                if (readResult > 0 && this.vosk.getRecognizer() != null) {
                    if (this.vosk.isCogInit()) {
                        if (this.vosk.getRecognizer().acceptWaveForm(AppUtils.AUDIO_BUFFER, readResult)) {
                            // Using log to see both result types and compare them.
                            Log.i("vt", "partial Result - " + this.vosk.getUtils().jsonSpkToString(this.vosk.getRecognizer().getPartialResult(), true, false));
                            double loopDuration= System.currentTimeMillis() - loopStart;
                            setDuration(loopDuration);
                            // Measure volume of the audio data
                            AppUtils.spkVolume = AppUtils.measureVolume(AppUtils.AUDIO_BUFFER);
                            String result = this.vosk.getUtils().jsonSpkToString(this.vosk.getRecognizer().getResult(), false, false);
                            Log.i("vt", "volume - " + AppUtils.spkVolume);
                            conversationLog.append(result);
                            conversationLog.append("\n");
                            textview.post(() -> textview.setText(result));
                        }
                    }
                }
            }

            double totalDuration = System.currentTimeMillis() - startTime;
            Log.i("vt", "Total Duration - " + totalDuration);
            if (this.vosk.getRecognizer() != null) {
                String finalResult = this.vosk.getUtils().jsonSpkToString(this.vosk.getRecognizer().getFinalResult(), false, true);
                if (!finalResult.equals("empty")) {
                    conversationLog.append(finalResult);
                    conversationLog.append("\n");
                    Log.i("vt", "Final Result - " + finalResult);
                }
                else {
                    Log.i("vt", "Final Result - empty");
                }
                textview.post(() -> textview.setText(conversationLog.toString())); // shows the entire conversation
            }
        }).start();
    }
}