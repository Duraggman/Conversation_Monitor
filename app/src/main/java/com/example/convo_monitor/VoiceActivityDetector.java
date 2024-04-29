package com.example.convo_monitor;

import android.content.Context;
import com.konovalov.vad.silero.Vad;
import com.konovalov.vad.silero.VadSilero;
import com.konovalov.vad.silero.config.FrameSize;
import com.konovalov.vad.silero.config.Mode;
import com.konovalov.vad.silero.config.SampleRate;

public class VoiceActivityDetector {
    private Context context;
    private VadSilero vad;

    // Create a new instance of the VAD
    public VoiceActivityDetector(Context context, byte[] audioData) {
        this.context = context;
         vad = Vad.builder()
                .setContext(this.context)
                .setSampleRate(SampleRate.SAMPLE_RATE_16K)
                .setFrameSize(FrameSize.FRAME_SIZE_512)
                .setMode(Mode.NORMAL)
                .setSilenceDurationMs(300)
                .setSpeechDurationMs(50)
                .build();
    }

    // Method to detect voice activity
    public boolean voiceDetected(byte[] audioData) {
        return vad.isSpeech(audioData);
    }

    // Method to release the VAD resources
    public void close() {
        vad.close();
    }
}
