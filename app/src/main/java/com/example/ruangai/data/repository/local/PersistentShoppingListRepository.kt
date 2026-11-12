package com.example.ruangai.data.repository.local

import com.example.ruangai.data.local.db.dao.ShoppingListDao
import com.example.ruangai.data.local.db.entity.ShoppingListItemEntity
import com.example.ruangai.data.repository.mock.MockData
import com.example.ruangai.domain.model.Product
import com.example.ruangai.domain.model.RoomScene
import com.example.ruangai.domain.model.ShoppingCartItem
import com.example.ruangai.domain.repository.ShoppingListRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PersistentShoppingListRepository(
    private val shoppingListDao: ShoppingListDao,
    seedInitialItemsIfNeeded: Boolean = true
) : ShoppingListRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        if (seedInitialItemsIfNeeded) {
            scope.launch {
                if (shoppingListDao.getAllItemsSync().isEmpty()) {
                    val seedItems = listOf(
                        ShoppingListItemEntity(
                            itemId = "cart_prod_sofa_01_demo_scene_kost_01",
                            productId = "prod_sofa_01",
                            quantity = 1,
                            isSelected = true,
                            associatedSceneId = "demo_scene_kost_01",
                            associatedPlacementId = "place_kost_1"
                        ),
                        ShoppingListItemEntity(
                            itemId = "cart_prod_table_01_demo_scene_kost_01",
                            productId = "prod_table_01",
                            quantity = 1,
                            isSelected = true,
                            associatedSceneId = "demo_scene_kost_01",
                            associatedPlacementId = "place_kost_2"
                        )
                    )
                    shoppingListDao.upsertItems(seedItems)
                }
            }
        }
    }

    override fun getShoppingListItems(): Flow<List<ShoppingCartItem>> {
        return shoppingListDao.getAllItems().map { entities ->
            entities.mapNotNull { entity ->
                val product = MockData.products.find { it.id == entity.productId } ?: return@mapNotNull null
                ShoppingCartItem(
                    itemId = entity.itemId,
                    product = product,
                    quantity = entity.quantity,
                    isSelected = entity.isSelected,
                    associatedSceneId = entity.associatedSceneId,
                    associatedPlacementId = entity.associatedPlacementId,
                    addedAt = entity.addedAt
                )
            }
        }
    }

    override suspend fun addOrIncrementProduct(
        product: Product,
        sceneId: String?,
        placementId: String?,
        quantityToAdd: Int
    ): Result<ShoppingCartItem> {
        val sceneKey = sceneId ?: "general"
        val itemId = "cart_${product.id}_$sceneKey"
        val existing = shoppingListDao.getItemById(itemId)
        val newQty = (existing?.quantity ?: 0) + quantityToAdd.coerceAtLeast(1)
        val entity = ShoppingListItemEntity(
            itemId = itemId,
            productId = product.id,
            quantity = newQty,
            isSelected = true,
            associatedSceneId = sceneId,
            associatedPlacementId = placementId ?: existing?.associatedPlacementId,
            addedAt = System.currentTimeMillis()
        )
        shoppingListDao.upsertItem(entity)
        return Result.success(
            ShoppingCartItem(
                itemId = itemId,
                product = product,
                quantity = newQty,
                isSelected = true,
                associatedSceneId = sceneId,
                associatedPlacementId = entity.associatedPlacementId,
                addedAt = entity.addedAt
            )
        )
    }

    override suspend fun updateItemQuantity(itemId: String, newQuantity: Int): Result<Unit> {
        if (newQuantity <= 0) {
            shoppingListDao.deleteItemById(itemId)
            val all = shoppingListDao.getAllItemsSync()
            all.filter { it.productId == itemId }.forEach { shoppingListDao.deleteItemById(it.itemId) }
        } else {
            val existing = shoppingListDao.getItemById(itemId)
                ?: shoppingListDao.getAllItemsSync().find { it.productId == itemId }
            if (existing != null) {
                shoppingListDao.upsertItem(existing.copy(quantity = newQuantity))
            }
        }
        return Result.success(Unit)
    }

    override suspend fun toggleItemSelection(itemId: String): Result<Unit> {
        val existing = shoppingListDao.getItemById(itemId)
            ?: shoppingListDao.getAllItemsSync().find { it.productId == itemId }
        if (existing != null) {
            shoppingListDao.upsertItem(existing.copy(isSelected = !existing.isSelected))
        }
        return Result.success(Unit)
    }

    override suspend fun removeItem(itemId: String): Result<Unit> {
        shoppingListDao.deleteItemById(itemId)
        val all = shoppingListDao.getAllItemsSync()
        all.filter { it.productId == itemId }.forEach { shoppingListDao.deleteItemById(it.itemId) }
        return Result.success(Unit)
    }

    override suspend fun syncScenePlacementsToShoppingList(
        scene: RoomScene,
        allProducts: List<Product>
    ): Result<Int> {
        var count = 0
        val grouped = scene.placements.groupBy { it.productId }
        for ((productId, placements) in grouped) {
            val prod = allProducts.find { it.id == productId } ?: continue
            val itemId = "cart_${prod.id}_${scene.id}"
            val existing = shoppingListDao.getItemById(itemId)
            val targetQty = maxOf(existing?.quantity ?: 0, placements.size)
            shoppingListDao.upsertItem(
                ShoppingListItemEntity(
                    itemId = itemId,
                    productId = prod.id,
                    quantity = targetQty,
                    isSelected = true,
                    associatedSceneId = scene.id,
                    associatedPlacementId = placements.first().placementId
                )
            )
            count += placements.size
        }
        return Result.success(count)
    }
}
