package io.github.brenoepics

import com.eu.habbo.Emulator
import com.eu.habbo.habbohotel.commands.CommandHandler
import com.eu.habbo.habbohotel.items.ItemInteraction
import com.eu.habbo.habbohotel.users.Habbo
import com.eu.habbo.plugin.EventHandler
import com.eu.habbo.plugin.EventListener
import com.eu.habbo.plugin.HabboPlugin
import com.eu.habbo.plugin.events.emulator.EmulatorLoadItemsManagerEvent
import com.eu.habbo.plugin.events.emulator.EmulatorLoadedEvent
import com.eu.habbo.plugin.events.users.UserDisconnectEvent
import com.eu.habbo.plugin.events.users.UserExitRoomEvent
import com.eu.habbo.plugin.events.users.UserLoginEvent
import io.github.brenoepics.commands.UpdateRewardsCommand
import io.github.brenoepics.core.MysteryManager
import io.github.brenoepics.core.MysteryUtility
import io.github.brenoepics.interactions.InteractionMysteryBox
import io.github.brenoepics.messages.Incoming
import io.github.brenoepics.messages.incoming.WaitingCanceledEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused", "UNUSED_PARAMETER")
class Main : HabboPlugin(), EventListener {
    override fun onEnable() {
        Emulator.getPluginManager().registerEvents(this, this)
    }

    override fun onDisable() {
        mysteryManager.dispose()
        LOGGER.info("[MysteryBox Plugin] was successfully Unloaded!")
    }

    override fun hasPermission(habbo: Habbo, s: String): Boolean {
        return false
    }

    @EventHandler
    @Throws(Exception::class)
    fun onEmulatorLoaded(event: EmulatorLoadedEvent?) {
        // register incoming message
        Emulator.getGameServer().packetManager.registerHandler(
            Incoming.MYSTERY_BOX_WAITING_CANCELED_MESSAGE_COMPOSER, WaitingCanceledEvent::class.java
        )

        Emulator.getConfig().register("mysterybox.challenge.enabled", "1")
        Emulator.getConfig().register("mysterybox.key.onlogin", "1")
        Emulator.getConfig().register("mysterybox.key.generate.time", "86400")

        Emulator.getTexts().register("cmd_mystery_update.updated", "Rewards updated successfully!")
        Emulator.getTexts().register("cmd_mystery_update.keys", "mysterybox_update")

        CommandHandler.addCommand(UpdateRewardsCommand("cmd_mystery_update",
            Emulator.getTexts().getValue("cmd_mystery_update.keys").split(";".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()))
        LOGGER.info("[MysteryBox Plugin] was successfully Loaded!")
    }

    @EventHandler
    fun onUserLoginEvent(e: UserLoginEvent) {
        if (e.habbo == null) return
        if (Emulator.getConfig().getBoolean("mysterybox.key.onlogin", true)) {
            val lastKey = MysteryUtility.checkLastKeyGen(e.habbo.habboInfo.id)
            val time = Emulator.getConfig().getInt("mysterybox.key.generate.time")
            if (Emulator.getIntUnixTimestamp() - lastKey >= time) {
                mysteryManager.createKey(e.habbo.habboInfo.id)
                MysteryUtility.updateLastKeyGen(e.habbo.habboInfo.id)
            }
        }
        mysteryManager.updateChallenge(e.habbo)
    }

    @EventHandler
    fun onUserExitRoom(e: UserExitRoomEvent) {
        if (e.habbo == null) return
        if (mysteryManager.isTrading(e.habbo.habboInfo.id)) {
            mysteryManager.cancelTrade(e.habbo.habboInfo.id)
        }
    }

    @EventHandler
    fun onUserDisconnect(e: UserDisconnectEvent) {
        if (e.habbo == null) return
        if (mysteryManager.isTrading(e.habbo.habboInfo.id)) {
            mysteryManager.cancelTrade(e.habbo.habboInfo.id)
        }
    }

    @EventHandler
    fun onLoadItemsManager(e: EmulatorLoadItemsManagerEvent?) {
        Emulator.getGameEnvironment().itemManager.addItemInteraction(
            ItemInteraction(
                "mysterybox", InteractionMysteryBox::class.java
            )
        )
    }

    companion object {
        @JvmField
        val LOGGER: Logger = LoggerFactory.getLogger(Main::class.java)
        val mysteryManager: MysteryManager = MysteryManager()

        @JvmStatic
        fun main(args: Array<String>) {
            println("Hello world!")
        }
    }
}
