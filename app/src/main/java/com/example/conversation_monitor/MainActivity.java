package com.example.conversation_monitor;


import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.mozilla.speechlibrary.SpeechService;
import com.mozilla.speechlibrary.SpeechServiceSettings;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize DeepSpeech SpeechService with the language model "en-UK"
        SpeechServiceSettings.Builder settingsBuilder = new SpeechServiceSettings.Builder();
        settingsBuilder.withLanguage("en-UK");
        SpeechService mSpeechService = new SpeechService(this);

    }

}