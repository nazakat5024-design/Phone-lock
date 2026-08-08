package com.example.voicepermissiontimer

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 80, 40, 40)
        }

        statusText = TextView(this).apply {
            text = "اجازت دینے کے لیے آواز میں کہیں: اجازت دو"
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
            != PackageManager.PERMISSION_GRANTED) {
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

        speechRecognizer?.destroy()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {

            override fun onReadyForSpeech(params: Bundle?) {
                statusText.text = "میں سن رہا ہوں..."
            }

            override fun onResults(results: Bundle?) {

                val list = results?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                )

                val text = list?.firstOrNull()?.lowercase(Locale.getDefault()) ?: ""

                if (
                    text.contains("اجازت دو") ||
                    text.contains("اجازت ہے") ||
                    text.contains("allow") ||
                    text.contains("yes")
                ) {
                    timer?.cancel()
                    statusText.text = "اجازت مل گئی۔ موبائل استعمال کیا جا سکتا ہے۔"
                } else {
                    statusText.text =
                        "اجازت نہیں ملی۔ 1 منٹ بعد یہ ایپ بند ہو جائے گی۔"

                    startOneMinuteTimer()
                }
            }

            override fun onError(error: Int) {
                statusText.text =
                    "آواز سمجھ نہیں آئی۔ 1 منٹ کا timer شروع ہو گیا ہے۔"

                startOneMinuteTimer()
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
            )
        }

        speechRecognizer?.startListening(intent)
    }

    private fun startOneMinuteTimer() {

        timer?.cancel()

        timer = object : CountDownTimer(60000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                statusText.text =
                    "اجازت نہیں ملی۔ باقی وقت: $seconds سیکنڈ"
            }

            override fun onFinish() {
                statusText.text = "وقت ختم ہو گیا۔"
                finish()
            }

        }.start()
    }

    override fun onDestroy() {
        timer?.cancel()
        speechRecognizer?.destroy()
        super.onDestroy()
    }
}
