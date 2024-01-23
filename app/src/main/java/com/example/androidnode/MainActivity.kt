package com.example.androidnode

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.androidnode.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.res = "22"
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
