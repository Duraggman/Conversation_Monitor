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
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class AppUtils {
    public static final int SAMPLE_RATE = 16000;
    public static final byte[] AUDIO_BUFFER = new byte[2048];
    public static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    // Use mono channel for microphone input
    public static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    // Define audio format as 16-bit PCM
     public static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
     public ParticipantManager pm;
    public static final int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, FORMAT);
    public static final int BUFFER_SIZE = Math.max(minBufferSize, 2048);
    public static boolean isRecording =  false;
    public static boolean isFirstTime = true;
    public static double spkVolume;
    public static HashMap<String, Double> speakerTimes = new HashMap<>();
    public static String currentSpeaker = "";
    public static double pDuration = 0;
    public static boolean isMillis;

    public AppUtils(){

    }
    public void setPM(ParticipantManager pM) {
        pm = pM;
    }
    public static String compareSpeaker(double[] incomingVector, ParticipantManager pm) {
        double maxSimilarity = -1; // Start with the lowest possible similarity
        String bestMatchParticipantName = null; // Name of the best matching participant

        for ( ParticipantManager.Participant p : pm.participants) {
            if (p != null) {
                double similarity = cosineSimilarity(incomingVector, p.refVector());
                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    bestMatchParticipantName = p.tagText(); // Get the tag of most similar participant
                }
            }
        }
        return Objects.requireNonNullElse(bestMatchParticipantName, "Unknown Speaker");
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
            double[] normVector = normalizeVector(spkVector); // Normalize the speaker vector for better accuracy
            Log.d("utils", "spkVector: " + Arrays.toString(normVector));
            return normVector;
        } catch (JSONException e) {
            Log.e("utils", "Error parsing JSON: " + e.getMessage());
            return new double[0]; // Return an empty array on parsing error
        }
    }
    public static double[] normalizeVector(double[] vector) {
        // Initialize the variable 'norm' to store the norm (magnitude) of the vector.
        double norm = 0.0;

        // Calculate the sum of squares of all vector components.
        for (double component : vector) {
            norm += component * component;
        }

        // Compute the square root of the sum of squares to get the Euclidean norm.
        norm = Math.sqrt(norm);

        // Create a new array to store the normalized vector.
        double[] normalizedVector = new double[vector.length];

        // Divide each component of the original vector by the norm to normalize.
        for (int i = 0; i < vector.length; i++) {
            normalizedVector[i] = vector[i] / norm;
        }

        // Return the normalized vector.
        return normalizedVector;
    }
    
    public static double cosineSimilarity(double[] vectorA, double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
            Log.i("cosineSimilarity", "Index " + i + ": dotProduct=" + dotProduct + ", normA=" + normA + ", normB=" + normB);
        }

        // Check to prevent division by zero
        if (normA == 0 || normB == 0) {
            Log.e("cosineSimilarity", "One of the vectors has zero norm. Returning 0.0 as similarity.");
            return 0;  // or handle this case according to your needs
        }

        double result = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        Log.i("cosineSimilarity", "Final similarity: " + result);
        return result;
    }
    public static String copyAssetsToLocalStorage(Context context, String path)  throws IOException{
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
    public static boolean checkAndRequestAudioPermissions(Activity activity, Context context) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((activity),new String[] {android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        }
        // Check if permission are granted if are return true else return false
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }
    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
    public static double measureVolume(byte[] audioBuffer) {
        double sum = 0;
        for (int sample : audioBuffer) {
            sum += Math.pow(sample, 2);
        }
        double rms = Math.sqrt(sum / audioBuffer.length);

        return 20 * Math.log10(rms / Math.sqrt(2));
    }
    public static void setDuration(double duration) {
        // Set the duration of the speaking time
        pDuration = duration;
    }

    public String jsonSpkToString(String json, boolean isPartial, boolean isFinal) {
        JSONObject jsonObject;
        if (isPartial) {
            try {
                jsonObject = new JSONObject(json);
                String text = jsonObject.optString("partial", "No transcription available");

                // Extract speaker vector using the jsonSpkVectorExtract method
                double[] spkVector = jsonSpkVectorExtract(json);

                String speakerId = compareSpeaker(spkVector, pm);
                // Create a string representation of the speaker vector
                if (AppUtils.spkVolume != 0) {
                    return String.format(Locale.UK, "%s: %s (%f"+"db) ", speakerId, text, AppUtils.spkVolume);
                }
                else {
                    return String.format(Locale.UK, "%s: %s ", speakerId, text);
                }
            } catch (JSONException e) {
                Log.e("JsonParser", "Error parsing JSON: " + e.getMessage());
                return "Parsing error: " + e.getMessage();
            }
        }
        else {
            if (!isFinal) {
                try {
                    jsonObject = new JSONObject(json);
                    String text = jsonObject.optString("text", "No transcription available");

                    // Extract speaker vector using the jsonSpkVectorExtract method
                    double[] spkVector = jsonSpkVectorExtract(json);

                    // Create a string representation of the speaker vector
                    String speakerId = compareSpeaker(spkVector, pm);
                    // Update speaking time for the speaker
                    updateSpeakingTime(speakerId, pDuration);

                    if (AppUtils.spkVolume != 0) {
                        double speakingTime = getSpeakingTime(speakerId);
                        if (AppUtils.isMillis) {
                            return String.format(Locale.UK, "%s: %s (%.2f" + "dB), (%.3f" + "ms)", speakerId, text, AppUtils.spkVolume, speakingTime);
                        } else {
                            return String.format(Locale.UK, "%s: %s (%.2f" + "dB), (%.3f" + "secs)", speakerId, text, AppUtils.spkVolume, speakingTime);
                        }
                    }

                    else {
                        return String.format(Locale.UK, "%s: %s ", speakerId, text);
                    }
                } catch (JSONException e) {
                    Log.e("JsonParser", "Error parsing JSON: " + e.getMessage());
                    return "Parsing error: " + e.getMessage();
                }
            }
            else {
                try {
                    jsonObject = new JSONObject(json);
                    String text = jsonObject.optString("text", "No transcription available");

                    if (!text.isEmpty()) {
                    // Extract speaker vector using the jsonSpkVectorExtract method
                    double[] spkVector = jsonSpkVectorExtract(json);

                        // Create a string representation of the speaker vector
                        String speakerId = compareSpeaker(spkVector, pm);
                        // Update speaking time for the speaker
                        updateSpeakingTime(speakerId, pDuration);

                        if (AppUtils.spkVolume != 0) {
                            double speakingTime = getSpeakingTime(speakerId);
                            if (AppUtils.isMillis) {
                                return String.format(Locale.UK, "%s: %s (%.2f" + "dB), (%.3f" + "ms)", speakerId, text, AppUtils.spkVolume, speakingTime);
                            } else {
                                return String.format(Locale.UK, "%s: %s (%.2f" + "dB), (%.3f" + "secs)", speakerId, text, AppUtils.spkVolume, speakingTime);
                            }
                        } else {
                            return String.format(Locale.UK, "%s: %s ", speakerId, text);
                        }
                    }
                    else {
                        return "empty";
                    }
                } catch (JSONException e) {
                    Log.e("JsonParser", "Error parsing JSON: " + e.getMessage());
                    return "Parsing error: " + e.getMessage();
                }
            }
        }
    }

    // Retrieve total speaking time for a speaker
    public static double getSpeakingTime(String speakerId) {
        isMillis = false;

        double totalSpeakingTimeMillis = (speakerTimes.getOrDefault(speakerId, 0D));
        Log.i("time", "time in milliseconds: " + totalSpeakingTimeMillis);
        if (totalSpeakingTimeMillis == 0F) {
            Log.i("GST", "Speaker ID " + speakerId + " not found in speakerTimes map. Returning 0.");
            return totalSpeakingTimeMillis;
        }
        else {
            double speakingTimeSeconds = totalSpeakingTimeMillis / 1000;
            Log.i("time", "time in seconds: " + speakingTimeSeconds);
            Log.i("GST", "Speaker ID " + speakerId + " found. Total speaking time in milliseconds: " + totalSpeakingTimeMillis + ". Converted to seconds: " + speakingTimeSeconds);


            return speakingTimeSeconds; // Convert milliseconds to seconds
        }
    }
    // Update speaking time for a given speaker
    public static void updateSpeakingTime(String speakerId, double duration) {
        double totalSpeakingTime = speakerTimes.getOrDefault(speakerId, 0D);
        Log.i("time", "UST, time in milliseconds: " + totalSpeakingTime);
        totalSpeakingTime += duration;
        speakerTimes.put(speakerId, totalSpeakingTime);
        Log.i("GST", "Updated speaking time for Speaker ID " + speakerId + ". New total speaking time: " + totalSpeakingTime);
    }
}

