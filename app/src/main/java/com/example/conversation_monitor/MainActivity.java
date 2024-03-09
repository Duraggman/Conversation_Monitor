package com.example.conversation_monitor;


import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.mozilla.speechlibrary.SpeechResultCallback;
import com.mozilla.speechlibrary.SpeechService;
import com.mozilla.speechlibrary.SpeechServiceSettings;
import com.mozilla.speechlibrary.stt.STTResult;

public class MainActivity extends AppCompatActivity {

    Button recordButton;
    TextView rText;

    StringBuilder transcriptionBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize DeepSpeech SpeechService with the language model "en-UK"
        SpeechServiceSettings.Builder settingsBuilder = new SpeechServiceSettings.Builder();
        settingsBuilder.withLanguage("en-UK");
        settingsBuilder.withModelPath("file:///android_asset/deepspeech-0.9.3-models.pbmm");
        settingsBuilder.withUseDeepSpeech(true);
        settingsBuilder.withStoreTranscriptions(true);

        SpeechService mSpeechService = new SpeechService(this); // Initialize the SpeechService with the current context

         recordButton = findViewById(R.id.rBtn);
            rText = findViewById(R.id.rText);
        recordButton.setOnClickListener(v -> mSpeechService.start(settingsBuilder.build(), mVoiceSearchListener));
    }


    SpeechResultCallback mVoiceSearchListener = new SpeechResultCallback() {

        @Override
        public void onStartListen() {
            // Handle when the api successfully opened the microphone and started listening
            recordButton.setText("Listening...");
        }

        @Override
        public void onMicActivity(double fftsum) {
            // Captures the activity from the microphone
        }

        @Override
        public void onDecoding() {
            // Handle when the speech object changes to decoding state
        }

        @Override
        public void onSTTResult(@Nullable STTResult result) {
            // When the api finished processing and returned a hypothesis

            if (result != null) {
                String transcript = result.mTranscription;
                transcriptionBuilder.append(transcript);

                // Update UI or perform any actions with the transcription
                // For example, you can display the transcription in a TextView in real-time.
                rText.setText(transcriptionBuilder.toString());
            }
        }

        @Override
        public void onNoVoice() {
            // Handle when the api didn't detect any voice
        }

        @Override
        public void onError(@SpeechResultCallback.ErrorType int errorType, @Nullable String error) {
            // Handle when any error occurred

        }
    };


}