package io.github.brenoepics.commands

import com.eu.habbo.Emulator
import com.eu.habbo.habbohotel.commands.Command
import com.eu.habbo.habbohotel.gameclients.GameClient
import io.github.brenoepics.Main

class UpdateRewardsCommand(permission: String?, keys: Array<String?>?) : Command(permission, keys) {
    override fun handle(gameClient: GameClient, strings: Array<String>): Boolean {
        Main.getMysteryManager().loadRewards()
        gameClient.habbo.whisper(Emulator.getTexts().getValue("cmd_mystery_update.updated"))
        return false
    }
}
