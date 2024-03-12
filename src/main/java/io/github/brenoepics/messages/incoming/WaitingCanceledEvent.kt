package io.github.brenoepics.messages.incoming

import com.eu.habbo.messages.incoming.MessageHandler
import io.github.brenoepics.Main
import io.github.brenoepics.core.types.MysteryItem
import io.github.brenoepics.core.types.MysteryState

class WaitingCanceledEvent : MessageHandler() {
    override fun handle() {
        val boxOwnerId = packet.readInt()
        val habbo = client.habbo ?: return

        val item: MysteryItem = Main.mysteryManager.getKey(habbo.habboInfo.id)!!

        if (item.state != MysteryState.TRADING) return
        Main.mysteryManager.cancelTrade(habbo, boxOwnerId)
    }
}
