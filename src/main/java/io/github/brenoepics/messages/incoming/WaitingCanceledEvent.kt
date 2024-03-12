package io.github.brenoepics.messages.incoming

import com.eu.habbo.messages.incoming.MessageHandler
import io.github.brenoepics.Main
import io.github.brenoepics.core.types.MysteryItem
import io.github.brenoepics.core.types.MysteryState

class WaitingCanceledEvent : MessageHandler() {
    override fun handle() {
        val boxOwnerId = packet.readInt()
        val habbo = client.habbo ?: return

        val item: MysteryItem = Main.getMysteryManager().getKey(habbo.habboInfo.id)

        if (item == null || item.state != MysteryState.TRADING) return
        Main.getMysteryManager().cancelTrade(habbo, boxOwnerId)
    }
}
