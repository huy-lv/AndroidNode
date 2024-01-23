package com.example.androidnode

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.eclipsesource.v8.V8
import com.example.androidnode.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.res = "22"

        val runtime = V8.createV8Runtime()
        val result = runtime.executeIntegerScript(
            """
          var first = 5;
          var second = 1;
          first << second;
          
          """.trimIndent()
        )
        binding.res = "5 << 1 = $result"
        runtime.release()

    }

    /**
     * A native method that is implemented by the 'androidnode' native library,
     * which is packaged with this application.
     */
//    external fun startNodeWithArguments(arguments: Array<String>): Int?

    companion object {
        // Used to load the 'androidnode' library on application startup.
        init {
            System.loadLibrary("androidnode")
        }
    }
}
