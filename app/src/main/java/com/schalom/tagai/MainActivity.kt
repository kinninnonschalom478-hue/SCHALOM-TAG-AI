package com.schalom.tagai

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.schalom.tagai.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityMainBinding
    private var textToSpeech: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTorchOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateHeaderDate()
        textToSpeech = TextToSpeech(this, this)
        initSpeechRecognizer()
        setupDailyMorningAlarm()
        setupAllButtons()
    }

    private fun updateHeaderDate() {
        val sdf = SimpleDateFormat("EEE. dd MMM. yyyy", Locale.FRENCH)
        val currentDate = sdf.format(Date())
        binding.tvDateHeader.text = currentDate
        binding.tvLocationHeader.text = "28°C • Abomey-Calavi / Cotonou"
    }

    private fun setupAllButtons() {
        binding.btnAppels.setOnClickListener { openApp(Intent.ACTION_DIAL) }
        binding.btnMessages.setOnClickListener { openApp(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_APP_MESSAGES) } }
        binding.btnWhatsapp.setOnClickListener { launchPackage("com.whatsapp") }
        binding.btnChrome.setOnClickListener { launchPackage("com.android.chrome") }
        binding.btnCamera.setOnClickListener { openApp(MediaStore.ACTION_IMAGE_CAPTURE) }
        binding.btnGalerie.setOnClickListener { launchPackage("com.google.android.apps.photos") }
        binding.btnPlayStore.setOnClickListener { launchPackage("com.android.vending") }
        binding.btnParametres.setOnClickListener { startActivity(Intent(android.provider.Settings.ACTION_SETTINGS)) }

        binding.btnIaGlory.setOnClickListener { openAiDialog("Salut ! Je suis Glory IA. Comment puis-je t'aider dans tes cours de 2nde F3 aujourd'hui ?") }
        binding.btnTraduction.setOnClickListener { openAiDialog("Mode Traduction prêt. Saisis ou dis la phrase à traduire.") }
        binding.btnScanner.setOnClickListener { openApp(MediaStore.ACTION_IMAGE_CAPTURE) }
        binding.btnCalculatrice.setOnClickListener { openAppCategory(Intent.CATEGORY_APP_CALCULATOR) }
        binding.btnNotes.setOnClickListener { launchPackage("com.google.android.keep") }
        binding.btnLampe.setOnClickListener { toggleFlashlight() }
        binding.btnWifi.setOnClickListener { startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)) }
        binding.btnBluetooth.setOnClickListener { startActivity(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)) }

        binding.btnDockCentralG.setOnClickListener { toggleAiVoicePanel() }
        binding.btnMicListen.setOnClickListener { startListeningVoice() }
        binding.btnAideDevoirs.setOnClickListener {
            processUserQuery("Explique-moi les bases des schémas électriques en 2nde F3 (Lois de Kirchhoff, Schéma unifilaire et multifilaire).")
        }
    }

    private fun processUserQuery(query: String) {
        binding.tvAiDialogStatus.text = "Glory IA réfléchit..."

        CoroutineScope(Dispatchers.IO).launch {
            val response = getF3OfflineKnowledge(query)
            withContext(Dispatchers.Main) {
                binding.tvAiResponse.text = response
                binding.tvAiDialogStatus.text = "Glory IA - Prêt"
                speak(response)
            }
        }
    }

    private fun getF3OfflineKnowledge(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("schéma") || q.contains("électrique") -> 
                "En 2nde F3, un schéma électrique représente un circuit avec des symboles normalisés. Souviens-toi des composants clés : Disjoncteur, Interrupteur simple/double allumage, et contacteurs !"
            q.contains("loi") || q.contains("ohm") -> 
                "La loi d'Ohm fondamentale est : U = R x I. U en Volts (V), R en Ohms (Ω), et I en Ampères (A)."
            q.contains("programme") || q.contains("cours") -> 
                "Le programme de 2nde F3 inclut : Électrotechnique, Schémas Électriques, Construction Mécanique, Physique-Chimie, Mathématiques et Travaux Pratiques."
            else -> "Je suis Glory IA, prêt à t'aider pour toutes tes matières de 2nde F3. Pose-moi une question sur tes cours !"
        }
    }

    private fun toggleAiVoicePanel() {
        if (binding.aiVoiceOverlay.visibility == View.VISIBLE) {
            binding.aiVoiceOverlay.visibility = View.GONE
        } else {
            binding.aiVoiceOverlay.visibility = View.VISIBLE
            speak("Salut ! Je suis Glory IA. Je t'écoute.")
        }
    }

    private fun startListeningVoice() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.FRENCH.language)
        }
        speechRecognizer?.startListening(intent)
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        binding.tvAiVoicePrompt.text = "Je t'écoute... Parle simplement, je suis là."
                    }
                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val userText = matches[0]
                            binding.tvAiUserQuery.text = userText
                            processUserQuery(userText)
                        }
                    }
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onError(error: Int) {}
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    private fun toggleFlashlight() {
        try {
            val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            isTorchOn = !isTorchOn
            cameraManager.setTorchMode(cameraId, isTorchOn)
            Toast.makeText(this, if (isTorchOn) "Lampe allumée" else "Lampe éteinte", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Erreur contrôle lampe", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupDailyMorningAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, MorningEncouragementReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 6)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun launchPackage(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            Toast.makeText(this, "Application non installée", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openApp(action: String) {
        try {
            startActivity(Intent(action))
        } catch (e: Exception) {
            Toast.makeText(this, "Impossible d'ouvrir l'application", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAppCategory(category: String) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(category) }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Application introuvable", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAiDialog(initialMessage: String) {
        toggleAiVoicePanel()
        binding.tvAiResponse.text = initialMessage
        speak(initialMessage)
    }

    private fun speak(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "GLORY_TTS")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.language = Locale.FRENCH
        }
    }

    override fun onDestroy() {
        textToSpeech?.shutdown()
        speechRecognizer?.destroy()
        super.onDestroy()
    }
}
