package com.mlr_apps.detection.ui.screens.Bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.*

import com.mlr_apps.detection.ui.theme.DetectionTheme
import java.io.IOException


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Bluetooth() {
    DetectionTheme {
        DetectionTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                val cameraPermissionState = rememberPermissionState(android.Manifest.permission.BLUETOOTH)
                val context = LocalContext.current
                val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager


            }
        }
    }
}


@Composable
fun TopBarUwU(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(text = "List Bluetooth Devices")
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun Permission(
    bluetoothPermissionState: PermissionState
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (!bluetoothPermissionState.status.isGranted) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally) {

                val textToShow = if (bluetoothPermissionState.status.shouldShowRationale) {
                    "The bluetooth is important for this app.\n Please grant the permission."
                } else {
                    "Bluetooth not available"
                }
                Text(textToShow, textAlign = TextAlign.Center, color = MaterialTheme.colors.onSurface)

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    shape = CircleShape,
                    onClick = { bluetoothPermissionState.launchPermissionRequest() }) {
                    Text("Request permission")
                    Icon(Icons.Outlined.PlayArrow, contentDescription = "content description")
                }
            }
        }
    }
}


