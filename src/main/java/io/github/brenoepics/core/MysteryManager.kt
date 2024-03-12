package io.github.brenoepics.core

import com.eu.habbo.Emulator
import com.eu.habbo.habbohotel.rooms.Room
import com.eu.habbo.habbohotel.users.Habbo
import com.eu.habbo.messages.outgoing.MessageComposer
import gnu.trove.map.hash.THashMap
import io.github.brenoepics.Main
import io.github.brenoepics.core.types.MysteryColor
import io.github.brenoepics.core.types.MysteryItem
import io.github.brenoepics.core.types.MysteryReward
import io.github.brenoepics.core.types.MysteryState
import io.github.brenoepics.interactions.InteractionMysteryBox
import io.github.brenoepics.messages.outgoing.CancelWaitComposer
import io.github.brenoepics.messages.outgoing.GotPrizeComposer
import io.github.brenoepics.messages.outgoing.KeysComposer
import java.sql.SQLException
import java.util.function.Function

class MysteryManager {
    private val keys: THashMap<Int?, MysteryItem?>? = MysteryUtility.loadMysteryItem("users_mystery_keys", "user_id")
    private val boxes: THashMap<Int?, MysteryItem?>? = MysteryUtility.loadMysteryItem("mystery_boxes", "item_id")
    private val trade = THashMap<MysteryItem, Int?>()
    private val rewards: MutableList<MysteryReward> = ArrayList()

    init {
        loadRewards()
        Main.LOGGER.info(
            "[Mystery-Manager] Loaded {} keys and {} rewards successfully!",
            keys.size(),
            rewards.size
        )
    }

    fun loadRewards() {
        rewards.clear()
        try {
            Emulator.getDatabase().dataSource.connection.use { connection ->
                connection.prepareStatement("SELECT * FROM `mystery_rewards`").use { statement ->
                    statement.executeQuery().use { set ->
                        while (set.next()) {
                            rewards.add(MysteryReward(set))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Main.LOGGER.error("[Mystery-Manager]", e)
        }
    }

    fun getKey(userId: Int): MysteryItem? {
        return keys!![userId]
    }

    fun getBox(itemId: Int): MysteryItem? {
        return boxes!![itemId]
    }

    fun getBoxByUserId(userId: Int): MysteryItem? {
        val box =
            boxes!!.values.stream()
                .filter { mysteryItem: MysteryItem? -> mysteryItem.getOwnerId() == userId }
                .findFirst()
        return box.orElse(null)
    }

    fun getTradeByKeyOwner(ownerId: Int): Map.Entry<MysteryItem, Int?> {
        return trade.entries.stream()
            .filter { trading: Map.Entry<MysteryItem, Int?> -> trading.value == ownerId }
            .findFirst()
            .orElse(null)
    }

    fun getTradingBoxByUserId(boxOwnerId: Int, keyUserId: Int): MysteryItem? {
        return trade.entries.stream()
            .filter { mysteryItem: Map.Entry<MysteryItem, Int?> -> findTrade(boxOwnerId, keyUserId, mysteryItem) }
            .map<MysteryItem?>(Function<Map.Entry<MysteryItem, Int?>, MysteryItem?> { java.util.Map.Entry.key })
            .findFirst()
            .orElse(null)
    }

    fun isTrading(keyOwner: Int): Boolean {
        return trade.containsValue(keyOwner)
    }

    fun areTrading(keyOwner: Int, boxOwner: Int): Boolean {
        return trade.entries.stream()
            .anyMatch { entry: Map.Entry<MysteryItem, Int?> -> findTrade(boxOwner, keyOwner, entry) }
    }

    fun generateBoxColor(itemId: Int, userId: Int): MysteryItem? {
        if (boxes!!.containsKey(itemId)) {
            return boxes[itemId]
        }

        val newBox = MysteryUtility.createBox(itemId, userId, MysteryUtility.getRandomColor()) ?: return null

        boxes[itemId] = newBox
        return newBox
    }

    fun createKey(userId: Int) {
        if (keys!!.containsKey(userId)) {
            return
        }

        val newKey = MysteryUtility.createKey(userId, MysteryUtility.getRandomColor()) ?: return

        keys[userId] = newKey
    }

    fun removeTrade(itemId: Int) {
        trade.keys.removeIf { mysteryItem: MysteryItem -> mysteryItem.itemId == itemId }
    }

    fun deleteKey(userId: Int) {
        if (!keys!!.containsKey(userId)) {
            return
        }

        try {
            Emulator.getDatabase().dataSource.connection.use { connection ->
                connection.prepareStatement("DELETE FROM `users_mystery_keys` WHERE `user_id` = ?").use { statement ->
                    statement.setInt(1, userId)
                    statement.execute()
                    keys.remove(userId)
                }
            }
        } catch (e: SQLException) {
            Main.LOGGER.error("[Mystery-Manager]", e)
        }
    }

    fun closeBox(itemId: Int) {
        if (!boxes!!.containsKey(itemId)) {
            return
        }
        boxes[itemId].setState(MysteryState.TRADED)

        try {
            Emulator.getDatabase().dataSource.connection.use { connection ->
                connection.prepareStatement(
                    "UPDATE `mystery_boxes` SET `state` = ? WHERE `item_id` = ?"
                ).use { statement ->
                    statement.setString(1, "TRADED")
                    statement.setInt(2, itemId)
                    statement.execute()
                }
            }
        } catch (e: SQLException) {
            Main.LOGGER.error("[Mystery-Manager]", e)
        }
    }

    fun startTrade(key: MysteryItem, box: MysteryItem): Boolean {
        if (trade.containsKey(box) || trade.containsValue(key.ownerId)) return false

        key.state = MysteryState.TRADING
        box.state = MysteryState.TRADING
        trade[box] = key.ownerId
        return true
    }

    fun cancelTrade(keyOwner: Int) {
        if (!keys!!.containsKey(keyOwner)) return
        val entry = getTradeByKeyOwner(keyOwner) ?: return

        val box = entry.key
        val key = this.getKey(keyOwner)
        key.setState(MysteryState.WAITING)
        box.state = MysteryState.WAITING

        val habbo = Emulator.getGameEnvironment().habboManager.getHabbo(box.ownerId)
        if (!MysteryUtility.habboIsValid(habbo) || !updateItem(habbo.habboInfo.currentRoom, box)) {
            this.removeTrade(box.itemId)
        }
    }

    fun cancelTrade(habbo: Habbo, boxOwnerId: Int) {
        if (!this.areTrading(habbo.habboInfo.id, boxOwnerId)) return
        val room = habbo.roomUnit.room ?: return

        val key = this.getKey(habbo.habboInfo.id)
        val box = this.getTradingBoxByUserId(boxOwnerId, habbo.habboInfo.id)
        if (key == null || box == null) return

        key.state = MysteryState.WAITING
        box.state = MysteryState.WAITING

        if (!updateItem(room, box)) {
            this.removeTrade(box.itemId)
        }
    }

    private fun updateItem(room: Room, box: MysteryItem): Boolean {
        val item = room.getHabboItem(box.itemId) ?: return false
        (item as InteractionMysteryBox).updateData(room)
        this.removeTrade(box.itemId)
        return true
    }

    fun endTrade(box: MysteryItem): Boolean {
        if (!trade.containsKey(box)) return false

        val key = this.getKey(trade[box]!!)
        if (key == null || key.state != MysteryState.TRADING) {
            box.state = MysteryState.WAITING
            return false
        }

        val habbo = Emulator.getGameEnvironment().habboManager.getHabbo(key.ownerId)
        val boxOwner = Emulator.getGameEnvironment().habboManager.getHabbo(box.ownerId)
        if (habbo == null || boxOwner == null) {
            box.state = MysteryState.WAITING
            key.state = MysteryState.WAITING
            return false
        }
        if (habbo.habboInfo.currentRoom != boxOwner.habboInfo.currentRoom) {
            this.cancelTrade(habbo, boxOwner.habboInfo.id)
            return false
        }

        habbo.client.sendResponse(CancelWaitComposer())
        this.closeBox(box.itemId)
        this.deleteKey(key.ownerId)
        this.giveReward(box, key.ownerId, boxOwner)
        this.removeTrade(box.itemId)
        updateChallenge(habbo)
        updateChallenge(boxOwner)
        return true
    }

    fun giveReward(box: MysteryItem, keyOwner: Int, habbo: Habbo?) {
        if ((!Emulator.getConfig().getBoolean("mysterybox.challenge.enabled")
                    || getRewards().isEmpty()) || habbo == null
        ) return

        val reward = this.randReward ?: return

        MysteryUtility.switchReward(habbo, reward)
        MysteryUtility.log(box, keyOwner, reward.id)

        val composer: MessageComposer = GotPrizeComposer(reward.type, reward.reward)
        habbo.client.sendResponse(composer)
    }

    fun updateChallenge(habbo: Habbo?) {
        if (habbo == null) return
        val userKey = this.getKey(habbo.habboInfo.id)
        val userBox = this.getBoxByUserId(habbo.habboInfo.id)
        var boxColor: MysteryColor? = MysteryColor.NONE
        var boxKey: MysteryColor? = MysteryColor.NONE

        if (userBox != null) {
            boxColor = userBox.color
        }

        if (userKey != null) {
            boxKey = userKey.color
        }

        habbo.client.sendResponse(KeysComposer(boxColor!!, boxKey!!))
    }

    val randReward: MysteryReward
        get() {
            val reward = Emulator.getRandom().nextInt(rewards.size)
            return rewards[reward]
        }

    fun getRewards(): List<MysteryReward> {
        return this.rewards
    }

    fun dispose() {
        keys!!.clear()
        rewards.clear()
    }

    companion object {
        private fun findTrade(
            boxOwnerId: Int, keyUserId: Int, mysteryItem: Map.Entry<MysteryItem, Int?>
        ): Boolean {
            return mysteryItem.key.ownerId == boxOwnerId && mysteryItem.value == keyUserId
        }
    }
}
