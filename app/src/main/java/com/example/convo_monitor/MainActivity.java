package com.example.convo_monitor;

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
    private Button recordButton;
    private TextView transcribedText;
    private RTTranscriberVosk RTTranscriberVosk;
    public VoiceActivityDetector vad;


    private boolean isRecording = false;

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

        // assigning the main activity attributes to the corresponding xml elements and classes
        recordButton = findViewById(R.id.RecordButton);
        transcribedText = findViewById(R.id.TranscribedView);
        RTTranscriberVosk = new RTTranscriberVosk(this, transcribedText , this);
        vad = new VoiceActivityDetector(this, new byte[0]);


        recordButton.setOnClickListener(v -> {
            if (isRecording) {
                // Stop recording when the button is clicked
                stopRecording();
            } else {
                // Start recording when the button is clicked
                startRecording();

            }
        });
    }

    protected void startRecording() {

        // Start recording when the activity starts
        recordButton.setText(R.string.stopRecording);
        RTTranscriberVosk.startRecording();
        isRecording = true;
    }

    protected void stopRecording() {
        // Stop recording when the activity is no longer visible
        recordButton.setText(R.string.startRecording);
        RTTranscriberVosk.stopRecording();
        isRecording = false;
    }
}