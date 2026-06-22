package com.wordforge.common.tts;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class MockTextToSpeechService implements TextToSpeechService {
    @Override
    public SpeechAsset synthesize(String text, String language) {
        return new SpeechAsset(text, language, "webspeech", null);
    }
}
