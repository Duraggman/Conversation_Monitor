package com.example.convo_monitor;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
}

