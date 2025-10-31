package com.example.myfirstapp.presentation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.myfirstapp.R
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable

class Gyroscope : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var gyroscopeSensor: Sensor? = null
    private val sensorType = Sensor.TYPE_GYROSCOPE
    private lateinit var gyroscopeTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gyroscope)
        gyroscopeTextView = findViewById(R.id.gyr)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyroscopeSensor = sensorManager.getDefaultSensor(sensorType)

        if (gyroscopeSensor == null) {
            Log.e("SensorError", "El dispositivo no cuenta con giroscopio.")
            gyroscopeTextView.text = "Sensor no disponible"
        }
    }

    override fun onResume() {
        super.onResume()
        gyroscopeSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == sensorType) {
            val xAxis = event.values[0]
            val yAxis = event.values[1]
            val zAxis = event.values[2]

            runOnUiThread {
                gyroscopeTextView.text = "X: %.2f rad/s\nY: %.2f rad/s\nZ: %.2f rad/s".format(xAxis, yAxis, zAxis)
            }

            val dataString = "$xAxis,$yAxis,$zAxis"
            val dataBytes = dataString.toByteArray()

            sendSensorData("/gyroscope", dataBytes)
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
