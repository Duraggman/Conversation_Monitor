package com.example.convo_monitor;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;

public class AudioProcessing {
    private AudioDispatcher dispatcher;
    private Thread audioThread;
    private final VolumeMeter volumeMeter;

    public AudioProcessing() {
        // Setup the AudioDispatcher from the default microphone with sample rate, audio buffer size, and buffer overlap
        //dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(16000, 16000, 0);

        // Initialize the volume meter
        volumeMeter = new VolumeMeter();

        // Add the volume meter to the dispatcher's chain of processors
        dispatcher.addAudioProcessor(volumeMeter);
    }

    // Start the audio processing
    public void start() {
        audioThread = new Thread(dispatcher, "Audio Thread");
        audioThread.start();
    }

    // Stop the audio processing
    public void stop() {
        if (dispatcher != null) {
            dispatcher.stop();
        }
        if (audioThread != null) {
            audioThread.interrupt();
        }
    }

    // Accessor method for the current volume
    public double getCurrentVolume() {
        return volumeMeter.getRMS();
    }

    // Custom processor that measures volume
    private static class VolumeMeter implements AudioProcessor {

        private double rms = 0; // Root Mean Square (RMS) value for volume

        @Override
        public boolean process(AudioEvent audioEvent) {
            // Compute the RMS value for the current audio buffer.
            float[] audioBuffer = audioEvent.getFloatBuffer();
            double sum = 0;
            for (int i = 0; i < audioBuffer.length; i++) {
                sum += audioBuffer[i] * audioBuffer[i];
            }
            rms = Math.sqrt(sum / audioBuffer.length);
            // The RMS value is now updated and can be retrieved with getRMS().
            return true;
        }

        @Override
        public void processingFinished() {
            // Any cleanup when processing is finished can be done here.
        }

        // Getter for the RMS value
        public double getRMS() {
            return rms;
        }
    }
}
