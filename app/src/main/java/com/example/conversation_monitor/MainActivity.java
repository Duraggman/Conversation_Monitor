package com.example.conversation_monitor;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.mozilla.speechlibrary.SpeechResultCallback;
import com.mozilla.speechlibrary.SpeechService;
import com.mozilla.speechlibrary.SpeechServiceSettings;
import com.mozilla.speechlibrary.stt.STTResult;

import java.io.*;

// TODO: Implement a check to verify if the model is ready before starting the SpeechService, do this by checking that the model file has been loaded properly

public class MainActivity extends AppCompatActivity {

    Button recordButton;

    TextView rText;

    StringBuilder transcriptionBuilder;

    ProgressBar progBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the record button from the layout
        recordButton = findViewById(R.id.rBtn);
        // Get the EditText for displaying the transcription
        rText = findViewById(R.id.rText);

        // Request RECORD_AUDIO permission
        final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }

        // Get the AssetManager instance
        AssetManager assetManager = getAssets();

        // List all files in the assets directory to verify the model file exists
        try {
            String[] files = assetManager.list("");  // Lists everything in the assets folder
            for (String file : files) {
                Log.d("AssetFiles", file);
            }
        } catch (IOException e) {
            Log.e("AssetFiles", "Error listing assets", e);
        }

        // Initialize the SpeechService
        SpeechService mSpeechService = new SpeechService(this);
        // Create a StringBuilder for the transcription
        transcriptionBuilder = new StringBuilder();


        // button setup
        try {
            // Copy the model file from assets to the internal storage
            String modelPath = copyAssetToFile("deepspeech-0.9.3-models.pbmm");

            // Configure settings for the speech service
            SpeechServiceSettings settings = new SpeechServiceSettings.Builder()
                    .withLanguage("en-UK")
                    .withModelPath(modelPath)
                    .withUseDeepSpeech(true)
                    .withStoreTranscriptions(true)
                    .build();



            // Set the OnClickListener for the record button
            recordButton.setOnClickListener(v -> {
                mSpeechService.start(settings, mVoiceSearchListener);
                Log.d("ModelLoader", "model loaded correctly");
            });

        } catch (IOException e) {
            Log.e("ModelLoader", "Error copying model from assets", e);
        }
    }


    SpeechResultCallback mVoiceSearchListener = new SpeechResultCallback() {

        @Override
        public void onStartListen() {
            // Handle when the api successfully opened the microphone and started listening
            recordButton.setText("Listening...");
        }

        @Override
        public void onMicActivity(double fftsum) {
            // Normalize fftsum to a range suitable for your ProgressBar (e.g., 0-100)
            int progress = (int) (fftsum * 100);

            // Ensure progress stays within the ProgressBar bounds
            progress = Math.max(0, Math.min(progress, 100));

            // Update the ProgressBar on the UI thread
            int finalProgress = progress;
            runOnUiThread(() -> {
                progBar.setProgress(finalProgress);
            });
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
            recordButton.setText("No Voice");
        }

        @Override
        public void onError(@SpeechResultCallback.ErrorType int errorType, @Nullable String error) {
            // Handle when any error occurred
            Log.e("SpeechService", "Error: " + error);




        }
    };

    // Function to copy a file from assets to the internal storage
    private String copyAssetToFile(String assetName) throws IOException {
        File outFile = new File(getFilesDir(), assetName);
        if (!outFile.exists()) {
            try (InputStream is = getAssets().open(assetName);
                 OutputStream os = new FileOutputStream(outFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            }
        }
        return outFile.getAbsolutePath();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                // Permission denied
            }
        }
    }


}