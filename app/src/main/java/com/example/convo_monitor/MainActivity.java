package com.example.convo_monitor;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    // Creating variable for the main activity attributes
    public UiController ui;
    public VoskProvider vosk;
    public VoskTranscriber vt;

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
        AppUtils utils = new AppUtils();
        vosk = new VoskProvider(this, this, utils);
        vt = new VoskTranscriber(vosk);
        ui = new UiController(this, vosk,vt);
        ui.setListeners(); // Setting the listeners after the transcriber is set
        ui.createUI();

    }
}