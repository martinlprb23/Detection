package com.mlr_apps.detection.Data.Model

import android.graphics.RectF

/**
 * Clase para poner el resultado de la detección.
 */
data class DetectionObject(
    val score: Float,
    val label: String,
    val boundingBox: RectF
)