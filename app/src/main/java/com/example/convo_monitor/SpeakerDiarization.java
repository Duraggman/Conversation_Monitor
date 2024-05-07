package com.example.convo_monitor;


import java.util.Objects;

public class SpeakerDiarization{
    private ParticipantManager pm;


    public String compareSpeaker(double[] incomingVector) {
        double maxSimilarity = -1; // Start with the lowest possible similarity
        String bestMatchParticipantName = null; // Name of the best matching participant

        for (ParticipantManager.Participant p : this.pm.participants) {
            double similarity = cosineSimilarity(incomingVector, p.refVector());
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                bestMatchParticipantName = p.tagText(); // Get the tag of most similar participant
            }
        }
        return Objects.requireNonNullElse(bestMatchParticipantName, "No match found");
    }

    public double cosineSimilarity(double[] vectorA, double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
