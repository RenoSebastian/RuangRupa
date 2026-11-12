package com.example.ruangai.data.repository.local

import com.example.ruangai.data.local.db.dao.RoomAnalysisDao
import com.example.ruangai.data.local.db.dao.RoomSceneDao
import com.example.ruangai.data.local.db.entity.RoomAnalysisEntity
import com.example.ruangai.domain.model.*
import com.example.ruangai.domain.repository.RoomAnalysisRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PersistentRoomAnalysisRepository(
    private val analysisDao: RoomAnalysisDao,
    private val roomSceneDao: RoomSceneDao? = null
) : RoomAnalysisRepository {

    override suspend fun getCachedAnalysis(sceneId: String): RoomAnalysisResult? = withContext(Dispatchers.IO) {
        val cached = analysisDao.getAnalysisForScene(sceneId) ?: return@withContext null
        toDomain(cached)
    }

    override suspend fun analyzeRoom(
        sceneId: String,
        imageUri: String
    ): Result<RoomAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            // 1. Check database cache first (avoid unnecessary repeated processing)
            val existing = analysisDao.getAnalysisForScene(sceneId)
            if (existing != null) {
                return@withContext Result.success(toDomain(existing))
            }

            // 2. Fetch room type from DB if available
            val sceneEntity = roomSceneDao?.getSceneByIdSync(sceneId)
            val roomType = if (sceneEntity != null) {
                try { RoomType.valueOf(sceneEntity.roomType) } catch (e: Exception) { RoomType.KOST_STUDIO }
            } else {
                RoomType.KOST_STUDIO
            }

            // 3. Generate structured deterministic mock analysis
            val result = generateStructuredMockAnalysis(sceneId, roomType)

            // 4. Save to persistent database cache
            analysisDao.insertAnalysis(toEntity(result))

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateStructuredMockAnalysis(sceneId: String, roomType: RoomType): RoomAnalysisResult {
        return when (roomType) {
            RoomType.KOST_STUDIO -> RoomAnalysisResult(
                sceneId = sceneId,
                detectedType = RoomType.KOST_STUDIO,
                lighting = LightingInfo(
                    naturalSource = "Jendela kamar arah timur",
                    timeOfDay = "Pagi / Siang",
                    intensity = "Cahaya alami terang menyebar rata"
                ),
                surface = SurfaceMaterial(
                    wallColorHex = 0xFFF5F2EB,
                    wallMaterial = "Cat tembok matt krem terang (off-white)",
                    floorType = "Vinyl motif kayu pinus terang"
                ),
                detectedItems = listOf(
                    DetectedFurnitureItem("KASUR", "Kasur Single 100x200cm dengan sprei netral", "Sudut kiri belakang", "Bisa diganti dipan kayu Japandi minimalis berkaki"),
                    DetectedFurnitureItem("MEJA", "Meja belajar ringkas 80x50cm", "Sisi kanan dekat stopkontak", "Pertahankan posisi untuk efisiensi ruang kerja")
                ),
                makeoverSuggestions = listOf(
                    "Gunakan dipan kayu berkaki ramping agar lantai tetap terlihat luas",
                    "Tambahkan karpet lingkaran berbahan serat alami di samping kasur",
                    "Ganti lampu putih neon dengan ambient standing lamp warm-white (3000K)"
                ),
                dominantColors = listOf("Krem Hangat (#F8F7F4)", "Putih Dinding (#FFFFFF)", "Kayu Pinus (#D4B393)"),
                wallAndFloorObservations = "Dinding bersih tanpa wallpaper, lantai vinyl kayu pinus dalam kondisi mulus",
                doorsWindows = "1 jendela kaca geser (120x100cm), 1 pintu kamar mandi dalam di sudut",
                approximateLayoutObservations = "Bentuk persegi panjang ~3x4m, orientasi vertikal memanjang",
                suggestedDesignStyles = listOf(DesignStyle.JAPANDI, DesignStyle.SCANDINAVIAN, DesignStyle.MINIMALIST),
                isSimulation = true,
                simulationNotice = "Simulasi Analisis — Estimasi visual awal tanpa sensor LiDAR/depth sensor fisik"
            )

            RoomType.LIVING_ROOM -> RoomAnalysisResult(
                sceneId = sceneId,
                detectedType = RoomType.LIVING_ROOM,
                lighting = LightingInfo(
                    naturalSource = "Jendela utama balkon ruang tengah",
                    timeOfDay = "Siang hari cerah",
                    intensity = "Pencahayaan alami melimpah"
                ),
                surface = SurfaceMaterial(
                    wallColorHex = 0xFFEDEAE3,
                    wallMaterial = "Dinding plester acian halus dengan cat putih gading",
                    floorType = "Ubin granit porcelain 60x60cm abu-abu terang"
                ),
                detectedItems = listOf(
                    DetectedFurnitureItem("SOFA", "Sofa sudut 3-seater bahan kain abu", "Tengah ruangan menghadap dinding", "Cocok dipadukan dengan meja kopi bundar kayu"),
                    DetectedFurnitureItem("KREDENSA", "Meja TV kayu jati gelap", "Dinding utama", "Bisa diganti rak gantung floating untuk kesan melayang")
                ),
                makeoverSuggestions = listOf(
                    "Pilih meja kopi bundar untuk melancarkan sirkulasi jalan di ruang keluarga",
                    "Gunakan karpet berukuran minimal 160x230cm untuk mendefinisikan seating area",
                    "Hadirkan tanaman indoor seperti Monstera atau Ficus Lyrata di sudut ruangan"
                ),
                dominantColors = listOf("Abu-abu Terang (#E0E0E0)", "Putih Bersih (#FFFFFF)", "Aksen Kayu Jati (#8B8278)"),
                wallAndFloorObservations = "Lantai granit licin reflektif, dinding polos siap untuk pajangan dinding",
                doorsWindows = "Pintu geser kaca balkon lebar 200cm, 1 pintu utama panel kayu",
                approximateLayoutObservations = "Area terbuka (open-plan) luas ~4x5m menyambung ke ruang makan",
                suggestedDesignStyles = listOf(DesignStyle.SCANDINAVIAN, DesignStyle.JAPANDI, DesignStyle.MODERN_LUXURY),
                isSimulation = true,
                simulationNotice = "Simulasi Analisis — Estimasi visual awal tanpa sensor LiDAR/depth sensor fisik"
            )

            RoomType.BEDROOM -> RoomAnalysisResult(
                sceneId = sceneId,
                detectedType = RoomType.BEDROOM,
                lighting = LightingInfo(
                    naturalSource = "Jendela kamar sudut",
                    timeOfDay = "Sore hari teduh",
                    intensity = "Cahaya lembut menenangkan"
                ),
                surface = SurfaceMaterial(
                    wallColorHex = 0xFFF2EFE9,
                    wallMaterial = "Dinding cat interior doff warna linen",
                    floorType = "Lantai parket kayu olahan"
                ),
                detectedItems = listOf(
                    DetectedFurnitureItem("KASUR", "Ranjang Queen 160x200cm", "Tengah ruangan", "Bisa dipercantik dengan headboard busa kain linen"),
                    DetectedFurnitureItem("LEMARI", "Lemari pakaian 2 pintu sliding", "Sisi samping kanan", "Ukuran sudah pas dengan tinggi plafon")
                ),
                makeoverSuggestions = listOf(
                    "Tambahkan nakas samping tidur dengan laci tertutup untuk mengurangi clutter",
                    "Gunakan tirai blackout dua lapis (sheer + blackout) untuk kenyamanan tidur optimal",
                    "Pilih palet linen earthy tones seperti sage green atau terracotta lembut"
                ),
                dominantColors = listOf("Linen Lembut (#F8F7F4)", "Kayu Walnut (#6B655E)", "Krem (#D4B393)"),
                wallAndFloorObservations = "Lantai parket hangat, dinding bebas jamur dan retak rambut",
                doorsWindows = "1 jendela jungkit dengan teralis, 1 pintu utama kayu solid",
                approximateLayoutObservations = "Kamar tidur master ~3.5x4.5m",
                suggestedDesignStyles = listOf(DesignStyle.JAPANDI, DesignStyle.MINIMALIST, DesignStyle.BOHEMIAN),
                isSimulation = true,
                simulationNotice = "Simulasi Analisis — Estimasi visual awal tanpa sensor LiDAR/depth sensor fisik"
            )

            RoomType.WORKSPACE -> RoomAnalysisResult(
                sceneId = sceneId,
                detectedType = RoomType.WORKSPACE,
                lighting = LightingInfo(
                    naturalSource = "Jendela samping meja kerja",
                    timeOfDay = "Siang hari fokus",
                    intensity = "Cahaya menyamping tanpa glare ke layar monitor"
                ),
                surface = SurfaceMaterial(
                    wallColorHex = 0xFFECEAE4,
                    wallMaterial = "Cat tembok kelabu netral",
                    floorType = "Lantai keramik doff warna semen ekspos"
                ),
                detectedItems = listOf(
                    DetectedFurnitureItem("MEJA", "Desk kerja 120x60cm besi hitam", "Menghadap dinding samping jendela", "Sangat pas untuk dual monitor setup"),
                    DetectedFurnitureItem("KURSI", "Kursi kantor ergonomis jaring mesh", "Depan meja", "Ergonomis untuk jam kerja panjang")
                ),
                makeoverSuggestions = listOf(
                    "Pasang pegboard modular di dinding depan meja untuk organizer aksesoris kabel",
                    "Gunakan task lamp monitor bar untuk penerangan keyboard malam hari tanpa silau",
                    "Tambahkan rak buku ambalan kayu industrial di atas meja"
                ),
                dominantColors = listOf("Besi Hitam Matte (#252525)", "Kayu Industri (#8B8278)", "Abu Beton (#9E978E)"),
                wallAndFloorObservations = "Karakter industrial modern, dinding kokoh untuk pemasangan ambalan",
                doorsWindows = "1 jendela aluminium hitam geser, 1 pintu panel",
                approximateLayoutObservations = "Ruang kerja privat ~2.5x3m",
                suggestedDesignStyles = listOf(DesignStyle.INDUSTRIAL, DesignStyle.MINIMALIST, DesignStyle.SCANDINAVIAN),
                isSimulation = true,
                simulationNotice = "Simulasi Analisis — Estimasi visual awal tanpa sensor LiDAR/depth sensor fisik"
            )

            else -> RoomAnalysisResult(
                sceneId = sceneId,
                detectedType = roomType,
                lighting = LightingInfo(
                    naturalSource = "Ventilasi dan jendela alami",
                    timeOfDay = "Siang hari",
                    intensity = "Pencahayaan fungsional cukup terang"
                ),
                surface = SurfaceMaterial(
                    wallColorHex = 0xFFF8F7F4,
                    wallMaterial = "Dinding cat netral mudah dibersihkan",
                    floorType = "Lantai ubin keramik anti-slip"
                ),
                detectedItems = listOf(
                    DetectedFurnitureItem("KABINET", "Kabinet penyimpanan serbaguna", "Sisi dinding utama", "Optimalkan dengan rak tingkat")
                ),
                makeoverSuggestions = listOf(
                    "Maksimalkan ruang vertikal dengan ambalan dinding bertingkat",
                    "Pilih furnitur multifungsi yang mudah dipindahkan dan dibersihkan",
                    "Gunakan pencahayaan task light di area kerja utama"
                ),
                dominantColors = listOf("Putih Bersih (#FFFFFF)", "Krem (#F8F7F4)", "Kayu Alami (#D4B393)"),
                wallAndFloorObservations = "Permukaan dinding dan lantai fungsional dan bersih",
                doorsWindows = "1 bukaan jendela ventilasi, 1 pintu akses",
                approximateLayoutObservations = "Bentuk area persegi kompak ~3x3m",
                suggestedDesignStyles = listOf(DesignStyle.MINIMALIST, DesignStyle.JAPANDI),
                isSimulation = true,
                simulationNotice = "Simulasi Analisis — Estimasi visual awal tanpa sensor LiDAR/depth sensor fisik"
            )
        }
    }

    private fun toEntity(domain: RoomAnalysisResult): RoomAnalysisEntity {
        return RoomAnalysisEntity(
            sceneId = domain.sceneId,
            detectedRoomType = domain.detectedType.name,
            dominantColorsJson = domain.dominantColors.joinToString(","),
            wallFloorObservations = domain.wallAndFloorObservations,
            doorsWindowsObservations = domain.doorsWindows,
            existingFurnitureJson = domain.detectedItems.joinToString(";") { "${it.category}|${it.description}|${it.positionLabel}|${it.actionSuggestion}" },
            lightingConditions = "${domain.lighting.naturalSource} - ${domain.lighting.intensity}",
            approximateLayoutObservations = domain.approximateLayoutObservations,
            suggestedStylesJson = domain.suggestedDesignStyles.joinToString(",") { it.name },
            isSimulation = domain.isSimulation,
            analyzedAt = domain.analyzedAt
        )
    }

    private fun toDomain(entity: RoomAnalysisEntity): RoomAnalysisResult {
        val type = try { RoomType.valueOf(entity.detectedRoomType) } catch (e: Exception) { RoomType.KOST_STUDIO }
        val dominantColors = entity.dominantColorsJson.split(",").filter { it.isNotBlank() }
        val styles = entity.suggestedStylesJson.split(",").mapNotNull { name ->
            try { DesignStyle.valueOf(name) } catch (e: Exception) { null }
        }.ifEmpty { listOf(DesignStyle.JAPANDI, DesignStyle.SCANDINAVIAN) }

        val detectedItems = entity.existingFurnitureJson.split(";").filter { it.isNotBlank() }.mapNotNull { part ->
            val segs = part.split("|")
            if (segs.size >= 4) {
                DetectedFurnitureItem(segs[0], segs[1], segs[2], segs[3])
            } else null
        }

        return RoomAnalysisResult(
            sceneId = entity.sceneId,
            detectedType = type,
            lighting = LightingInfo(entity.lightingConditions, "Siang", "Normal"),
            surface = SurfaceMaterial(0xFFF8F7F4, entity.wallFloorObservations, "Keramik/Vinyl"),
            detectedItems = detectedItems,
            makeoverSuggestions = listOf(
                "Gunakan furnitur proporsional dengan pencahayaan alami ruangan",
                "Pilih warna netral untuk menjaga kelegaan visual"
            ),
            dominantColors = dominantColors.ifEmpty { listOf("Krem Hangat", "Putih", "Kayu") },
            wallAndFloorObservations = entity.wallFloorObservations,
            doorsWindows = entity.doorsWindowsObservations,
            approximateLayoutObservations = entity.approximateLayoutObservations,
            suggestedDesignStyles = styles,
            isSimulation = entity.isSimulation,
            simulationNotice = "Simulasi Analisis — Estimasi visual awal tanpa sensor LiDAR/depth sensor fisik",
            analyzedAt = entity.analyzedAt
        )
    }
}
