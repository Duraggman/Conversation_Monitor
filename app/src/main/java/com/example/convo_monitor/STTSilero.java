/**
package com.example.convo_monitor;

import android.app.Activity;
import android.content.Context;
import android.media.AudioRecord;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class STTSilero extends VoskTranscriber {
    private PyObject stt;
    private Python py;
    // Threshold for silence detection. Adjust based on your environment.
    private static final double SILENCE_THRESHOLD = -70.0;

    private Context context; // Context is necessary for file and permission management
    private TextView textview; // TextView
    private Activity activity;
    private AudioRecord recorder;
    private Boolean isRecording = false;

    public STTSilero(Context context, TextView transcribedText, Activity activity) {
        super(context, transcribedText, activity);
        byte[] audioBuffer = new byte[minBufferSize];
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(context));
        }
        this.py = Python.getInstance();
        this.stt = py.getModule("stt");

    }

    public void startRecording() {
        if (recorder.getState() == AudioRecord.STATE_INITIALIZED && !isRecording) {
            recorder.startRecording();
            isRecording = true;

        }
    }

    public void startTranscribing(byte[] audioData){
            PyObject result = stt.callAttr("start_transcription", audioData);
        }

    public void stopTranscribing() {
        stt.callAttr("stop_transcription");
    }

    public void close() {
        stt.callAttr("close");
    }
}

**/