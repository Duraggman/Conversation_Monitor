package com.example.convo_monitor;

//Vosk imports
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.StorageService;

import java.io.IOException;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * This class is responsible for managing audio recording.
 */

public class AudioRecorder{
    // Using 16 kHz sample rate for audio recording for compatibility with speech recognition APIs
    private static final int SAMPLE_RATE = 16000;
    // Use mono channel for microphone input
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    // Define audio format as 16-bit PCM
    private static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    // AudioRecord object for managing audio recording
    private AudioRecord recorder;
    // Boolean to check if recording is currently active
    private boolean isRecording = false;
    // Buffer to hold the audio data during each read operation
    private byte[] audioBuffer;

    private static final int REQUEST_AUDIO_PERMISSION_CODE = 200;

    // Vosk variables
    private Recognizer recognizer;
    private Model model;
    private Context context; // Context is necessary for file and permission management

    // Constructor for AudioRecorder
    public AudioRecorder(Context context) {
        this.context = context; // Set the context for the AudioRecorder
    }
    public void initRecorder() {
        // Calculate the minimum required buffer size for the specified audio settings
        int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, FORMAT);
        // Initialize the buffer array to the minimum buffer size
        audioBuffer = new byte[minBufferSize];

        // Check if the required permissions are granted
        if (ContextCompat.checkSelfPermission(this.context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) this.context, new String[] {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
        }
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL, FORMAT, minBufferSize);
        // setup Vosk
        setupVosk();
    }
    private void setupVosk() {
        // Load the Vosk model from the assets folder using the StorageService class from Vosk
        StorageService.unpack(this.context, "model-en-us", "model", (model) -> {
                    // Model is successfully unpacked and ready to be used
                    this.model = model;
                    this.recognizer = new Recognizer(model, SAMPLE_RATE);
                }, (exception) -> {
                    // Handle exceptions or failures
                    Log.e("VoskAPI", "Failed to unpack the model", exception);
                });
    }

    // Start recording audio
    public void startRecording() {
        if (recorder.getState() == AudioRecord.STATE_INITIALIZED && recognizer != null) {
            recorder.startRecording();
            isRecording = true;
            recordingLoop();
        }
    }

    // Stop recording audio
    public void stopRecording() {
        if (recorder != null && recorder.getState() == AudioRecord.STATE_INITIALIZED) {
            isRecording = false;
            recorder.stop();
            if (recognizer != null) {
                recognizer.close(); // Free up resources
            }
        }
    }

    // Method that runs in a separate thread to read audio data from the microphone
    private void recordingLoop() {
        // Start a new thread to handle the audio recording and processing
        new Thread(() -> {
            // Continue recording and processing as long as the isRecording flag is true
            while (isRecording) {
                // Read audio data from the microphone into the audioBuffer
                int readResult = recorder.read(audioBuffer, 0, audioBuffer.length);

                // Check if the data was successfully read and if the recognizer is initialized
                if (readResult > 0 && recognizer != null) {
                    // Feed the audio data to the recognizer and check if a complete utterance is recognized
                    if (recognizer.acceptWaveForm(audioBuffer, readResult)) {
                        // Get the result from the recognizer when an utterance is recognized
                        String result = recognizer.getResult();
                        // Log the recognized partial result
                        Log.i("VoskAPI", "Partial Result - " + result);
                    }
                }
            }

            // After stopping the recording, check if the recognizer is still available
            if (!isRecording && recognizer != null) {
                // Get the final result from the recognizer
                String finalResult = recognizer.getFinalResult();
                // Log the final recognition result
                Log.i("VoskAPI", "Final Result - " + finalResult);
            }
        }).start(); // Start the thread
    }
}
