package com.example.androidnode

import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.androidnode.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.URL


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    var _startedNodeAlready = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalScope.launch(Dispatchers.Main) {
            copyFolder()
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.res = "Press above button to execute node function. The result will display here"
        binding.button1.setOnClickListener {
            Log.e("cxz","cxzcxzczx")
            GlobalScope.launch(Dispatchers.Main) {
                binding.res = executeNodeFunction()
            }
        }
    }

    private suspend fun copyFolder(): Int? {
        return withContext(Dispatchers.IO) {
            //The path where we expect the node project to be at runtime.
            val nodeDir = applicationContext.filesDir.absolutePath + "/nodejs-project"
            //Recursively delete any existing nodejs-project.
            val nodeDirReference = File(nodeDir)
            if (nodeDirReference.exists()) {
                deleteFolderRecursively(File(nodeDir))
            }
            //Copy the node project from assets into the application's data path.
            copyAssetFolder(applicationContext.assets, "nodejs-project", nodeDir)
            startNodeWithArguments(
                arrayOf(
                    "node",
                    "$nodeDir/main.js"
                )
            )
        }
    }

    private suspend fun executeNodeFunction(): String {
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

    private fun deleteFolderRecursively(file: File): Boolean {
        return try {
            var res = true
            for (childFile in file.listFiles()) {
                res = if (childFile.isDirectory) {
                    res and deleteFolderRecursively(childFile)
                } else {
                    res and childFile.delete()
                }
            }
            res = res and file.delete()
            res
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun copyAssetFolder(
        assetManager: AssetManager,
        fromAssetPath: String,
        toPath: String
    ): Boolean {
        return try {
            val files = assetManager.list(fromAssetPath)
            var res = true
            if (files!!.size == 0) {
                //If it's a file, it won't have any assets "inside" it.
                res = res and copyAsset(
                    assetManager,
                    fromAssetPath,
                    toPath
                )
            } else {
                File(toPath).mkdirs()
                for (file in files) res = res and copyAssetFolder(
                    assetManager,
                    "$fromAssetPath/$file",
                    "$toPath/$file"
                )
            }
            res
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun copyAsset(
        assetManager: AssetManager,
        fromAssetPath: String,
        toPath: String
    ): Boolean {
        var `in`: InputStream? = null
        var out: OutputStream? = null
        return try {
            `in` = assetManager.open(fromAssetPath)
            File(toPath).createNewFile()
            out = FileOutputStream(toPath)
            copyFile(`in`, out)
            `in`.close()
            `in` = null
            out.flush()
            out.close()
            out = null
            true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            false
        }
    }

    @Throws(IOException::class)
    private fun copyFile(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
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
