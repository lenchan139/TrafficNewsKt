package org.lenchan139.trafficnews

import android.speech.tts.TextToSpeech
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView

import java.util.Locale

class TextToSpeechActivity : AppCompatActivity() {
    internal var txtReadText: TextView? = null
    internal var btnReadLoud: Button? =  null
    internal var tts: TextToSpeech? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_to_speech)
        txtReadText = findViewById(R.id.readText) as TextView
        btnReadLoud = findViewById(R.id.btnReadIt) as Button
        txtReadText!!.text = "香港有個大達地，好多鴨仔真肥美。圓碌碌又圓又碌，唯有阿媽食牛鹿。That is All!"
        tts = TextToSpeech(this@TextToSpeechActivity, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                tts!!.language = Locale.forLanguageTag("yue-Hant-HK")
            }
        })
        btnReadLoud!!.setOnClickListener {
            val t = tts!!.availableLanguages
            for (s in t) {
                Log.v("awe", s.toString())
            }
            tts!!.speak(txtReadText!!.text.toString(), TextToSpeech.QUEUE_ADD, null)
        }
    }

    override fun onPause() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onPause()
    }
}
