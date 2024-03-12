package io.github.brenoepics.interactions

import com.eu.habbo.habbohotel.gameclients.GameClient
import com.eu.habbo.habbohotel.items.Item
import com.eu.habbo.habbohotel.items.interactions.InteractionDefault
import com.eu.habbo.habbohotel.rooms.Room
import com.eu.habbo.habbohotel.rooms.RoomUnit
import com.eu.habbo.habbohotel.users.Habbo
import com.eu.habbo.messages.outgoing.MessageComposer
import io.github.brenoepics.Main
import io.github.brenoepics.core.types.MysteryColor
import io.github.brenoepics.core.types.MysteryItem
import io.github.brenoepics.core.types.MysteryState
import io.github.brenoepics.messages.outgoing.KeysComposer
import io.github.brenoepics.messages.outgoing.ShowWaitMessageComposer
import java.sql.ResultSet

class InteractionMysteryBox : InteractionDefault {
    constructor(set: ResultSet?, baseItem: Item?) : super(set, baseItem)

    constructor(id: Int, userId: Int, item: Item?, extra: String?, limitedStack: Int, limitedSells: Int) : super(
        id, userId, item, extra, limitedStack, limitedSells
    )

    override fun isWalkable(): Boolean {
        return false
    }

    override fun canWalkOn(roomUnit: RoomUnit, room: Room, objects: Array<Any>): Boolean {
        return false
    }

    override fun onPickUp(room: Room) {
        this.needsUpdate(true)
        super.onPickUp(room)
    }

    override fun allowWiredResetState(): Boolean {
        return false
    }

    override fun onPlace(room: Room) {
        if (isValidBox(room)) return
        val owner = room.getHabbo(this.userId)

        val box: MysteryItem = Main.mysteryManager.generateBoxColor(this.id, this.userId)!!
        val userKey: MysteryItem = Main.mysteryManager.getKey(this.userId)!!

        val composer: MessageComposer = KeysComposer(box.color!!, getBoxKey(userKey)!!)
        owner.client.sendResponse(composer)
        this.updateData(room)
    }

    private fun isValidBox(room: Room?): Boolean {
        return room?.getHabbo(this.userId) == null || Main.mysteryManager.getBox(this.id) != null
    }

    override fun onClick(client: GameClient, room: Room, objects: Array<Any>) {
        if (room.getHabbo(this.userId) == null) return
        val owner = room.getHabbo(this.userId)
        val habbo = client.habbo

        val result = canTrade(client, room, owner, habbo)
        if (result == null) {
            this.updateData(room)
            return
        }

        if (Main.mysteryManager.startTrade(result.userKey, result.item)) {
            client.sendResponse(ShowWaitMessageComposer())
            this.updateData(room)
        }
    }

    private fun canTrade(client: GameClient, room: Room, owner: Habbo?, habbo: Habbo?): CheckResult? {
        // Cannot trade if the owner is offline
        if (owner == null || habbo == null) return null

        val item = this.getMysteryItem(room) ?: return null

        if (isOwner(client, owner, item)) {
            this.updateData(room)
            return null
        }

        val userKey: MysteryItem = Main.mysteryManager.getKey(habbo.habboInfo.id)!!
        if (notReady(userKey, item)) return null

        if (isBeingTraded(item, userKey)) return null
        return CheckResult(item, userKey)
    }

    private class CheckResult(val item: MysteryItem, val userKey: MysteryItem)

    private fun notReady(userKey: MysteryItem?, item: MysteryItem): Boolean {
        return (userKey == null || userKey.color != item.color) || userKey.ownerId == this.userId
    }

    fun getMysteryItem(room: Room?): MysteryItem? {
        if (Main.mysteryManager.getBox(this.id) == null) {
            handleNullBox(room)
            return null
        }

        return Main.mysteryManager.getBox(this.id)
    }

    private fun handleNullBox(room: Room?) {
        room?.pickUpItem(this, room.getHabbo(this.userId))

        Main.LOGGER.error("MysteryBox {} is null!", this.id)
    }

    fun updateData(room: Room?) {
        val item = this.getMysteryItem(room) ?: return

        setExtraData(item)
        this.needsUpdate(true)

        room?.updateItem(this)
    }

    private fun setExtraData(item: MysteryItem) {
        when (item.state) {
            MysteryState.WAITING -> this.extradata = item.color!!.waitingState.toString()
            MysteryState.TRADING -> this.extradata = item.color!!.tradingState.toString()
            MysteryState.TRADED -> this.extradata = item.color!!.tradedState.toString()
        }
    }

    companion object {
        private fun getBoxKey(userKey: MysteryItem?): MysteryColor? {
            return if (userKey == null) MysteryColor.NONE else userKey.color
        }

        private fun isBeingTraded(item: MysteryItem, userKey: MysteryItem): Boolean {
            return item.state != MysteryState.WAITING || userKey.state != MysteryState.WAITING
        }

        private fun isOwner(client: GameClient, owner: Habbo, item: MysteryItem): Boolean {
            return client.habbo.habboInfo.id == owner.habboInfo.id && item.state == MysteryState.TRADING && Main.mysteryManager.endTrade(
                    item
                )
        }
    }
}
