package com.example.convo_monitor;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Locale;

public class AppUtils {
    private int SAMPLE_RATE;
    private byte[] audioBuffer;
    public static int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    // Use mono channel for microphone input
    private int CHANNEL;
    // Define audio format as 16-bit PCM
     int FORMAT;
    private int BufferSize;

    public AppUtils() {
        SAMPLE_RATE = 16000;
        audioBuffer = new byte[2048];
        // Use mono channel for microphone input
        CHANNEL = AudioFormat.CHANNEL_IN_MONO;
        // Define audio format as 16-bit PCM
        FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, FORMAT);
        BufferSize = Math.max(minBufferSize, 2048);
    }

     static Boolean checkAndRequestAudioPermissions(Activity activity, Context context) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((activity),new String[] {android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        }
        // Check if permission are granted if are return true else return false
         return ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }
    // getters
    public int getSAMPLE_RATE() {
        return SAMPLE_RATE;
    }
    public byte[] getAudioBuffer() {
        return audioBuffer;
    }

    public int getCHANNEL() {
        return CHANNEL;
    }
    public int getFORMAT() {
        return FORMAT;
    }
    public int getBufferSize() {
        return BufferSize;
    }

    //setters
    public void setSAMPLE_RATE(int SAMPLE_RATE) {
        this.SAMPLE_RATE = SAMPLE_RATE;
    }
    public void setAudioBuffer(byte[] audioBuffer) {
        this.audioBuffer = audioBuffer;
    }

    public void setCHANNEL(int CHANNEL) {
        this.CHANNEL = CHANNEL;
    }
    public void setFORMAT(int FORMAT) {
        this.FORMAT = FORMAT;
    }
    public void setBufferSize(int BufferSize) {
        this.BufferSize = BufferSize;
    }

    public String copyAssetsToLocalStorage(Context context, String path)  throws IOException{
        AssetManager assetManager = context.getAssets();
        String destinationPath = context.getFilesDir().getAbsolutePath();
        String[] files = null;
            files = assetManager.list(path);
            File destinationFolder = new File(destinationPath);
            if (!destinationFolder.exists()) {
                destinationFolder.mkdir(); // Create the folder if it does not exist
            }

            for (String filename : files) {
                InputStream in = assetManager.open(path + "/" + filename);
                File outFile = new File(destinationFolder, filename);
                OutputStream out = Files.newOutputStream(outFile.toPath());
                copyFile(in, out);
                in.close();
                out.flush();
                out.close();
            }

        return destinationPath; // Return the path to the directory where files are copied
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    public static String jsonSpkToString(String json) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json);
            String text = jsonObject.optString("text", "No transcription available");
            int speakerId = jsonObject.optInt("speaker", 0); // Default to 0 if no speaker ID
            double speakerConfidence = jsonObject.optDouble("confidence", 0.0); // Default to 0.0 if no confidence score
            return String.format(Locale.UK, "Speaker %d: %s (Confidence: %.2f%%)", speakerId, text, speakerConfidence * 100);
        } catch (JSONException e) {
            Log.e("JsonParser", "Error parsing JSON: " + e.getMessage());
            return "Parsing error: " + e.getMessage();
        }
    }

    public static String jsonPartialToString(String json) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json);
            String partialText = jsonObject.optString("partial", "");
            int speakerId = jsonObject.optInt("speaker", 0); // Default to 0 if no speaker ID
            double speakerConfidence = jsonObject.optDouble("confidence", 0.0); // Default to 0.0 if no confidence score
            return partialText.isEmpty() ? "No partial transcription available" : String.format(Locale.UK, "Speaker %d (Partial): %s (Confidence: %.2f%%)", speakerId, partialText, speakerConfidence * 100);
        } catch (JSONException e) {
            Log.e("JsonParser", "Error parsing JSON: " + e.getMessage());
            return "Parsing error: " + e.getMessage();
        }
    }

    public static double[] jsonSpkVectorExtract(String json) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json);
            JSONArray spkVectorArray = jsonObject.optJSONArray("spk");
            if (spkVectorArray == null) {
                Log.e("utils", "No 'spk' vector found in JSON");
                return new double[0]; // Return an empty array if no "spk" vector found
            }
            double[] spkVector = new double[spkVectorArray.length()];
            for (int i = 0; i < spkVectorArray.length(); i++) {
                spkVector[i] = spkVectorArray.optDouble(i, 0.0); // Default to 0.0 if any entry is not a valid double
            }
            return spkVector;
        } catch (JSONException e) {
            Log.e("utils", "Error parsing JSON: " + e.getMessage());
            return new double[0]; // Return an empty array on parsing error
        }
    }
}

