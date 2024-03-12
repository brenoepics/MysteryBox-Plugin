package io.github.brenoepics.core

import com.eu.habbo.Emulator
import com.eu.habbo.habbohotel.items.Item
import com.eu.habbo.habbohotel.users.Habbo
import com.eu.habbo.habbohotel.users.HabboItem
import com.eu.habbo.habbohotel.users.subscriptions.Subscription
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer
import gnu.trove.map.hash.THashMap
import io.github.brenoepics.Main
import io.github.brenoepics.core.types.*
import java.sql.SQLException
import java.sql.Statement
import java.util.*
import java.util.stream.Collectors

object MysteryUtility {
    val randomColor: MysteryColor
        get() {
            val colors: Array<MysteryColor> = MysteryColor.entries.toTypedArray()
            val mysteryColor2 = getValidColors(colors)
            val color = Emulator.getRandom().nextInt(mysteryColor2.size)

            return mysteryColor2[color]
        }

    fun getValidColors(colors: Array<MysteryColor>): List<MysteryColor> {
        return Arrays.stream(colors).filter { obj: MysteryColor -> isValidColor(obj) }
            .collect(Collectors.toCollection { ArrayList() })
    }

    private fun isValidColor(color: MysteryColor): Boolean {
        return color != MysteryColor.NONE
    }

    fun switchReward(habbo: Habbo, reward: MysteryReward) {
        when (reward.type) {
            ProductTypeEnum.WALL, ProductTypeEnum.FLOOR -> giveHabboItem(habbo, reward.reward)
            ProductTypeEnum.EFFECT -> habbo.inventory.effectsComponent.createEffect(reward.reward)
            ProductTypeEnum.HABBO_CLUB -> {
                val duration = reward.reward * 86400
                habbo.habboStats.createSubscription(Subscription.HABBO_CLUB, duration)
            }

            ProductTypeEnum.BADGE -> TODO()
            ProductTypeEnum.GAME_TOKEN -> TODO()
            ProductTypeEnum.PET -> TODO()
            ProductTypeEnum.ROBOT -> TODO()
            null -> TODO()
        }
    }

    fun giveHabboItem(habbo: Habbo, itemId: Int) {
        val rewardItem = Emulator.getGameEnvironment().itemManager.getItem(itemId)
        if (rewardItem == null) {
            Main.LOGGER.error("Could not find item with id {}", itemId)
            return
        }

        val newItem = createItem(habbo.habboInfo.id, rewardItem)
        habbo.inventory.itemsComponent.addItem(newItem)
        habbo.client.sendResponse(AddHabboItemComposer(newItem))
        habbo.client.sendResponse(InventoryRefreshComposer())
    }

    fun createItem(ownerId: Int, rewardItem: Item?): HabboItem {
        return Emulator.getGameEnvironment().itemManager.createItem(ownerId, rewardItem, 0, 0, "")
    }

    fun loadMysteryItem(table: String, idColumn: String?): THashMap<Int, MysteryItem> {
        val items = THashMap<Int, MysteryItem>()

        try {
            Emulator.getDatabase().dataSource.connection.use { connection ->
                connection.prepareStatement("SELECT * FROM `$table`").use { statement ->
                    statement.executeQuery().use { set ->
                        while (set.next()) {
                            val key = MysteryItem(set)
                            items[set.getInt(idColumn)] = key
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Main.LOGGER.error("[Mystery-Manager]", e)
        }

        return items
    }

    fun log(box: MysteryItem, keyOwner: Int, rewardId: Int) {
        try {
            Emulator.getDatabase().dataSource.connection.use { connection ->
                connection.prepareStatement(
                    "INSERT INTO `mystery_items_log` (`item_id`, `box_id`, `box_owner`, `key_owner`, `reward_id`, `timestamp`) VALUES (?, ?, ?, ?, ?, ?)"
                ).use { statement ->
                    box.itemId?.let { statement.setInt(1, it) }
                    statement.setInt(2, box.id)
                    statement.setInt(3, box.ownerId)
                    statement.setInt(4, keyOwner)
                    statement.setInt(5, rewardId)
                    statement.setInt(6, Emulator.getIntUnixTimestamp())
                    statement.execute()
                }
            }
        } catch (e: SQLException) {
            Main.LOGGER.error("[Mystery-Manager]", e)
        }
    }

    fun createBox(itemId: Int, userId: Int, color: MysteryColor?): MysteryItem? {
        try {
            Emulator.getDatabase().dataSource.connection.use { connection ->
                connection.prepareStatement(
                    "INSERT INTO `mystery_boxes` (`item_id`, user_id, `color`) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                ).use { statement ->
                    statement.setInt(1, itemId)
                    statement.setInt(2, userId)
                    statement.setString(3, color!!.name)
                    statement.execute()
                    statement.generatedKeys.use { generatedKeys ->
                        return if (generatedKeys.next()) {
                            MysteryItem(
                                generatedKeys.getInt(1), color, MysteryState.WAITING, userId, itemId
                            )
                        } else {
                            Main.LOGGER.error("Creating MysteryBox failed, no ID found.")
                             null
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            Main.LOGGER.error("[Mystery-Manager]", e)
            return null
        }
    }

    fun createKey(userId: Int, color: MysteryColor?): MysteryItem? {
        try {
            Emulator.getDatabase().dataSource.connection.use { connection ->
                connection.prepareStatement(
                    "INSERT INTO `users_mystery_keys` (`user_id`, `color`, `state`) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                ).use { statement ->
                    statement.setInt(1, userId)
                    statement.setString(2, color!!.name)
                    statement.setString(3, MysteryState.WAITING.name)
                    statement.execute()
                    statement.generatedKeys.use { generatedKeys ->
                        return if (generatedKeys.next()) {
                            MysteryItem(
                                generatedKeys.getInt(1), color, MysteryState.WAITING, userId, null
                            )
                        } else {
                            Main.LOGGER.error("Creating MysteryBox failed, no ID found.")
                            return null
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            Main.LOGGER.error("[Mystery-Manager]", e)
        }

        return null
    }

    fun habboIsValid(habbo: Habbo?): Boolean {
        return habbo != null && habbo.habboInfo != null && habbo.habboInfo.currentRoom != null
    }

    fun checkLastKeyGen(userId: Int): Int {
        try {
            Emulator.getDatabase().dataSource.connection.use { connection ->
                connection.prepareStatement(
                    "SELECT `last_mystery_key` FROM `users` WHERE `id` = ? LIMIT 1"
                ).use { statement ->
                    statement.setInt(1, userId)
                    val set = statement.executeQuery()
                    if (set.next()) {
                        return set.getInt("last_mystery_key")
                    }
                }
            }
        } catch (e: Exception) {
            Main.LOGGER.error("[Mystery-Manager]", e)
        }
        return 0
    }

    fun updateLastKeyGen(userId: Int) {
        try {
            Emulator.getDatabase().dataSource.connection.use { connection ->
                connection.prepareStatement(
                    "UPDATE `users` SET `last_mystery_key` = ? WHERE `id` = ?"
                ).use { statement ->
                    statement.setInt(1, Emulator.getIntUnixTimestamp())
                    statement.setInt(2, userId)
                    statement.execute()
                }
            }
        } catch (e: Exception) {
            Main.LOGGER.error("[Mystery-Manager]", e)
        }
    }
}
