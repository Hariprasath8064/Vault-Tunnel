package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.example.myapplication.utils.initializer.AppInitializer
import com.example.myapplication.vpn.VPNService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MainActivity : Activity(),VPNService.OnStatusChangedListener {
    private val insensitiveTables = listOf("Logs", "Config","temp_keys")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppInitializer.initializeOnce(this, insensitiveTables)
        setContentView(R.layout.activity_main)
        VPNService.addOnStatusChangedListener(this)
        checkRunning()


        val connect: Button = findViewById(R.id.btnConnect)
        val test: Button = findViewById(R.id.button)

        connect.setOnClickListener {
            if (!VPNService.isRunning) {
                val intent = VPNService.prepare(this)
                if (intent != null) {
                    startActivityForResult(intent, 0)
                } else {
                    onActivityResult(0, RESULT_OK, null)
                }
            } else {

                Log.d("VPNControl", "Disconnect button clicked")
                val intent = Intent(this, VPNService::class.java).apply {
                    action = "STOP_VPN"
                }
                startService(intent)
            }
        }

        test.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val url = URL("https://www.reddit.com/")
                    val conn = url.openConnection() as HttpsURLConnection
                    conn.connectTimeout = 5000
                    conn.readTimeout = 5000
                    conn.requestMethod = "GET"
                    val response = conn.inputStream.bufferedReader().use { it.readText() }

                    Log.d("TestRequest", "Response received: ${response.take(100)}")
                } catch (e: Exception) {
                    Log.e("TestRequest", "Error fetching file", e)
                }
            }
        }


    }
    override fun onActivityResult(request: Int, result: Int, data: Intent?) {
        if (result == RESULT_OK) {
            val intent = Intent(this, VPNService::class.java)
            startService(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        checkRunning()
    }

    override fun onStatusChanged(status: Boolean) {
        val connect: Button? = findViewById(R.id.btnConnect)
        connect?.post {
            connect.text = if (status) "Disconnect" else "Connect"
        }
    }

    private fun checkRunning() {
        val status = VPNService.isRunning
        val connect: Button = findViewById(R.id.btnConnect)
        connect.text = if (status) "Disconnect" else "Connect"
    }
}
