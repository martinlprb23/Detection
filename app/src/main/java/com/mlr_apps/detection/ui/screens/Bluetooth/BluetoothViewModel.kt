package com.mlr_apps.detection.ui.screens.Bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.logging.Handler

import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel
@Inject
constructor():ViewModel(){

    private val PERMISSION_CODE = 1
    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()



}