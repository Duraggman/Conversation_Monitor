package com.example.conversation_monitor;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.mozilla.speechlibrary.SpeechResultCallback;
import com.mozilla.speechlibrary.SpeechService;
import com.mozilla.speechlibrary.SpeechServiceSettings;
import com.mozilla.speechlibrary.stt.STTResult;
import com.mozilla.speechlibrary.utils.ModelUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

// TODO: Fix insufficient storage error.
public class MainActivity extends AppCompatActivity {
    // Declare the SpeechService to manage voice recognition.
    private SpeechService mSpeechService;
    // Declare the SpeechResultCallback to handle voice recognition events.
    private SpeechResultCallback mVoiceSearchListener;
    // Declare the UI elements for the app.
    private TextView transcriptionTextView;
    private Button recordButton;
    private String language = "en-US";
    private CheckBox modelCheckBox;

    String modelPath = ModelUtils.modelPath(this, language);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to your layout defined in XML.
        setContentView(R.layout.activity_main);

        // Initialize the UI elements.
        transcriptionTextView = findViewById(R.id.rText);
        recordButton = findViewById(R.id.RecBtn);

        // Initialize the speech recognition service.
        initializeSpeechService();

        if (ModelUtils.isReady(modelPath)) {
            modelCheckBox.setChecked(true);
        }

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSpeechService != null) {

                    try {
                        SpeechServiceSettings settings = new SpeechServiceSettings.Builder()
                                .withLanguage(language)
                                .withModelPath(copyAssetToFile("deepspeech-0.9.3-models.pbmm"))
                                .build();
                        mSpeechService.start(settings, mVoiceSearchListener);
                    } catch (IOException e) {
                        Log.e("MainActivity", "Error setting up speech service", e);
                    }
                }
            }

        });
    }

    private void initializeSpeechService() {
        // Create an instance of SpeechResultCallback to handle various speech events.
        mVoiceSearchListener = new SpeechResultCallback() {
            @Override
            public void onStartListen() {
                // This is called when the speech service has successfully started listening.
                Log.i("Speech", "Started listening");
            }

            @Override
            public void onMicActivity(double fftsum) {
                // This method reports microphone activity, useful for visual feedback in UI.
                Log.i("Speech", "Microphone activity detected: " + fftsum);
            }

            @Override
            public void onDecoding() {
                // This is triggered when the speech decoding process begins.
                Log.i("Speech", "Decoding speech");
            }

            @Override
            public void onSTTResult(@Nullable STTResult result) {
                // This is triggered when the speech-to-text processing is complete.
                if (result != null) {
                    runOnUiThread(() -> transcriptionTextView.setText(String.format("Transcription: %s", result.mTranscription)));
                }
            }

            @Override
            public void onNoVoice() {
                // This is called if no voice is detected.
                Log.i("Speech", "No voice detected");
            }

            @Override
            public void onError(@SpeechResultCallback.ErrorType int errorType, @Nullable String error) {
                // Handle any errors during the speech recognition process.
                Log.e("Speech", "Error occurred: " + error);
            }
        };

        // Set up and start the speech service with configuration settings.
        setupSpeechService();
    }

    private void setupSpeechService() {
        try {
            String modelPath = copyAssetToFile("deepspeech-0.9.3-models.pbmm");
            mSpeechService = new SpeechService(this);
            SpeechServiceSettings settings = new SpeechServiceSettings.Builder()
                    .withLanguage("en-US")
                    .withStoreSamples(true)
                    .withStoreTranscriptions(true)
                    .withProductTag("product-tag")
                    .withUseDeepSpeech(true)
                    .withModelPath(modelPath)
                    .build();

            mSpeechService.start(settings, mVoiceSearchListener);
        } catch (IOException e) {
            Log.e("MainActivity", "Failed to copy assets and initialize speech service", e);
        }
    }

    private String copyAssetToFile(String filename) throws IOException {
        File file = new File(getFilesDir(), filename);
        if (!file.exists()) {
            try (InputStream is = getAssets().open(filename);
                 OutputStream os = Files.newOutputStream(file.toPath())) {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
            }
        }
        return file.getAbsolutePath();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Properly stop and release resources from the speech service when the activity is destroyed.
        if (mSpeechService != null) {
            mSpeechService.stop(); // Ensure the service is stopped to release resources.
        }
    }
}
