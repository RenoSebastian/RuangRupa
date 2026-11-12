package com.example.ruangai.data.remote.dto

import com.example.ruangai.domain.model.JobMode
import com.example.ruangai.domain.model.JobStatus
import com.example.ruangai.domain.model.VisualizationJob

data class VisualizationJobRequestDto(
    val sceneId: String,
    val mode: String, // "FULL_MAKEOVER" or "PRODUCT_PLACEMENT"
    val styleId: String? = null,
    val placementIds: List<String> = emptyList(),
    val idempotencyKey: String
)

data class VisualizationJobResponseDto(
    val jobId: String,
    val sceneId: String,
    val mode: String,
    val status: String,
    val creditsReserved: Int,
    val message: String
) {
    fun toDomain(): VisualizationJob = VisualizationJob(
        jobId = jobId,
        sceneId = sceneId,
        mode = runCatching { JobMode.valueOf(mode) }.getOrDefault(JobMode.PRODUCT_PLACEMENT),
        status = runCatching { JobStatus.valueOf(status) }.getOrDefault(JobStatus.PROCESSING),
        creditsReserved = creditsReserved,
        message = message
    )
}
