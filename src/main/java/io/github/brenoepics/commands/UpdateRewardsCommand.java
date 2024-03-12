package io.github.brenoepics.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import io.github.brenoepics.Main;

public class UpdateRewardsCommand extends Command {
		public UpdateRewardsCommand(String permission, String[] keys) {
				super(permission, keys);
		}

		@Override
		public boolean handle(GameClient gameClient, String[] strings) {
				Main.getMysteryManager().loadRewards();
				gameClient.getHabbo().whisper(Emulator.getTexts().getValue("cmd_mystery_update.updated"));
				return false;
		}
}
