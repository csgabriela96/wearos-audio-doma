/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.doma.presentation

import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import com.example.doma.AudioHelper
import java.util.Locale
import android.content.Intent
import android.provider.Settings
import android.media.MediaPlayer
import com.example.doma.R

class MainActivity : ComponentActivity() {

    private lateinit var audioManager: AudioManager
    private lateinit var audioHelper: AudioHelper
    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioHelper = AudioHelper(this)

        inicializarTTS()
        verificarDispositivos()
        monitorarDispositivos()
    }
    private fun abrirConfiguracoesBluetooth() {

        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK)

            putExtra("EXTRA_CONNECTION_ONLY", true)
            putExtra("EXTRA_CLOSE_ON_CONNECT", true)
            putExtra("android.bluetooth.devicepicker.extra.FILTER_TYPE", 1)
        }

        startActivity(intent)
    }
    private fun inicializarTTS() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale("pt", "BR")

                falar("Sistema iniciado com sucesso", TextToSpeech.QUEUE_FLUSH)
                falar("Atenção. Alerta de emergência ativado.", TextToSpeech.QUEUE_ADD)
            }
        }
    }

    private fun falar(texto: String, fila: Int = TextToSpeech.QUEUE_ADD) {
        tts.speak(texto, fila, null, null)
    }
    private fun tocarSirene() {
        val mediaPlayer = MediaPlayer.create(this, R.raw.sirene)
        mediaPlayer?.start()
    }
    private fun alertaEmergencia() {
        tocarSirene()
        falar("Atenção. Alerta de emergência ativado.")
    }
    private fun verificarDispositivos() {

        val speaker =
            audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)

        val bluetooth =
            audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)

        if (bluetooth) {
            falar("Fone Bluetooth conectado")
        } else {
            falar("Nenhum fone Bluetooth conectado. Abrindo configurações.")
            abrirConfiguracoesBluetooth()
        }
    }

    private fun monitorarDispositivos() {

        audioManager.registerAudioDeviceCallback(object : AudioDeviceCallback() {

            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {

                if (audioHelper.audioOutputAvailable(
                        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                    )
                ) {
                    falar("Fone Bluetooth conectado")
                }
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {

                if (!audioHelper.audioOutputAvailable(
                        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                    )
                ) {
                    falar("Fone Bluetooth desconectado")
                }
            }

        }, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
    }
}