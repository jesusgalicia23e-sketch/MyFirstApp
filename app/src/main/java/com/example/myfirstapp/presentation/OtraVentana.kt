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

class OtraVentana : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null
    private val sensorType = Sensor.TYPE_HEART_RATE

    private lateinit var heartRateTextView: TextView

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("PERMISOS", "Permiso BODY_SENSORS concedido.")
                // Si se concede el permiso, onResume() se encargará de registrar el sensor.
                // No es necesario llamar a startSensor() aquí directamente para evitar duplicados.
            } else {
                Log.d("PERMISOS", "Permiso BODY_SENSORS denegado.")
                heartRateTextView.text = "Permiso denegado"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.otra_ventana)

        heartRateTextView = findViewById(R.id.ritmo_cardiaco)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(sensorType)

        if (heartRateSensor == null) {
            Log.e("SensorError", "El dispositivo no tiene sensor de ritmo cardíaco.")
            heartRateTextView.text = "No disponible"
        }
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

    override fun onResume() {
        super.onResume()

        if (heartRateSensor == null) {
            return
        }

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BODY_SENSORS
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("Sensor", "El permiso ya estaba concedido. Registrando listener.")
                startSensor()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.BODY_SENSORS)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        Log.d("Sensor", "Listener del sensor des-registrado.")
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
}
