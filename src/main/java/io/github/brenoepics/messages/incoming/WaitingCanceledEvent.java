package io.github.brenoepics.messages.incoming;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;
import io.github.brenoepics.Main;
import io.github.brenoepics.core.types.MysteryItem;
import io.github.brenoepics.core.types.MysteryState;

public class WaitingCanceledEvent extends MessageHandler {

    @Override
    public void handle() {
        int boxOwnerId = this.packet.readInt();
        Habbo habbo = this.client.getHabbo();
        if (habbo == null) return;

        MysteryItem item = Main.getMysteryManager().getKey(habbo.getHabboInfo().getId());

        if (item == null || item.getState() != MysteryState.TRADING) return;
        Main.getMysteryManager().cancelTrade(habbo, boxOwnerId);
    }
}
