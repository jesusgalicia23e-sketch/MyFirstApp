package com.example.myfirstapp

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myfirstapp.ui.theme.MyFirstAppTheme
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import java.nio.charset.StandardCharsets
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope(), DataClient.OnDataChangedListener, MessageClient.OnMessageReceivedListener,
CapabilityClient.OnCapabilityChangedListener{
    lateinit var conectar: Button
    val connect: Button = findViewById(R.id.connect)
    var activityContext: Context?=null
    private var deviceConnected: Boolean=false
    private val PAYLOAD_PATH = "/APP_OPEN"
    lateinit var nodeID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activityContext=this
        conectar=findViewById(R.id.button)

        connect.setOnClickListener {
            get("https://wwww.inegi.org.mx/app/api/indicadores/desarrolladores/jsonxml/INDICATOR/6207061361/es/00/true/BISE/2.0/508b1850-b896-8f7f-8d27-ab1f659e98e0?type=json")
        }

        conectar.setOnClickListener {
            if (!deviceConnected){
                val tempAct: Activity=activityContext as MainActivity
                getNodes(tempAct)
            }
        }
    }
    fun get(url: String){
        val client = OkHttpClient();
        val request = Request.Builder().url(url).build();

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("FETCH", "Respuesta: ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (response.isSuccessful) {
                        val responseData = response.body?.toString()
                        Log.d("FETCH", "Respuesta: $responseData")
                    } else {
                        Log.d("Fetch", "Error en la respuesta: ${response.code}")
                    }
                }
            }
        })
    }
    private fun getNodes(context: Context){
        launch(Dispatchers.Default){
            val nodeList= Wearable.getNodeClient(context).connectedNodes
            try {
                val  nodes= Tasks.await(nodeList)
                for (node in nodes){
                    Log.d("NODO", node.toString())
                    Log.d("NODO", "El id del nodo es:  ${node.id}")
                    nodeID=node.id
                    deviceConnected=true
                }
            } catch (exception: Exception){
                Log.d("ERROR en el nodo", exception.toString())
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

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyFirstAppTheme {
        Greeting("Android")
    }
}