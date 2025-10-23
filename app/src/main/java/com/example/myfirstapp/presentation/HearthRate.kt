package com.example.myfirstapp.presentation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.myfirstapp.R
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.nio.charset.StandardCharsets

class HearthRate : ComponentActivity(), SensorEventListener, DataClient.OnDataChangedListener, MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener, CoroutineScope by MainScope() {

    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null
    private val sensorType = Sensor.TYPE_HEART_RATE
    var activityContext: Context?=null
    private val PAYLOAD_PATH = "/APP_OPEN"
    lateinit var nodeID: String

    private lateinit var heartRateTextView: TextView

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("PERMISOS", "Permiso BODY_SENSORS concedido.")
            } else {
                Log.d("PERMISOS", "Permiso BODY_SENSORS denegado.")
                heartRateTextView.text = "Permiso denegado"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hearth_rate)
        activityContext=this

        heartRateTextView = findViewById(R.id.ritmo_cardiaco)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(sensorType)

        if (heartRateSensor == null) {
            Log.e("SensorError", "El dispositivo no tiene sensor de ritmo cardíaco.")
            heartRateTextView.text = "No disponible"
        }
    }

    private fun sendMessage() {
        val sendMessageResult = Wearable.getMessageClient(activityContext!!)
            .sendMessage(nodeID, PAYLOAD_PATH, "hola mundo".toByteArray())
            .addOnSuccessListener { Log.d("sendMessage", "Mensaje enviado correctamente") }
            .addOnFailureListener { exception -> Log.d("sendMessage", "Error al enviar el mensaje ${exception.toString()}") }
    }

    private fun startSensor() {
        heartRateSensor?.let { sensor ->
            val isRegistered = sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            if (!isRegistered) {
                Log.e("SensorError", "No se pudo registrar el listener para el sensor.")
            } else {
                Log.d("Sensor", "Listener del sensor registrado correctamente.")
            }
        }
    }


    override fun onPause() {
        super.onPause()
        try {
            Wearable.getDataClient(activityContext!!).removeListener(this)
            Wearable.getMessageClient(activityContext!!).removeListener(this)
            Wearable.getCapabilityClient(activityContext!!).removeListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            Wearable.getDataClient(activityContext!!).addListener(this)
            Wearable.getMessageClient(activityContext!!).addListener(this)
            Wearable.getCapabilityClient(activityContext!!).addListener(this, Uri.parse("wear://"),
                CapabilityClient.FILTER_REACHABLE)
        }catch (e: Exception){

        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == sensorType) {
            val heartRate = event.values[0]
            if (heartRate > 0) {
                Log.d("onSensorChanged", "Lectura del sensor: $heartRate")
                heartRateTextView.text = "${heartRate.toInt()} BPM"
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d("SensorAccuracy", "Precisión del sensor cambió a: $accuracy")
    }

    override fun onDataChanged(p0: DataEventBuffer) {
    }

    override fun onMessageReceived(ME: MessageEvent) {
        Log.d("onMessageReceived", ME.toString())
        Log.d("onMessageReceived", "ID del nodo: ${ME.sourceNodeId}")
        Log.d("onMessageReceived", "Payload: ${ME.path}")
        val message=String(ME.data, StandardCharsets.UTF_8)
        Log.d("onMessageReceived", "Mensaje: ${message}")
    }

    override fun onCapabilityChanged(p0: CapabilityInfo) {
    }
}
