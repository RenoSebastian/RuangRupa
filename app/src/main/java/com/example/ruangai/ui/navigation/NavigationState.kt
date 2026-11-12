package com.example.ruangai.ui.navigation

import android.net.Uri

enum class BottomTab(val title: String, val routeName: String) {
    HOME("Beranda", "home"),
    EXPLORE("Eksplor", "explore"),
    DESIGN("Desain", "design"),
    SHOPPING("Belanja", "shopping"),
    PROFILE("Profil", "profile")
}

object RuangAiRoutes {
    const val DEEP_LINK_SCHEME = "ruangai"
    const val DEEP_LINK_HOST = "app"

    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val AUTH = "auth"
    const val MAIN = "main"
    const val ROOM_SCANNER = "room_scanner"
    const val CONSULTATION = "consultation"
    const val STYLE_EXPLORER = "style_explorer"
    const val DESIGN_RESULTS = "design_results"
    const val PRODUCT_DETAIL = "product_detail"
    const val PRODUCT_COMPARISON = "product_comparison"
    const val VIRTUAL_TRY_ON = "virtual_try_on"
    const val ROOM_DETAIL = "room_detail"
    const val SHOPPING_LIST = "shopping_list"
    const val CREDIT_WALLET = "credit_wallet"

    // Argument Keys
    const val ARG_SCENE_ID = "sceneId"
    const val ARG_PRODUCT_ID = "productId"
    const val ARG_TAB = "tab"
}

sealed class Screen(val route: String) {
    object Splash : Screen(RuangAiRoutes.SPLASH)
    object Onboarding : Screen(RuangAiRoutes.ONBOARDING)
    object Auth : Screen(RuangAiRoutes.AUTH)
    data class Main(val activeTab: BottomTab = BottomTab.HOME) : Screen("${RuangAiRoutes.MAIN}?tab=${activeTab.name}")
    object RoomScanner : Screen(RuangAiRoutes.ROOM_SCANNER)
    data class Consultation(val sceneId: String? = null) : Screen(
        if (sceneId != null) "${RuangAiRoutes.CONSULTATION}?sceneId=$sceneId" else RuangAiRoutes.CONSULTATION
    )
    object StyleExplorer : Screen(RuangAiRoutes.STYLE_EXPLORER)
    data class DesignResults(val sceneId: String) : Screen("${RuangAiRoutes.DESIGN_RESULTS}/$sceneId")
    data class RoomDetail(val sceneId: String) : Screen("${RuangAiRoutes.ROOM_DETAIL}/$sceneId")
    data class ProductDetail(
        val productId: String,
        val sourceSceneId: String? = null,
        val sourcePlacementId: String? = null
    ) : Screen("${RuangAiRoutes.PRODUCT_DETAIL}/$productId")
    object ProductComparison : Screen(RuangAiRoutes.PRODUCT_COMPARISON)
    data class VirtualTryOn(val sceneId: String, val preselectedProductId: String? = null) : Screen(
        if (preselectedProductId != null) "${RuangAiRoutes.VIRTUAL_TRY_ON}/$sceneId?productId=$preselectedProductId"
        else "${RuangAiRoutes.VIRTUAL_TRY_ON}/$sceneId"
    )
    object ShoppingList : Screen(RuangAiRoutes.SHOPPING_LIST)
    object CreditWallet : Screen(RuangAiRoutes.CREDIT_WALLET)

    companion object {
        /**
         * Resolves deep links (e.g. ruangai://product_detail/prod_sofa_japandi_01 or /try_on/demo_scene_kost_01)
         */
        fun parseDeepLinkUri(uri: Uri): Screen? {
            val host = uri.host ?: return null
            val pathSegments = uri.pathSegments

            return when {
                host == RuangAiRoutes.PRODUCT_DETAIL || pathSegments.firstOrNull() == RuangAiRoutes.PRODUCT_DETAIL -> {
                    val fallbackId = if (host == RuangAiRoutes.PRODUCT_DETAIL) pathSegments.firstOrNull() else pathSegments.getOrNull(1)
                    val pId = uri.getQueryParameter("id") ?: fallbackId ?: return null
                    val sId = uri.getQueryParameter("sceneId")
                    val plId = uri.getQueryParameter("placementId")
                    ProductDetail(pId, sId, plId)
                }
                host == RuangAiRoutes.PRODUCT_COMPARISON || pathSegments.firstOrNull() == RuangAiRoutes.PRODUCT_COMPARISON -> {
                    ProductComparison
                }
                host == RuangAiRoutes.VIRTUAL_TRY_ON || pathSegments.firstOrNull() == RuangAiRoutes.VIRTUAL_TRY_ON -> {
                    val fallbackScene = if (host == RuangAiRoutes.VIRTUAL_TRY_ON) pathSegments.firstOrNull() else pathSegments.getOrNull(1)
                    val sId = uri.getQueryParameter("sceneId") ?: fallbackScene ?: "demo_scene_kost_01"
                    val pId = uri.getQueryParameter("productId")
                    VirtualTryOn(sId, pId)
                }
                host == RuangAiRoutes.CONSULTATION || pathSegments.firstOrNull() == RuangAiRoutes.CONSULTATION -> {
                    val sId = uri.getQueryParameter("sceneId")
                    Consultation(sId)
                }
                host == RuangAiRoutes.CREDIT_WALLET || pathSegments.firstOrNull() == RuangAiRoutes.CREDIT_WALLET -> {
                    CreditWallet
                }
                host == RuangAiRoutes.ROOM_DETAIL || pathSegments.firstOrNull() == RuangAiRoutes.ROOM_DETAIL -> {
                    val fallbackScene = if (host == RuangAiRoutes.ROOM_DETAIL) pathSegments.firstOrNull() else pathSegments.getOrNull(1)
                    val sId = uri.getQueryParameter("sceneId") ?: fallbackScene ?: "demo_scene_kost_01"
                    RoomDetail(sId)
                }
                host == RuangAiRoutes.ROOM_SCANNER || pathSegments.firstOrNull() == RuangAiRoutes.ROOM_SCANNER -> {
                    RoomScanner
                }
                host == RuangAiRoutes.DESIGN_RESULTS || pathSegments.firstOrNull() == RuangAiRoutes.DESIGN_RESULTS -> {
                    val fallbackScene = if (host == RuangAiRoutes.DESIGN_RESULTS) pathSegments.firstOrNull() else pathSegments.getOrNull(1)
                    val sId = uri.getQueryParameter("sceneId") ?: fallbackScene ?: "demo_scene_kost_01"
                    DesignResults(sId)
                }
                host == RuangAiRoutes.STYLE_EXPLORER || pathSegments.firstOrNull() == RuangAiRoutes.STYLE_EXPLORER -> {
                    StyleExplorer
                }
                host == RuangAiRoutes.AUTH || pathSegments.firstOrNull() == RuangAiRoutes.AUTH -> {
                    Auth
                }
                host == RuangAiRoutes.ONBOARDING || pathSegments.firstOrNull() == RuangAiRoutes.ONBOARDING -> {
                    Onboarding
                }
                host == RuangAiRoutes.SHOPPING_LIST || pathSegments.firstOrNull() == RuangAiRoutes.SHOPPING_LIST -> {
                    ShoppingList
                }
                host == "shopping" || pathSegments.firstOrNull() == "shopping" -> {
                    Main(BottomTab.SHOPPING)
                }
                host == "explore" || pathSegments.firstOrNull() == "explore" -> {
                    Main(BottomTab.EXPLORE)
                }
                host == "profile" || pathSegments.firstOrNull() == "profile" -> {
                    Main(BottomTab.PROFILE)
                }
                host == "home" || pathSegments.firstOrNull() == "home" -> {
                    Main(BottomTab.HOME)
                }
                host == RuangAiRoutes.MAIN || pathSegments.firstOrNull() == RuangAiRoutes.MAIN -> {
                    val tabName = uri.getQueryParameter("tab")?.uppercase()
                    val tab = BottomTab.entries.find { it.name == tabName || it.routeName.equals(tabName, ignoreCase = true) } ?: BottomTab.HOME
                    Main(tab)
                }
                else -> Main(BottomTab.HOME)
            }
        }
    }
}
