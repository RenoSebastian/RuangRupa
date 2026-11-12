package com.example.ruangai.domain.model

data class DetectedFurnitureItem(
    val category: String,
    val description: String,
    val positionLabel: String,
    val actionSuggestion: String
)

data class LightingInfo(
    val naturalSource: String,
    val timeOfDay: String,
    val intensity: String
)

data class SurfaceMaterial(
    val wallColorHex: Long,
    val wallMaterial: String,
    val floorType: String
)

data class RoomAnalysisResult(
    val sceneId: String,
    val detectedType: RoomType,
    val lighting: LightingInfo,
    val surface: SurfaceMaterial,
    val detectedItems: List<DetectedFurnitureItem>,
    val makeoverSuggestions: List<String>,
    // Milestone 3 Vision Analysis Additions
    val dominantColors: List<String> = listOf("Krem Hangat (#F8F7F4)", "Putih Matt (#FFFFFF)", "Kayu Pinus Alami (#D4B393)"),
    val wallAndFloorObservations: String = "Dinding cat matt off-white, lantai ubin keramik 40x40cm / vinyl motif kayu cerah",
    val doorsWindows: String = "1 jendela kaca geser di sisi timur, 1 pintu kayu standar",
    val approximateLayoutObservations: String = "Bentuk ruangan persegi panjang ~3x4m, orientasi vertikal memanjang",
    val suggestedDesignStyles: List<DesignStyle> = listOf(DesignStyle.JAPANDI, DesignStyle.SCANDINAVIAN, DesignStyle.MINIMALIST),
    val isSimulation: Boolean = true,
    val simulationNotice: String = "Simulasi Analisis — Estimasi visual awal tanpa sensor LiDAR/depth sensor fisik",
    val analyzedAt: Long = System.currentTimeMillis()
)
