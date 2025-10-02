package com.example.myfirstapp.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle;
import androidx.activity.ComponentActivity;
import androidx.core.app.ActivityCompat
import com.example.myfirstapp.R;


abstract class OtraVentana: ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager:SensorManager
    private var sensor: Sensor?=null
    private var sensorType = Sensor.TYPE_ACCELEROMETER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager=getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor= sensorManager.getDefaultSensor(sensorType)
        setContentView(R.layout.otra_ventana)
    }

    private fun startSensor(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BODY_SENSORS), 1001)
        }

        if (sensor!=null){
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onSensorChanged(p0: SensorEvent?) {

    }
}