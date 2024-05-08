package com.example.convo_monitor;

import static com.example.convo_monitor.AppUtils.BUFFER_SIZE;
import static com.example.convo_monitor.AppUtils.CHANNEL;
import static com.example.convo_monitor.AppUtils.FORMAT;
import static com.example.convo_monitor.AppUtils.SAMPLE_RATE;
import static com.example.convo_monitor.AppUtils.checkAndRequestAudioPermissions;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.Toast;

import org.vosk.Recognizer;
import org.vosk.SpeakerModel;
import org.vosk.android.StorageService;
import org.vosk.Model;
import java.io.IOException;

public class VoskProvider {
    private Recognizer recognizer;
    private AudioRecord recorder;
    private Model tModel;
    private SpeakerModel idModel;
    public final Context context;
    private final AppUtils utils;
    private boolean isCogInit = false;

    public VoskProvider(Context context, Activity activity, AppUtils utils) {
        this.context = context;
        this.utils = utils;
        try {
            if (checkAndRequestAudioPermissions(activity, context)) {
                initRecorder();
                initTModel();
                initIDModel();
            } else {
                Toast.makeText(context, "Audio permissions are required for recording", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("VoskProvider", "Failed to initialize the VoskProvider: " + e.getMessage());
            Toast.makeText(context, "Permissions not granted.", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void initRecorder(){
        // Initialize the AudioRecord object
        try {
            // using a local recorder object
            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL, FORMAT, BUFFER_SIZE);
            if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e("vp", "Audio Record can't initialize!");
                Log.e("vosk", "Audio Record can't initialize!");
            } else {
                Log.i("vp", "Recorder initialized successfully");
                Log.i("vosk", "Recorder initialized successfully");
                this.recorder = recorder;
            }
        } catch (Exception e) {
            Log.e("vp", "Failed to initialize the recorder" + e.getMessage());
            Log.e("vosk", "Failed to initialize the recorder" + e.getMessage());
        }
    }

    public AppUtils getUtils() {
        return this.utils;
    }

    private void initTModel(){
        // Load the Vosk model from the assets folder using the StorageService class from Vosk
        StorageService.unpack(this.context, "model-en-us", "model",
                (model) -> {
                    Log.i("vp", "rec Model unpacked successfully");
                    Log.i("vosk", "Model unpacked successfully");
                    this.tModel = model;
                    initRecognizer();
                }, (exception) -> {
                    Log.e("vp", "Failed to unpack the rec model" + exception.getMessage());
                    Log.e("vosk", "Failed to unpack the rec model" + exception.getMessage());
                });
    }
    private void initIDModel() {
        try {
            String modelPath = AppUtils.copyAssetsToLocalStorage(this.context, "model-spk");
            try {
                this.idModel = new SpeakerModel(modelPath);
                Log.i("vp", "ID Model initialized successfully");
                Log.i("vosk", "ID Model initialized successfully");
            } catch (IOException e) {
                Log.e("vp", "Failed to initialize the ID model " + e.getMessage());
                Log.e("vosk", "Failed to initialize the ID model " + e.getMessage());
            }
        } catch (IOException e) {
            Log.e("vp", "Failed to copy the ID model to local storage " + e.getMessage());
            Log.e("vosk", "Failed to copy the ID model to local storage " + e.getMessage());
        }
    }


    private void initRecognizer(){
        try {
            recognizer = new Recognizer(this.tModel, SAMPLE_RATE, this.idModel);
            Log.i("vp", "Recognizer initialized successfully");
            Log.i("vosk", "Recognizer initialized successfully");
            isCogInit = true;
        } catch (IOException e) {
            Log.e("vp", "Failed to initialize the recognizer" + e.getMessage());
            Log.e("vosk", "Failed to initialize the recognizer" + e.getMessage());
        }
    }

    public Recognizer getRecognizer(){return recognizer;}
    public AudioRecord getRecorder(){
        return recorder;
    }
    public boolean isCogInit(){
        return isCogInit;
    }

    //release all resources
}
