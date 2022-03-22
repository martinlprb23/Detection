package com.mlr_apps.detection.ui.screens

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.mlr_apps.detection.Data.Model.DetectionObject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class DetectionViewModel
@Inject
constructor():ViewModel() {


    var detectionList = mutableStateOf<List<DetectionObject>>(listOf())
    var isLoading = mutableStateOf(value = false)

    fun setList(detectedObjectList: List<DetectionObject>){
        if (detectedObjectList.isNotEmpty()){
            isLoading.value = true
            detectionList.value = detectedObjectList
        }else{
            isLoading.value = false
        }
    }
    
}