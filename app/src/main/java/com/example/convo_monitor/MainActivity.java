package com.example.convo_monitor;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    // TODO: Use and test the AudioRecorder class in the MainActivity

    // Creating variable for the main activity attributes
    private Button recordButton;
    private TextView transcribedText;
    private AudioRecorder audioRecorder;


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
        audioRecorder = new AudioRecorder(this, transcribedText , this);

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
        recordButton.setText("Stop Recording");
        audioRecorder.startRecording();
        isRecording = true;
    }

    protected void stopRecording() {
        // Stop recording when the activity is no longer visible
        recordButton.setText("Start Recording");
        audioRecorder.stopRecording();
        isRecording = false;
    }
}