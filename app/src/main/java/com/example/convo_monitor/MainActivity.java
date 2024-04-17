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
// import log class from android.util package
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    // TODO: Use and test the AudioRecorder class in the MainActivity

    // Creating variable for the main activity attributes
    private Button recordButton;
    private TextView transcribedText;
    private AudioRecorder audioRecorder;
    private static final int REQUEST_AUDIO_PERMISSION_CODE = 200;

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
        audioRecorder = new AudioRecorder(this, transcribedText);


        // Check if recording permissions are already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            audioRecorder.initRecorder();  // Initialize components of the recorder that depend on permission.
        } else {
            // If not granted, request permissions
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_PERMISSION_CODE);
        }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_AUDIO_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            audioRecorder.initRecorder();  // Now permissions are granted, initialize the recorder.
        } else {
            Toast.makeText(this, "Permission Denied to record audio", Toast.LENGTH_SHORT).show();
        }
    }
}