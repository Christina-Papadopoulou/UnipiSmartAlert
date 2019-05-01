package com.papadopoulou.christina.unipismartalert;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

/**
 * Class for Text to Speech. Alepis class
 */
public class MyTts {
    private TextToSpeech tts;
    private TextToSpeech.OnInitListener initListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS)
                tts.setLanguage(Locale.ENGLISH);
        }
    };

    public MyTts(Context context) {
        tts = new TextToSpeech(context, initListener);
    }

    public void speak(String s) {
        for (int i = 0; i < 3; i++) {
            tts.speak(s, TextToSpeech.QUEUE_ADD, null, null);
        }
    }

    public void stop() {
        tts.stop();
        tts.shutdown();
    }
}
