package com.example.ruangai.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ruangai.data.local.db.RuangAiDatabase
import com.example.ruangai.data.local.storage.LocalImageStorageManager
import com.example.ruangai.data.remote.config.ApiConfig
import com.example.ruangai.data.remote.config.Environment
import com.example.ruangai.data.repository.local.PersistentAIChatRepository
import com.example.ruangai.data.repository.local.PersistentAIInteriorDesignRepository
import com.example.ruangai.data.repository.local.PersistentAffiliateLinkRepository
import com.example.ruangai.data.repository.local.PersistentMultimodalInputRepository
import com.example.ruangai.data.repository.local.PersistentRoomAnalysisRepository
import com.example.ruangai.data.repository.local.PersistentRoomSceneRepository
import com.example.ruangai.data.repository.local.PersistentShoppingListRepository
import com.example.ruangai.data.repository.mock.*
import com.example.ruangai.domain.repository.*
import com.example.ruangai.ui.viewmodel.RuangAiViewModel

interface AppContainer {
    val apiConfig: ApiConfig
    val authRepository: AuthRepository
    val roomSceneRepository: RoomSceneRepository
    val roomAnalysisRepository: RoomAnalysisRepository
    val imageGenerationRepository: ImageGenerationRepository
    val productCatalogRepository: ProductCatalogRepository
    val shoppingListRepository: ShoppingListRepository
    val affiliateLinkRepository: AffiliateLinkRepository
    val creditWalletRepository: CreditWalletRepository
    val aiChatRepository: AIChatRepository
    val aiRecommendationRepository: AIRecommendationRepository
    val multimodalInputRepository: MultimodalInputRepository
    val aiInteriorDesignRepository: AIInteriorDesignRepository
    val sceneCompositor: com.example.ruangai.data.local.graphics.SceneCompositor?
}

class DefaultAppContainer(
    val context: Context? = null,
    override val apiConfig: ApiConfig = ApiConfig(environment = Environment.DEV),
    private val useMock: Boolean = false
) : AppContainer {

    private val database: RuangAiDatabase? by lazy {
        context?.let { RuangAiDatabase.getInstance(it) }
    }

    private val storageManager: LocalImageStorageManager? by lazy {
        context?.let { LocalImageStorageManager(it) }
    }

    override val sceneCompositor: com.example.ruangai.data.local.graphics.SceneCompositor? by lazy {
        context?.let { com.example.ruangai.data.local.graphics.SceneCompositor(it) }
    }

    override val authRepository: AuthRepository by lazy {
        MockAuthRepository()
    }

    override val roomSceneRepository: RoomSceneRepository by lazy {
        val db = database
        val ctx = context
        val sm = storageManager
        if (db != null && ctx != null && sm != null && !useMock) {
            PersistentRoomSceneRepository(
                context = ctx,
                roomSceneDao = db.roomSceneDao(),
                placementDao = db.roomPlacementDao(),
                storageManager = sm
            )
        } else {
            MockRoomSceneRepository()
        }
    }

    override val roomAnalysisRepository: RoomAnalysisRepository by lazy {
        val db = database
        if (db != null && !useMock) {
            PersistentRoomAnalysisRepository(
                analysisDao = db.roomAnalysisDao(),
                roomSceneDao = db.roomSceneDao()
            )
        } else {
            MockRoomAnalysisRepository()
        }
    }

    override val imageGenerationRepository: ImageGenerationRepository by lazy {
        MockImageGenerationRepository()
    }

    override val productCatalogRepository: ProductCatalogRepository by lazy {
        MockProductCatalogRepository()
    }

    override val shoppingListRepository: ShoppingListRepository by lazy {
        val db = database
        if (db != null && !useMock) {
            PersistentShoppingListRepository(shoppingListDao = db.shoppingListDao())
        } else {
            MockShoppingListRepository()
        }
    }

    override val affiliateLinkRepository: AffiliateLinkRepository by lazy {
        val db = database
        if (db != null && !useMock) {
            PersistentAffiliateLinkRepository(affiliateClickDao = db.affiliateClickDao())
        } else {
            MockAffiliateLinkRepository()
        }
    }

    override val creditWalletRepository: CreditWalletRepository by lazy {
        MockCreditWalletRepository()
    }

    private val recommendationEngine: ConsultantRecommendationEngine by lazy {
        ConsultantRecommendationEngine(productCatalogRepository)
    }

    override val aiRecommendationRepository: AIRecommendationRepository by lazy {
        recommendationEngine
    }

    override val aiChatRepository: AIChatRepository by lazy {
        val db = database
        if (db != null && !useMock) {
            PersistentAIChatRepository(
                consultationDao = db.consultationDao(),
                roomSceneDao = db.roomSceneDao(),
                recommendationEngine = recommendationEngine,
                storageManager = storageManager
            )
        } else {
            MockAIChatRepository(recommendationEngine = recommendationEngine)
        }
    }

    override val multimodalInputRepository: MultimodalInputRepository by lazy {
        if (storageManager != null && !useMock) {
            PersistentMultimodalInputRepository(storageManager = storageManager)
        } else {
            MockMultimodalInputRepository()
        }
    }

    override val aiInteriorDesignRepository: AIInteriorDesignRepository by lazy {
        val db = database
        if (db != null && !useMock) {
            PersistentAIInteriorDesignRepository(
                designGenerationDao = db.designGenerationDao(),
                roomSceneDao = db.roomSceneDao(),
                creditWalletRepository = creditWalletRepository
            )
        } else {
            MockAIInteriorDesignRepository(
                creditWalletRepository = creditWalletRepository
            )
        }
    }
}

class RuangAiViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RuangAiViewModel::class.java)) {
            val appContext = (container as? DefaultAppContainer)?.context
            val prefs = appContext?.getSharedPreferences("ruangai_prefs", Context.MODE_PRIVATE)
            val isCompleted = prefs?.getBoolean("onboarding_completed", false) ?: false

            return RuangAiViewModel(
                authRepo = container.authRepository,
                roomSceneRepo = container.roomSceneRepository,
                analysisRepo = container.roomAnalysisRepository,
                imageGenRepo = container.imageGenerationRepository,
                productRepo = container.productCatalogRepository,
                shoppingListRepo = container.shoppingListRepository,
                affiliateRepo = container.affiliateLinkRepository,
                creditRepo = container.creditWalletRepository,
                aiChatRepo = container.aiChatRepository,
                aiRecommendationRepo = container.aiRecommendationRepository,
                multimodalRepo = container.multimodalInputRepository,
                aiInteriorDesignRepo = container.aiInteriorDesignRepository,
                compositor = container.sceneCompositor,
                initialOnboardingCompleted = isCompleted,
                onSaveOnboardingCompleted = { completed ->
                    prefs?.edit()?.putBoolean("onboarding_completed", completed)?.apply()
                }
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
