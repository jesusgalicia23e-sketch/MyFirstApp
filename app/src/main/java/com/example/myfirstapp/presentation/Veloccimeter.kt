package com.example.myfirstapp.presentation

import com.example.myfirstapp.R
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class Veloccimeter: ComponentActivity(), SensorEventListener, DataClient.OnDataChangedListener, MessageClient.OnMessageReceivedListener, CapabilityClient.OnCapabilityChangedListener, CoroutineScope by MainScope() {

    private lateinit var sensorManager: SensorManager
    private var velocimeter: Sensor? = null
    private val sensorType = Sensor.TYPE_ACCELEROMETER
    var activityContext: Context?= null
    private val PAYLOAD_PATH = "/APP_OPEN"
    lateinit var nodeId: String
    private lateinit var veloccimeterTextView: TextView

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted){
                Log.d("PERMISOS", "Permiso BODY_SENSORS otorgado")
            } else {
                Log.d("PERMISOS", "Permiso BODY_SENSORS denegado")
                veloccimeterTextView.text = "Permiso Denegado"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.veloccimeter)
        activityContext = this

        veloccimeterTextView = findViewById(R.id.vel)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        velocimeter = sensorManager.getDefaultSensor(sensorType)

        if (velocimeter == null){
            Log.e("SensorError", "El dispositivo no cuenta con este sensor")
            veloccimeterTextView.text = "Sensor no disponible"
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onSensorChanged(p0: SensorEvent?) {
    }

    override fun onDataChanged(p0: DataEventBuffer) {
    }

    override fun onMessageReceived(p0: MessageEvent) {
    }

    override fun onCapabilityChanged(p0: CapabilityInfo) {
        TODO("Not yet implemented")
    }

}