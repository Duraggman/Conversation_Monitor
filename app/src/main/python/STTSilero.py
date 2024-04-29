import torch
import time
from queue import Queue
from silero import silero_stt as stt

class STTSilero:
    def __init__(self):
        self.device = torch.device("cpu")
        self.transcriber = stt.SileroSTT(language="en_v6")
        self.audio_buffer = Queue()
        self.transcribing = False

    def buffer_audio(self, audio_chunk):
        """Buffer incoming audio chunks."""
        self.audio_buffer.put(audio_chunk)

    def start_transcription(self):
        """Start the transcription process."""
        self.transcribing = True
        while self.transcribing:
            if not self.audio_buffer.empty():
                audio_data = self.audio_buffer.get()
                text = self.transcribe_audio(audio_data)
                print(text)  # Or do something with the transcribed text
            else:
                # Add some sleep to avoid busy-waiting
                time.sleep(0.1)

    def stop_transcription(self):
        """Stop the transcription process."""
        self.transcribing = False

    def transcribe_audio(self, audio_data):
        """Transcribe a single audio chunk."""
        text = self.transcriber.transcribe(audio_data, self.device)
        return text
    
    def close(self):
        """Close the transcriber."""
        self.transcriber.close()
