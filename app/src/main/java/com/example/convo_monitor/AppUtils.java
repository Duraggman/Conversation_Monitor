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
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class AppUtils {
    private final int SAMPLE_RATE;
    private final byte[] audioBuffer;
    public static int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    // Use mono channel for microphone input
    private final int CHANNEL;
    // Define audio format as 16-bit PCM
     int FORMAT;
     private static ParticipantManager pm;
    private final int BufferSize;
    public static boolean isRecording = false;
    public static boolean isFirstTime = true;
    public static int newPCount = 0;

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

     static boolean checkAndRequestAudioPermissions(Activity activity, Context context) {
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
    public String copyAssetsToLocalStorage(Context context, String path)  throws IOException{
        boolean dirCreated = true;
        AssetManager assetManager = context.getAssets();
        String destinationPath = context.getFilesDir().getAbsolutePath();
        String[] files;
            files = assetManager.list(path);
            File destinationFolder = new File(destinationPath);
            if (!destinationFolder.exists()) {
               dirCreated  =destinationFolder.mkdir(); // Create the folder if it does not exist
            }
            if (!dirCreated) {
                Log.e("AppUtils", "Failed to create directory: " + destinationFolder.getAbsolutePath());
            }
            try {
                assert files != null;
                for (String filename : files) {
                    InputStream in = assetManager.open(path + "/" + filename);
                    File outFile = new File(destinationFolder, filename);
                    OutputStream out = Files.newOutputStream(outFile.toPath());
                    copyFile(in, out);
                    in.close();
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                Log.e("AppUtils", "Error copying assets: " + e.getMessage(), e);
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

            // Extract speaker vector using the jsonSpkVectorExtract method
            double[] spkVector = jsonSpkVectorExtract(json);

            String speakerId = compareSpeaker(spkVector, AppUtils.getPM());
            double speakerConfidence = jsonObject.optDouble("confidence", 0.0); // Default to 0.0 if no confidence score

            // Create a string representation of the speaker vector
            String spkVectorStr = Arrays.toString(spkVector);

            return String.format(Locale.UK, "Speaker %d: %s (Confidence: %.2f%%) [Vector: %s]", speakerId, text, speakerConfidence * 100, spkVectorStr);
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
            Log.d("utils", "spkVector: " + Arrays.toString(spkVector));
            return spkVector;
        } catch (JSONException e) {
            Log.e("utils", "Error parsing JSON: " + e.getMessage());
            return new double[0]; // Return an empty array on parsing error
        }
    }

    public static String compareSpeaker(double[] incomingVector, ParticipantManager pm) {
        double maxSimilarity = -1; // Start with the lowest possible similarity
        String bestMatchParticipantName = null; // Name of the best matching participant

        for (ParticipantManager.Participant p : pm.participants) {
            double similarity = cosineSimilarity(incomingVector, p.refVector());
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                bestMatchParticipantName = p.tagText(); // Get the tag of most similar participant
            }
        }
        return Objects.requireNonNullElse(bestMatchParticipantName, "No match found");
    }

    public static ParticipantManager getPM() {
        return AppUtils.pm;
    }

    public void setPM(ParticipantManager pm) {
        AppUtils.pm = pm;
    }
    public static double cosineSimilarity(double[] vectorA, double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}

