package com.example.convo_monitor;



import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.StorageService;

import static com.example.convo_monitor.MainActivity.CHANNEL;
import static com.example.convo_monitor.MainActivity.FORMAT;
import static com.example.convo_monitor.MainActivity.PERMISSIONS_REQUEST_RECORD_AUDIO;
import static com.example.convo_monitor.MainActivity.SAMPLE_RATE;
import static com.example.convo_monitor.MainActivity.audioBuffer;


import java.io.IOException;

// TODO: Implement real-time diarization using TarsosDSP
public class SpeakerDiarization{
    @SuppressLint("MissingPermission")
    public SpeakerDiarization(Context context, Activity activity, VoskProvider vosk){
    }

}
