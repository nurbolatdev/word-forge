package com.wordforge.common.tts;

public interface TextToSpeechService {
    SpeechAsset synthesize(String text, String language);
}
