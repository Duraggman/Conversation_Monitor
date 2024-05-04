package com.example.convo_monitor;

import static com.example.convo_monitor.AppUtils.checkAndRequestAudioPermissions;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import org.vosk.Recognizer;
import org.vosk.SpeakerModel;
import org.vosk.android.StorageService;
import org.vosk.Model;
import java.io.IOException;
import android.content.res.AssetManager;

public class VoskProvider {
    private Recognizer recognizer;
    private AudioRecord recorder;
    private Model vpModel;
    private SpeakerModel idModel;
    private final AppUtils utils;

    private final Context context;
    private Boolean isCogInit = false;

    public VoskProvider(Context context, Activity activity, AppUtils utils ) {
        this.utils = utils;
        this.context = context;
        if(checkAndRequestAudioPermissions(activity, context)){
            initRecorder();
        }
        initIDModel();
    }

    @SuppressLint("MissingPermission")
    private void initRecorder(){
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

    private void initRCModel(){
        // Load the Vosk model from the assets folder using the StorageService class from Vosk
        StorageService.unpack(this.context, "model-en-us", "model-en",
                (model) -> {
                    this.vpModel = model;
                    Log.i("vp", "rec Model unpacked successfully");
                    Log.i("vosk", "Model unpacked successfully");
                },
                (exception) -> {
                    Log.e("vp", "Failed to unpack the rec model" + exception.getMessage());
                    Log.e("vosk", "Failed to unpack the rec model" + exception.getMessage());
                });
    }
    private void initIDModel() {
        AssetManager assetManager = this.context.getAssets();
        try {
            this.idModel = new SpeakerModel("assets/model-spk");
        } catch (IOException e) {
            Log.e("vp", "Failed to initialize the ID model " + e.getMessage());
            Log.e("vosk", "Failed to initialize the ID model " + e.getMessage());
        }
    }

    private void initRecognizer(){
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

    public Recognizer getRecognizer(){return recognizer;}
    public AudioRecord getRecorder(){
        return recorder;
    }
    public Boolean isCogInit(){
        return isCogInit;
    }

    //release all resources
    public void close() {
        try{
        if (recorder != null && recorder.getState() == AudioRecord.STATE_INITIALIZED) {
            recorder.stop();
            recorder.release();
        }
        if (recognizer != null) {
        recognizer.close();
        }
        if (vpModel != null) {
            vpModel.close();
        }
        } catch (Exception e) {
            Log.e("vp", "Failed to release resources" + e.getMessage());
            Log.e("vosk", "Failed to release resources" + e.getMessage());
        }
    }
    // Release the resources used by the AudioRecorder
}
