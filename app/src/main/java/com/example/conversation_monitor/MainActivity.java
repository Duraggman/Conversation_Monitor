package com.example.conversation_monitor;

import ai.picovoice.cheetah.Cheetah;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;


public class MainActivity extends AppCompatActivity {
    Button recordButton;
    TextView rText;
    StringBuilder transcriptionBuilder;
    private Cheetah cheetah;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check for recording audio permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }

        // Initialize the cheetah instance
        try {
            // Copy the model file from the assets directory to the internal storage
            String modelPath = copyAssetToFile("leopard_params.pv");
            cheetah = new Cheetah.Builder()
                    .setAccessKey("o8A07SUfYMqhsJr5tohOBqlT1ZjdgKxVelMjVA2iMjLmOJII04tl5A==") // Replace with your actual access key
                    .setModelPath(modelPath) // Ensure this path is correct
                    .build(getApplicationContext());
        } catch (Exception e) {
            Log.e("Cheetah", "Failed to initialize Cheetah", e);
        }
    }

    // Make sure to call leopard.delete() in your onDestroy() method to release resources
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cheetah != null) {
            cheetah.delete();
        }
    }

    private String copyAssetToFile(String filename) throws IOException {
        File outFile = new File(getFilesDir(), filename);
        if (!outFile.exists()) {
            AssetManager assetManager = getAssets();
            InputStream in = assetManager.open(filename);
            OutputStream out = Files.newOutputStream(outFile.toPath());
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        }
        return outFile.getAbsolutePath();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                // Permission denied
                Log.e("Cheetah", "Recording audio permission denied");
            }
        }
    }


}
