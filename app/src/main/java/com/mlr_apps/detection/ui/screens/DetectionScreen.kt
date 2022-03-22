package com.mlr_apps.detection.ui.screens

import android.content.Context
import android.graphics.Color.*
import android.graphics.Paint
import android.util.Log
import android.util.Size
import android.view.Surface.ROTATION_0
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.google.accompanist.permissions.*
import com.mlr_apps.detection.ObjectDetector
import com.mlr_apps.detection.R
import com.mlr_apps.detection.YuvToRgbConverter
import com.mlr_apps.detection.ui.theme.DetectionTheme
import org.tensorflow.lite.Interpreter
import java.io.File
import java.util.concurrent.ExecutorService

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DetectionScreen(
    navController: NavController,
    cameraExecutor: ExecutorService,
    yuvToRgbConverter: YuvToRgbConverter,
    interpreter: Interpreter,
    labels: List<String>
) {
    DetectionTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
            Column{
                TopBar(navController = navController)
                if (cameraPermissionState.status.isGranted){
                    OpenCamera(cameraExecutor, yuvToRgbConverter, interpreter, labels)
                }else{
                    Permission(cameraPermissionState)
                }
            }
        }
    }
}

@Composable
fun TopBar(navController: NavController) {
    Row(
        modifier = Modifier
            .padding(start = 16.dp, top = 50.dp, end = 24.dp, bottom = 16.dp)
            .fillMaxWidth(),
        Arrangement.SpaceBetween
    ) {

        Text(
            text = "DETECTION",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )

        IconButton(
            onClick = {
                navController.navigate(
                   route = "Bluetooth"
                )
            },
            modifier = Modifier.size(21.dp)
        ){
            Icon(
                painter = painterResource(id = R.drawable.ic_outline_bluetooth_searching_24),
                contentDescription = "Icon back screen",
                tint = MaterialTheme.colors.onSurface,
            )
        }
    }

}


@Composable
fun OpenCamera(
    cameraExecutor: ExecutorService,
    yuvToRgbConverter: YuvToRgbConverter,
    interpreter: Interpreter,
    labels: List<String>
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    Column {
        SimpleCameraPreview(
            context = context,
            lifecycleOwner = lifecycleOwner,
            cameraExecutor = cameraExecutor,
            yuvToRgbConverter = yuvToRgbConverter,
            interpreter = interpreter,
            labels = labels
        )
    }
}



@Composable
fun SimpleCameraPreview(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    cameraExecutor: ExecutorService,
    yuvToRgbConverter: YuvToRgbConverter,
    interpreter: Interpreter,
    labels: List<String>,
    viewModel: DetectionViewModel = hiltViewModel(),
) {
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var preview by remember { mutableStateOf<Preview?>(null) }
    val executor = ContextCompat.getMainExecutor(context)
    val cameraProvider = cameraProviderFuture.get()

    val drawCanvas by remember { viewModel.isLoading }
    val detectionListObject by remember { viewModel.detectionList }

    val paint = Paint()
    val pathColorList = listOf(Color.Red, Color.Green, Color.Cyan, Color.Blue)
    val pathColorListInt = listOf(RED, GREEN, CYAN, BLUE)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()){
        val boxConstraint = this

        val sizeWith = with(LocalDensity.current) { boxConstraint.maxWidth.toPx() }
        val sizeHeight = with(LocalDensity.current) { boxConstraint.maxHeight.toPx() }


        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                cameraProviderFuture.addListener({

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setTargetRotation(ROTATION_0)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(
                                cameraExecutor,
                                ObjectDetector(
                                    yuvToRgbConverter = yuvToRgbConverter,
                                    interpreter = interpreter,
                                    labels = labels,
                                    resultViewSize = Size(sizeWith.toInt(), sizeHeight.toInt()
                                    )
                                ) { detectedObjectList ->
                                    viewModel.setList(detectedObjectList)
                                }
                            )
                        }

                    imageCapture = ImageCapture.Builder()
                        .setTargetRotation(previewView.display.rotation)
                        .build()

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        imageCapture,
                        preview,
                        imageAnalyzer
                    )
                }, executor)
                preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                previewView
            }
        )

        if (drawCanvas){

            Canvas(
                modifier = Modifier
                    .fillMaxSize(),
                onDraw = {
                    detectionListObject.mapIndexed { i, detectionObject ->
                        Log.d("Object", detectionObject.label + " --- "  +detectionObject.score)

                        paint.apply {
                            color = pathColorListInt[i]
                            style = Paint.Style.FILL
                            isAntiAlias = true
                            textSize = 50f
                        }

                        drawRect(
                            color = pathColorList[i],
                            topLeft =  Offset(
                                x = detectionObject.boundingBox.left,
                                y = detectionObject.boundingBox.top
                            ),
                            size = androidx.compose.ui.geometry.Size(
                                width = detectionObject.boundingBox.width(),
                                height = detectionObject.boundingBox.height()
                            ),
                            style = Stroke(width = 3.dp.toPx())
                        )

                        drawIntoCanvas {

                            //it.nativeCanvas.drawRect(detectionObject.boundingBox, paint)
                            it.nativeCanvas.drawText(
                                detectionObject.label + " " + "%,.2f".format(detectionObject.score * 100) + "%",
                                detectionObject.boundingBox.left,            // x-coordinates of the origin (top left)
                                detectionObject.boundingBox.top - 5f, // y-coordinates of the origin (top left)
                                paint
                            )
                        }
                    }
                }
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(16.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(MaterialTheme.colors.background, RoundedCornerShape(15.dp))
                .padding(8.dp)
                .align(Alignment.BottomCenter)
        ) {
            IconButton(
                onClick = {

                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_outline_not_started_24),
                    contentDescription = "",
                    modifier = Modifier.size(35.dp),
                    tint = MaterialTheme.colors.onSurface
                )
            }


            Row {

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                        .align(CenterVertically)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Disconnect",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }


            IconButton(
                onClick = {

                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_outline_settings_power_24),
                    contentDescription = "",
                    modifier = Modifier.size(35.dp),
                    tint = MaterialTheme.colors.onSurface
                )
            }
        }
    }
}



@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun Permission(
    cameraPermissionState: PermissionState
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (!cameraPermissionState.status.isGranted) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally) {

                val textToShow = if (cameraPermissionState.status.shouldShowRationale) {
                    "The camera is important for this app.\n Please grant the permission."
                } else {
                    "Camera not available"
                }
                Text(textToShow, textAlign = TextAlign.Center, color = MaterialTheme.colors.onSurface)

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    shape = CircleShape,
                    onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text("Request permission")
                    Icon(Icons.Outlined.PlayArrow, contentDescription = "content description")
                }
            }
        }
    }

}