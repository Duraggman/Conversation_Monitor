package com.example.convo_monitor;

import android.media.AudioFormat;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {


    // Creating variable for the main activity attributes
    public AppUtils utils;
    private Button recordButton;
    public VoskProvider vosk;
    private TextView transcribedText;
    private VoskTranscriber vt;
    public VoiceActivityDetector vad;
    public SpeakerDiarization sd;
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


    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the parent class constructor
        super.onCreate(savedInstanceState);
        //Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        utils = new AppUtils();

        // assigning the main activity attributes to the corresponding xml elements and classes
        recordButton = findViewById(R.id.RecordButton);
        transcribedText = findViewById(R.id.TranscribedView);
        vosk = new VoskProvider(this, this, utils);
        vt = new VoskTranscriber(vosk, utils, transcribedText, recordButton);
        //sd = new SpeakerDiarization(this, this, vosk);
        //vad = new VoiceActivityDetector(this, new byte[0]);
        recordButton.setOnClickListener(v -> {
            if (isRecording) {
                // Stop recording when the button is clicked
                vt.stopRecording();
                isRecording = false;
            } else {
                // Start recording when the button is clicked
                vt.startRecording();
                isRecording = true;
            }
        });
    }




}