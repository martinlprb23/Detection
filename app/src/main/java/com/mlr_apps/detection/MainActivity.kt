package com.mlr_apps.detection

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.AssetFileDescriptor
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.mlr_apps.detection.ui.screens.Bluetooth.Bluetooth
import com.mlr_apps.detection.ui.screens.DetectionScreen
import dagger.hilt.android.AndroidEntryPoint
import org.tensorflow.lite.Interpreter
import java.io.*
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    //CAMERA
    companion object {
        private const val MODEL_FILE_NAME = "ssd_mobilenet_v1_1_metadata_1.tflite"
        private const val MODEL_FILE_NAME2 = "ssd_mobilenet_v1.tflite"
        private const val LABEL_FILE_NAME = "coco_dataset_labels_v1.txt"
        private const val LABEL_FILE_NAME2 = "coco_dataset_labels.txt"
    }
    private lateinit var cameraExecutor: ExecutorService
    // Intérprete con un contenedor para trabajar con modelos tflite
    private val interpreter: Interpreter by lazy { Interpreter(loadModel()) }
    // Modelo de lista de etiquetas correctas
    private val labels: List<String> by lazy { loadLabels() }
    // Convertidor para convertir la imagen YUV de la cámara a RGB
    private val yuvToRgbConverter: YuvToRgbConverter by lazy { YuvToRgbConverter(this) }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val systemUiController = rememberSystemUiController()
            val navController = rememberNavController()

            if(isSystemInDarkTheme()){
                systemUiController.setNavigationBarColor(Color.Black, darkIcons = false)
                systemUiController.setStatusBarColor(Color.Black, darkIcons = false)
            }
            cameraExecutor = Executors.newSingleThreadExecutor()

            NavHost(navController = navController, startDestination = "Detection"){
                composable("Detection"){
                    DetectionScreen(
                        navController = navController,
                        cameraExecutor = cameraExecutor,
                        yuvToRgbConverter = yuvToRgbConverter,
                        interpreter = interpreter,
                        labels = labels
                    )
                }
                composable("Bluetooth"){
                    Bluetooth()
                }
            }
        }
    }





    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }



    // Cargue el modelo tflite desde los activos
    private fun loadModel(fileName: String = MODEL_FILE_NAME2): ByteBuffer {
        lateinit var modelBuffer: ByteBuffer
        var file: AssetFileDescriptor? = null
        try {
            file = assets.openFd(fileName)
            val inputStream = FileInputStream(file.fileDescriptor)
            val fileChannel = inputStream.channel
            modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, file.startOffset, file.declaredLength)
        } catch (e: Exception) {
            Log.d("XD", e.toString())
            Toast.makeText(this, "Error de lectura del archivo del modelo", Toast.LENGTH_SHORT).show()
            finish()
        } finally {
            file?.close()
        }
        return modelBuffer
    }


    // Obtenga datos de etiquetas correctos del modelo de los activos
    private fun loadLabels(fileName: String = LABEL_FILE_NAME): List<String> {
        var labels = listOf<String>()
        var inputStream: InputStream? = null
        try {
            inputStream = assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            labels = reader.readLines()
        } catch (e: Exception) {
            Toast.makeText(this, "Error de lectura del archivo del modelo", Toast.LENGTH_SHORT).show()
            finish()
        } finally {
            inputStream?.close()
        }
        return labels
    }

}