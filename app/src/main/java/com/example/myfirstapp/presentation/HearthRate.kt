package com.example.myfirstapp.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.myfirstapp.R
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable

class HearthRate : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null
    private val sensorType = Sensor.TYPE_HEART_RATE

    private lateinit var heartRateTextView: TextView

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("PERMISOS", "Permiso BODY_SENSORS concedido.")
                // onResume se encargará de registrar el sensor
            } else {
                Log.d("PERMISOS", "Permiso BODY_SENSORS denegado.")
                heartRateTextView.text = "Permiso denegado"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hearth_rate)
        heartRateTextView = findViewById(R.id.ritmo_cardiaco)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(sensorType)

        if (heartRateSensor == null) {
            Log.e("SensorError", "El dispositivo no tiene sensor de ritmo cardíaco.")
            heartRateTextView.text = "No disponible"
        }
    }

    override fun onResume() {
        super.onResume()
        if (heartRateSensor == null) return

        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("Sensor", "Permiso concedido. Registrando listener.")
                sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.BODY_SENSORS)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == sensorType && event.values[0] > 0) {
            val heartRate = event.values[0]
            runOnUiThread {
                heartRateTextView.text = "${heartRate.toInt()} BPM"
            }
            val MAX_HR_THRESHOLD = 180
            if (heartRate > MAX_HR_THRESHOLD){
                Log.w("ALERTA", "Ritmo cardiaco alto: $heartRate")
            }
            val dataString = heartRate.toString()
            val dataBytes = dataString.toByteArray()

            sendSensorData("/heart_rate", dataBytes)
        }
        }

    private fun sendSensorData(path: String, data: ByteArray) {
        try {
            val putDataRequest = PutDataRequest.create(path).apply {
                setData(data)
                setUrgent()
            }

            val putDataTask = Wearable.getDataClient(this).putDataItem(putDataRequest)

            putDataTask.addOnSuccessListener {
                Log.d("DataTransfer", "Datos de giroscopio enviados con éxito: ${it.uri}")
            }
            putDataTask.addOnFailureListener { e ->
                Log.e("DataTransfer", "Error al enviar datos de giroscopio", e)
            }
        } catch (e: Exception) {
            Log.e("DataTransfer", "Excepción al intentar enviar datos", e)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
