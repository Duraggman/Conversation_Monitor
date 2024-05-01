package com.example.convo_monitor;

import static com.example.convo_monitor.AppUtils.checkAndRequestAudioPermissions;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import org.vosk.Recognizer;
import org.vosk.android.StorageService;
import org.vosk.Model;
import java.io.IOException;

public class VoskProvider {
    private Recognizer recognizer;
    private AppUtils utils;
    private Model vpModel;
    private final Context context;
    private final Activity activity;
    private AudioRecord recorder;
    private Boolean isRecording = false;
    private Boolean isCogInit = false;

    public VoskProvider(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        utils = new AppUtils();
        if(checkAndRequestAudioPermissions(activity, context)){
            initRecorder();
        }
        initModel();
    }

    @SuppressLint("MissingPermission")
    public void initRecorder(){
        // Initialize the AudioRecord object
        try {
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, utils.getSAMPLE_RATE(), utils.getCHANNEL(), utils.getFORMAT(), utils.getBufferSize());
            if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e("vp", "Audio Record can't initialize!");
                Log.e("vosk", "Audio Record can't initialize!");
            } else {
                Log.i("vp", "Recorder initialized successfully");
                Log.i("vosk", "Recorder initialized successfully");
            }
        } catch (Exception e) {
            Log.e("vp", "Failed to initialize the recorder" + e.getMessage());
            Log.e("vosk", "Failed to initialize the recorder" + e.getMessage());
        }
    }

    public void initModel(){
        // Load the Vosk model from the assets folder using the StorageService class from Vosk
        StorageService.unpack(this.context, "model-en-us", "model",
                (model) -> {
                    this.vpModel = model;
                    Log.i("vp", "Model unpacked successfully");
                    Log.i("vosk", "Model unpacked successfully");
                    initRecognizer();
                },
                (exception) -> {
                    Log.e("vp", "Failed to unpack the model" + exception.getMessage());
                    Log.e("vosk", "Failed to unpack the model" + exception.getMessage());
                });
    }

    public void initRecognizer(){
        try {
            recognizer = new Recognizer(this.vpModel, utils.getSAMPLE_RATE());
            Log.i("vp", "Recognizer initialized successfully");
            Log.i("vosk", "Recognizer initialized successfully");
            isCogInit = true;
        } catch (IOException e) {
            Log.e("vp", "Failed to initialize the recognizer" + e.getMessage());
            Log.e("vosk", "Failed to initialize the recognizer" + e.getMessage());
        }
    }

    public Recognizer getRecognizer(){
        return recognizer;}
    public AudioRecord getRecorder(){
        return recorder;
    }
    public Boolean isCogInit(){
        return isCogInit;
    }

    //release all resources
    public void close() {
        recognizer.close();
        recorder.release();
        Log.i("vp", "Resources released");
        Log.i("vosk", "Resources released");
    }
}
