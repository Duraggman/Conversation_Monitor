package com.example.convo_monitor;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;

import java.util.HashMap;

public class ConvoBandwidthCalc implements AudioProcessor {
    private HashMap<String, Long> speakerTimes = new HashMap<>(); // Stores total speaking time per speaker
    private HashMap<String, Long> startTimes = new HashMap<>(); // Stores start time of speaking per speaker
    private String currentSpeaker = ""; // Tracks the currently speaking speaker ID

    // Method to call when a speaker starts speaking
    public void startSpeaking(String speakerId) {
        startTimes.put(speakerId, System.currentTimeMillis()); // Log start time
        currentSpeaker = speakerId; // Set current speaker
    }

    // Method to call when a speaker stops speaking
    public void stopSpeaking(String speakerId) {
        Long startTime = startTimes.getOrDefault(speakerId, System.currentTimeMillis());
        Long totalSpeakingTime = speakerTimes.getOrDefault(speakerId, 0L);
        totalSpeakingTime += System.currentTimeMillis() - startTime; // Accumulate speaking time
        speakerTimes.put(speakerId, totalSpeakingTime); // Update speaker time
        currentSpeaker = ""; // Reset current speaker
    }

    // Retrieves total speaking time for a given speaker
    public Long getSpeakingTime(String speakerId) {
        return speakerTimes.getOrDefault(speakerId, 0L); // Return speaking time
    }

    @Override
    // Process audio events to manage speaking times
    public boolean process(AudioEvent audioEvent) {
        if (!currentSpeaker.isEmpty()) {
            // Logic to detect non-speaking could trigger stopSpeaking
            stopSpeaking(currentSpeaker);
        }
        return true; // Continue processing
    }

    @Override
    // Called when audio processing is finished
    public void processingFinished() {
        System.out.println("Finished processing audio for bandwidth measurement."); // Notification of end processing
    }
}
