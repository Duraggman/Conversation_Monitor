package com.example.convo_monitor;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import android.util.Log;
import android.widget.TextView;

public class VoskRTAudioProcessor implements AudioProcessor {
    private final VoskProvider vosk;
    private final TextView textView;

    public VoskRTAudioProcessor(VoskProvider vosk, TextView textView) {
        this.vosk = vosk;
        this.textView = textView;
    }

    @Override
    public boolean process(AudioEvent audioEvent) {
        float[] audioBuffer = audioEvent.getFloatBuffer();
        // Convert float buffer to byte array if necessary
        byte[] byteBuffer = floatToByte(audioBuffer);

        if (vosk.getRecognizer() != null) {
            if (vosk.getRecognizer().acceptWaveForm(byteBuffer, byteBuffer.length)) {
                String result = vosk.getUtils().jsonSpkToString(vosk.getRecognizer().getResult(), false, false);
                Log.i("Vosk", "Result - " + result);
                textView.post(() -> textView.setText(result));
            }
        }
        return true; // Return true to indicate that processing should continue
    }

    @Override
    public void processingFinished() {
        if (vosk.getRecognizer() != null) {
            String finalResult = vosk.getUtils().jsonSpkToString(vosk.getRecognizer().getFinalResult(), false, true);
            Log.i("Vosk", "Final Result - " + finalResult);
            textView.post(() -> textView.setText(finalResult));
        }
    }

    // Helper method to convert float array to byte array
    private byte[] floatToByte(float[] floatArray) {
        byte[] byteArray = new byte[floatArray.length * 2];
        for (int i = 0, j = 0; i < floatArray.length; i++, j += 2) {
            int intVal = (int)(floatArray[i] * 32768); // assuming the floats are in the range -1 to 1
            byteArray[j] = (byte)(intVal & 0xFF);
            byteArray[j+1] = (byte)((intVal >> 8) & 0xFF);
        }
        return byteArray;
    }
}

