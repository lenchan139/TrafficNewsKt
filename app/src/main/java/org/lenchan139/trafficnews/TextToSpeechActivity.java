package org.lenchan139.trafficnews;

import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;
import java.util.Set;

public class TextToSpeechActivity extends AppCompatActivity {
    TextView txtReadText;
    Button btnReadLoud;
    TextToSpeech tts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_to_speech);
        txtReadText = (TextView) findViewById(R.id.readText);
        btnReadLoud = (Button) findViewById(R.id.btnReadIt);
        txtReadText.setText("香港有個大達地，好多鴨仔真肥美。圓碌碌又圓又碌，唯有阿媽食牛鹿。That is All!");
        tts = new TextToSpeech(TextToSpeechActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.forLanguageTag("yue-Hant-HK"));
                }
            }
        });
        btnReadLoud.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Set<Locale> t = tts.getAvailableLanguages();
                for (Locale s : t) {
                    Log.v("awe",s.toString());
                }
                tts.speak(txtReadText.getText().toString(), TextToSpeech.QUEUE_ADD, null);
            }
        });
    }

    @Override
    protected void onPause() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }
}
