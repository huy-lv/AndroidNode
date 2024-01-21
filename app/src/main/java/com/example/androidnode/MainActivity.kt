package com.example.androidnode

import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.androidnode.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.res = "Press above button to execute node function. The result will display here"
        binding.button1.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                GlobalScope.launch(Dispatchers.Main) {
                    downloadCodeAndRun()
                }
                delay(1000)
                binding.res = fetchDataFromLocalServer()
            }
        }
        binding.saveButton.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                binding.webView1.reload()
                delay(3000)
                triggerRebirth(applicationContext)
            }
        }
        binding.webView1.settings.javaScriptEnabled = true
        binding.webView1.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        binding.webView1.loadUrl("https://textdoc.co/GKUlZhJgesabOMyF")
    }

    fun triggerRebirth(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent!!.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        // Required for API 34 and later
        // Ref: https://developer.android.com/about/versions/14/behavior-changes-14#safer-intents
        mainIntent.setPackage(context.packageName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }
    private suspend fun downloadCodeAndRun(): Int? {
        return withContext(Dispatchers.IO) {
            //The path where we expect the node project to be at runtime.
            val nodeDir = applicationContext.filesDir.absolutePath + "/nodejs-project"
            Log.e("cxz", "cz $nodeDir");
            //Recursively delete any existing nodejs-project.
            val nodeDirReference = File(nodeDir)
            if (nodeDirReference.exists()) {
                deleteFolderRecursively(File(nodeDir))
            }
            val document: Document = Jsoup.connect("https://textdoc.co/TcyC2pgf7isSG4DQ").get()
            val nodeFileContent = document.body().getElementById("txt-doc").text()
            copyStringToFile(nodeDir,nodeFileContent)

            //Copy the node project from assets into the application's data path.
//            copyAssetFolder(applicationContext.assets, "nodejs-project", nodeDir)

            startNodeWithArguments(
                arrayOf(
                    "node",
//                    "$nodeDir/main.js"
                    applicationContext.filesDir.absolutePath + "/main.js"
                )
            )
        }
    }

    private fun copyStringToFile(nodeDir: String, content: String) {
        try {
            // Open a file output stream in private mode (MODE_PRIVATE)
            File(nodeDir).mkdirs()
            val file = File(applicationContext.filesDir, "main.js")
            applicationContext.openFileOutput(file.name, Context.MODE_PRIVATE).use {
                it.write(content.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun fetchDataFromLocalServer(): String {
        Log.e("Cxz","cxz2")

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
