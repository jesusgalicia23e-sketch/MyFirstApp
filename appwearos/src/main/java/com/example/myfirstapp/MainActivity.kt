
package com.example.myfirstapp

import com.example.myfirstapp.R
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.wearable.*
import kotlinx.coroutines.*
import java.io.IOException
import java.nio.charset.StandardCharsets
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope(), DataClient.OnDataChangedListener {

    private lateinit var connectButton: Button
    private lateinit var apiCallButton: Button
    private lateinit var accelerometerDataTextView: TextView
    private lateinit var gyroscopeDataTextView: TextView

    private var connectedNodeId: String? = null

    private val ACCELEROMETER_DATA_PATH = "/velocimeter"
    private val GYROSCOPE_DATA_PATH = "/gyroscope"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectButton = findViewById(R.id.button)
        apiCallButton = findViewById(R.id.connect)
        accelerometerDataTextView = findViewById(R.id.accelerometer_text)
        gyroscopeDataTextView = findViewById(R.id.gyroscope_text)

        connectButton.text = "Buscar Reloj"
        apiCallButton.text = "Llamar API"

        connectButton.setOnClickListener {
            findWearableNode()
        }

        apiCallButton.setOnClickListener {
            get("https://www.inegi.org.mx/app/api/indicadores/desarrolladores/jsonxml/INDICATOR/6207061361/es/00/true/BISE/2.0/508b1850-b896-8f7f-8d27-ab1f659e98e0?type=json")
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d("DataSync", "onDataChanged se ha disparado.")
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                dataItem.data?.let { data ->
                    when (dataItem.uri.path) {
                        ACCELEROMETER_DATA_PATH -> {
                            val magnitudeStr = String(data, StandardCharsets.UTF_8)
                            Log.d("DataSync", "Magnitud del acelerómetro recibida: $magnitudeStr")

                            val magnitudeFloat = magnitudeStr.toFloatOrNull() ?: 0.0f
                            val formattedMagnitude = "%.2f".format(magnitudeFloat)

                            runOnUiThread {
                                accelerometerDataTextView.text = "Caída (Magnitud): $formattedMagnitude"
                            }
                        }
                        GYROSCOPE_DATA_PATH -> {
                            val gyroData = String(data, StandardCharsets.UTF_8)
                            Log.d("DataSync", "Datos de Giroscopio recibidos: $gyroData")

                            runOnUiThread {
                                gyroscopeDataTextView.text = "Giroscopio (X,Y,Z): $gyroData"
                            }
                        }
                    }
                }
            }
        }
    }




    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(this).addListener(this)
        Log.d("DataSync", "Listener de DataClient registrado.")
    }

    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(this)
        Log.d("DataSync", "Listener de DataClient removido.")
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    private fun findWearableNode() {
        launch(Dispatchers.IO) {
            try {
                val nodes = Wearable.getNodeClient(applicationContext).connectedNodes.await()
                connectedNodeId = nodes.firstOrNull()?.id
                withContext(Dispatchers.Main) {
                    if (connectedNodeId != null) {
                        Log.d("NodeSearch", "Nodo encontrado: $connectedNodeId")
                        connectButton.text = "Reloj Conectado"
                        connectButton.isEnabled = false
                    } else {
                        Log.w("NodeSearch", "No se encontraron nodos de Wear OS conectados.")
                        connectButton.text = "Reintentar Conexión"
                    }
                }
            } catch (e: Exception) {
                Log.e("NodeSearch", "Error al buscar nodos", e)
            }
        }
    }

    fun get(url: String) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FETCH_API", "Fallo en la llamada a la API: ${e.message}", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                if (response.isSuccessful && responseData != null) {
                    Log.d("FETCH_API", "Respuesta de la API: $responseData")
                } else {
                    Log.e("FETCH_API", "Error en la respuesta de la API: ${response.code}")
                }
                response.close()
            }
        })
    }
}

suspend fun <T> Task<T>.await(): T = Tasks.await(this)

