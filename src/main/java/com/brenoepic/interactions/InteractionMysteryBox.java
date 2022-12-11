package com.brenoepic.interactions;


import com.brenoepic.Main;
import com.brenoepic.core.messages.outgoing.MysteryBoxKeysMessageComposer;
import com.brenoepic.core.messages.outgoing.ShowMysteryBoxWaitMessageComposer;
import com.brenoepic.core.types.Colors;
import com.brenoepic.core.types.MysteryItem;
import com.brenoepic.core.types.MysteryState;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionDefault;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InteractionMysteryBox extends InteractionDefault {
    public InteractionMysteryBox(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public InteractionMysteryBox(int id, int userId, Item item, String extra, int limitedStack, int limitedSells) {
        super(id, userId, item, extra, limitedStack, limitedSells);
    }

    @Override
    public boolean isWalkable() {
        return false;
    }

    @Override
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objects) {
        return false;
    }

    @Override
    public void onPickUp(Room room) {
        this.needsUpdate(true);
        super.onPickUp(room);
    }

    @Override
    public boolean allowWiredResetState() {
        return false;
    }
    @Override
    public void onPlace(Room room) {
        if (room == null || room.getHabbo(this.getUserId()) == null) return;
        Habbo owner = room.getHabbo(this.getUserId());

        if (Main.getMysteryManager().getBox(this.getId()) == null) {
            MysteryItem box = Main.getMysteryManager().generateBoxColor(this.getId(), this.getUserId());
            MysteryItem userKey = Main.getMysteryManager().getKey(this.getUserId());
            if (box == null) {
                room.pickUpItem(this, owner);
                return;
            }
            owner.getClient().sendResponse(new MysteryBoxKeysMessageComposer(box.getColor(), userKey == null ? Colors.NONE : userKey.getColor()));
            this.updateData(room);
        }
    }

    @Override
    public void onClick(GameClient client, Room room, Object[] objects) {
        if (room == null || room.getHabbo(this.getUserId()) == null) return;
        Habbo owner = room.getHabbo(this.getUserId());
        Habbo habbo = client.getHabbo();

        //Cannot trade if owner is offline
        if (owner == null || habbo == null) return;


        MysteryItem item = this.getMysteryItem(room);
        if (item == null) return;

        //Owner can't open their own box but can confirm if key is ready
        if (client.getHabbo().getHabboInfo().getId() == owner.getHabboInfo().getId() && item.getState() == MysteryState.TRADING && Main.getMysteryManager().endTrade(item)) {
            this.updateData(room);
            return;
        }

        //If it is not the owner, check if the box is ready
        MysteryItem userKey = Main.getMysteryManager().getKey(habbo.getHabboInfo().getId());
        if (userKey == null || userKey.getColor() != item.getColor() || userKey.getOwnerId() == this.getUserId()) return;

        if (item.getState() != MysteryState.WAITING || userKey.getState() != MysteryState.WAITING) return;

        if (Main.getMysteryManager().startTrade(userKey, item)) {
            client.sendResponse(new ShowMysteryBoxWaitMessageComposer());
            this.updateData(room);
        }

    }

    public MysteryItem getMysteryItem(Room room) {
        if (Main.getMysteryManager().getBox(this.getId()) == null) {
            if (room != null)
                room.pickUpItem(this, room.getHabbo(this.getUserId()));
            Main.LOGGER.error("MysteryBox {} is null!", this.getId());
            return null;
        }
        return Main.getMysteryManager().getBox(this.getId());
    }

    public void updateData(Room room) {
        MysteryItem item = this.getMysteryItem(room);
        if (item == null) return;
        switch (item.getState()) {
            case WAITING:
                this.setExtradata(String.valueOf(item.getColor().getWaitingState()));
                break;
            case TRADING:
                this.setExtradata(String.valueOf(item.getColor().getTradingState()));
                break;
            case TRADED:
                this.setExtradata(String.valueOf(item.getColor().getTradedState()));
                break;
        }
        this.needsUpdate(true);
        if (room != null)
            room.updateItem(this);
    }
}
