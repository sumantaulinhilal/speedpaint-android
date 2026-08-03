package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "speedpaint_projects")
data class SpeedPaintProjectEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val dateCreated: Long,
    val sketchDurationSec: Int,
    val fillDurationSec: Int,
    val handStyleName: String,
    val sequenceOrderName: String,
    val sketchTypeName: String,
    val aspectRatioName: String,
    val backgroundStyleName: String,
    val fps: Int,
    val qualityName: String,
    val exportFormatName: String,
    val vectorPathsJson: String
)
