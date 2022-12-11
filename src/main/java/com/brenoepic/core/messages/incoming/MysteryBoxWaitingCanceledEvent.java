package com.brenoepic.core.messages.incoming;

import com.brenoepic.Main;
import com.brenoepic.core.types.MysteryItem;
import com.brenoepic.core.types.MysteryState;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.MessageHandler;

public class MysteryBoxWaitingCanceledEvent extends MessageHandler {

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
