package com.example.convo_monitor;

import android.media.AudioFormat;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    // Creating variable for the main activity attributes
    public UiController ui;
    public AppUtils utils;
    public VoskProvider vosk;
    private ParticipantManager pm;
    private VoskTranscriber vt;
    // Using 16 kHz sample rate for audio recording for compatibility with speech recognition APIs
    public static int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    public static  int SAMPLE_RATE = 16000;
    // Use mono channel for microphone input
    public static  int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    // Define audio format as 16-bit PCM
    public static  int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    // Buffer to hold the audio data during each read operation
    public static byte[] audioBuffer = new byte[16000];

    // Request code for audio permission request
    // Threshold for silence detection. Adjust based on your environment.
    //public static final double SILENCE_THRESHOLD = -70.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the parent class constructor
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        utils = new AppUtils();
        vosk = new VoskProvider(this, this, utils);
        ui = new UiController(this, vosk, utils);

        ui.createUI();
        vt = new VoskTranscriber(vosk, utils, ui);
    }
}