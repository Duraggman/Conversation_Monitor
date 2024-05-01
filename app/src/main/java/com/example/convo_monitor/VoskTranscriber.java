package com.example.convo_monitor;

import org.vosk.Model;
import org.vosk.Recognizer;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;
import android.widget.TextView;

/**
 * This class is responsible for managing audio.
 */
public class VoskTranscriber {
    // Using 16 kHz sample rate for audio recording for compatibility with speech recognition APIs
    private static final int SAMPLE_RATE = 16000;
    // Use mono channel for microphone input
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    // Define audio format as 16-bit PCM
    private static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    // AudioRecord object for managing audio recording
    // Boolean to check if recording is currently active
    private boolean isRecording = false;
    // Buffer to hold the audio data during each read operation
    private byte[] audioBuffer;

    private static final int REQUEST_AUDIO_PERMISSION_CODE = 200;

    // Vosk variables
    private TextView textview;
    private AppUtils utils;

    // Threshold for silence detection. Adjust based on your environment.
    private static final double SILENCE_THRESHOLD = -70.0;
    private VoskProvider vosk;

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    // Constructor for AudioRecorder
    public VoskTranscriber(VoskProvider vosk, TextView textview) {
        this.vosk = vosk;
        Log.i("vt", "vosk: " + this.vosk);
        this.textview = textview;
    }

    // Start recording audio
    public void startRecording() {
        if (this.vosk.getRecorder().getState() == AudioRecord.STATE_INITIALIZED && this.vosk.getRecognizer() != null) {
            Log.i("vt", "Audio Record initialized");
            isRecording = true;
            this.vosk.getRecorder().startRecording();
            recordingLoop();
        }
        else {
            Log.e("vt", "Audio Record can't initialize!");
            Log.e("vosk", "Audio Record can't initialize!");
        }
    }

    // Stop recording audio
    public void stopRecording() {
        if (this.vosk.getRecorder() != null && this.vosk.getRecorder().getState() == AudioRecord.STATE_INITIALIZED) {
            this.vosk.getRecorder().stop();
            isRecording = false;
            Log.i("vt", "Recording stopped");
            Log.i("vosk", "Recording stopped");
        }
    }

    // Method that runs in a separate thread to read audio data from the microphone
    private void recordingLoop() {
        new Thread(() -> {
            while (isRecording) {
                int readResult = this.vosk.getRecorder().read(utils.getAudioBuffer(), 0, utils.getAudioBuffer().length);
                if (readResult > 0 && this.vosk.getRecognizer() != null) {
                    Log.i("vt", "loopCog: " + this.vosk.getRecognizer());
                    if (this.vosk.isCogInit()) {
                        if(this.vosk.getRecognizer().acceptWaveForm(utils.getAudioBuffer(), readResult)) {
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
}
