package com.example.convo_monitor;

//Vosk imports
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.StorageService;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;

/**
 * This class is responsible for managing audio.
 */
// TODO: 1. Find out why class no longer works2.Test out different silence thresholds, frame sizes and audio buffer sizes to optimize performance

public class RTTranscriberVosk {
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

    // Threshold for silence detection. Adjust based on your environment.
    private static final double SILENCE_THRESHOLD = -70.0;

    private Context context; // Context is necessary for file and permission management
    private TextView textview; // TextView
    private Activity activity;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    // Calculate the minimum required buffer size for the specified audio settings
    int minBufferSize = SAMPLE_RATE;

    // Constructor for AudioRecorder
    public RTTranscriberVosk(Context context, TextView transcribedText, Activity activity) {
        this.context = context; // Set the context for the AudioRecorder
        this.textview = transcribedText; // Set the TextView
        this.activity = activity;

        // Check if user has given permission to record audio, init the model after permission is granted
        int permissionCheck = ContextCompat.checkSelfPermission(context.getApplicationContext(), android.Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.activity, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            initRecorder();  // Initialize components of the recorder that depend on permission.
        } else {
            initRecorder();
        }
    }

    public void initRecorder() {
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
        StorageService.unpack(this.context, "model-en-us", "model",
                (model) -> {
                    this.model = model;
                    Log.i("AudioRecorder", "Model unpacked successfully");
                    // Initialize the recognizer with the model and sample rate
                    try {
                        recognizer = new Recognizer(model, SAMPLE_RATE);
                    } catch (IOException e) {
                        Log.e("AudioRecorder", "Failed to initialize the recognizer" + e.getMessage());
                    }
                },
                (exception) -> Log.e("AudioRecorder","Failed to unpack the model" + exception.getMessage()));
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
                        // Display the partial recognition result in the TextView
                        textview.post(() -> textview.setText(result));
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
