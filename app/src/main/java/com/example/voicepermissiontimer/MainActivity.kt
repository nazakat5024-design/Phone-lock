```kotlin
package com.example.voicepermissiontimer

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private var speechRecognizer: SpeechRecognizer? = null
    private var timer: CountDownTimer? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 80, 40, 40)
        }

        statusText = TextView(this).apply {
            text = "اجازت دینے کے لیے کہیں: اجازت دو"
            textSize = 22f
        }

        val button = Button(this).apply {
            text = "آواز سنیں"
            setOnClickListener {
                startListening()
            }
        }

        layout.addView(statusText)
        layout.addView(button)

        setContentView(layout)

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                100
            )
        }
    }

    private fun startListening() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            statusText.text = "اس فون پر Voice Recognition دستیاب نہیں۔"
            return
        }

        timer?.cancel()
        speechRecognizer?.destroy()

        speechRecognizer =
            SpeechRecognizer.createSpeechRecognizer(this)

        speechRecognizer?.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(params: Bundle?) {
                    statusText.text = "میں سن رہا ہوں... ابھی کہیں: اجازت دو"
                }

                override fun onBeginningOfSpeech() {
                    statusText.text = "آواز سن رہا ہوں..."
                }

                override fun onResults(results: Bundle?) {

                    val list = results?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )

                    val text =
                        list?.firstOrNull()
                            ?.lowercase(Locale.getDefault())
                            ?: ""

                    if (
                        text.contains("اجازت دو") ||
                        text.contains("اجازت ہے") ||
                        text.contains("اجازت") ||
                        text.contains("allow") ||
                        text.contains("yes")
                    ) {
                        timer?.cancel()
                        statusText.text =
                            "اجازت مل گئی۔ موبائل استعمال کیا جا سکتا ہے۔"
                    } else {
                        statusText.text =
                            "بات سمجھ نہیں آئی۔ دوبارہ کہیں: اجازت دو"

                        restartListening()
                    }
                }

                override fun onError(error: Int) {
                    statusText.text =
                        "آواز واضح نہیں سنی گئی۔ دوبارہ کوشش کریں..."

                    restartListening()
                }

                override fun onEndOfSpeech() {
                    statusText.text = "آواز مکمل ہوئی، سمجھ رہا ہوں..."
                }

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onPartialResults(
                    partialResults: Bundle?
                ) {}

                override fun onEvent(
                    eventType: Int,
                    params: Bundle?
                ) {}
            }
        )

        val intent =
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {

                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )

                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    "ur-PK"
                )

                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                    "ur-PK"
                )

                putExtra(
                    RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                    true
                )

                putExtra(
                    RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,
                    10000L
                )

                putExtra(
                    RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                    10000L
                )

                putExtra(
                    RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                    10000L
                )
            }

        speechRecognizer?.startListening(intent)
    }

    private fun restartListening() {

        handler.removeCallbacksAndMessages(null)

        handler.postDelayed({
            startListening()
        }, 1000)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        timer?.cancel()
        speechRecognizer?.destroy()
        super.onDestroy()
    }
}
```
