package com.example.convo_monitor;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
public class AppUtils {
    private int SAMPLE_RATE = 16000;
    private byte[] audioBuffer = new byte[SAMPLE_RATE];
    public static int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    // Use mono channel for microphone input
    private int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    // Define audio format as 16-bit PCM
     int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private int BufferSize = audioBuffer.length;

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
    public int getPERMISSIONS_REQUEST_RECORD_AUDIO() {
        return PERMISSIONS_REQUEST_RECORD_AUDIO;
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

}

