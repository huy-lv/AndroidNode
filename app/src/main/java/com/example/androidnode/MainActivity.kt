package com.example.androidnode

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.androidnode.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    var _startedNodeAlready = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.res = "22"
        binding.button1.setOnClickListener {
            Log.e("cxz","cxzcxzczx")
            GlobalScope.launch(Dispatchers.Main) {
                binding.res = fetchUser()
            }
        }

        if (!_startedNodeAlready) {
            _startedNodeAlready = true
            Thread {
                startNodeWithArguments(
                    arrayOf<String>(
                        "node", "-e",
                        "var http = require('http'); " +
                                "var versions_server = http.createServer( (request, response) => { " +
                                "  response.end('Versions: ' + JSON.stringify(process.versions)); " +
                                "}); " +
                                "versions_server.listen(3000);"
                    )
                )
            }.start()
        }
    }

    private suspend fun fetchUser(): String {
        return GlobalScope.async(Dispatchers.IO) {
            var nodeResponse = ""
            try {
                val localNodeServer = URL("http://localhost:3000/")
                val inputStream = BufferedReader(InputStreamReader(localNodeServer.openStream()))
                inputStream.forEachLine {
                    nodeResponse += it
                }
                inputStream.close()
            } catch (ex: Exception) {
                nodeResponse = ex.toString()
            }
            return@async nodeResponse
        }.await()
    }

    /**
     * A native method that is implemented by the 'androidnode' native library,
     * which is packaged with this application.
     */
    external fun startNodeWithArguments(arguments: Array<String>): Int?

    companion object {
        // Used to load the 'androidnode' library on application startup.
        init {
            System.loadLibrary("androidnode")
            System.loadLibrary("node");
        }
    }
}
