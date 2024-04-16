package com.example.convo_monitor;

//Vosk imports
import org.vosk.LibVosk;
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

public class AudioRecorder extends Activity {
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
    private Context context;

    // Constructor for AudioRecorder
    public AudioRecorder(Context context) {
        this.context = context; // Set the context for the AudioRecorder

        // Calculate the minimum required buffer size for the specified audio settings
        int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, FORMAT);
        // Initialize the buffer array to the minimum buffer size
        audioBuffer = new byte[minBufferSize];

        // Request audio recording permissions if not already granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
            // Initialize the AudioRecord object
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL, FORMAT, minBufferSize);
            setupVosk();
        }
        else {
            // Error handling for permission not granted
            Log.e("AudioRecorder", "Permission not granted for audio recording");
        }
    }

    private void setupVosk() {
        // Load the Vosk model from the assets folder using the StorageService class from Vosk
        // Check and unpack the model from assets to a usable directory
        StorageService.unpack(context, "model-en-us/vosk-model-small-en-us-0.15.zip", "model", (model) -> {
                    // Model is successfully unpacked and ready to be used
                    this.model = model;
                    recognizer = new Recognizer(model, SAMPLE_RATE);
                }, (exception) -> {
                    // Handle exceptions or failures
                    Log.e("VoskAPI", "Failed to unpack the model", exception);
                });
    }


    // Start recording audio
    public void startRecording() {
        if (recorder != null && recorder.getState() == AudioRecord.STATE_INITIALIZED) {
            // Start the audio recording
            recorder.startRecording();
            isRecording = true;
            // Call the method that contains the loop to continuously read data from the microphone
            recordingLoop();
        }
    }

    // Stop recording audio
    public void stopRecording() {
        if (recorder != null && recorder.getState() == AudioRecord.STATE_INITIALIZED) {
            // Stop the recording
            isRecording = false;
            recorder.stop();
        }
    }

    // Method that runs in a separate thread to read audio data from the microphone
    private void recordingLoop() {
        new Thread(() -> {
            while (isRecording) {
                // Read the audio data into the buffer and capture the number of bytes read
                int readResult = recorder.read(audioBuffer, 0, audioBuffer.length);
                if (readResult > 0) {
                    // Optionally, process the audio data (e.g., send to speech recognizer)
                    // Example: recognizer.acceptWaveForm(audioBuffer, readResult);
                }
            }
        }).start(); // Start the thread to begin the loop
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}
