package io.github.brenoepics.interactions;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionDefault;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.MessageComposer;
import io.github.brenoepics.Main;
import io.github.brenoepics.messages.outgoing.KeysComposer;
import io.github.brenoepics.messages.outgoing.ShowWaitMessageComposer;
import io.github.brenoepics.core.types.MysteryColor;
import io.github.brenoepics.core.types.MysteryItem;
import io.github.brenoepics.core.types.MysteryState;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jetbrains.annotations.Nullable;

public class InteractionMysteryBox extends InteractionDefault {
  public InteractionMysteryBox(ResultSet set, Item baseItem) throws SQLException {
    super(set, baseItem);
  }

  public InteractionMysteryBox(
      int id, int userId, Item item, String extra, int limitedStack, int limitedSells) {
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
    if (isValidBox(room)) return;
    Habbo owner = room.getHabbo(this.getUserId());

    MysteryItem box = Main.getMysteryManager().generateBoxColor(this.getId(), this.getUserId());
    MysteryItem userKey = Main.getMysteryManager().getKey(this.getUserId());
    if (box == null) {
      room.pickUpItem(this, owner);
      return;
    }

    MessageComposer composer =
        new KeysComposer(box.getColor(), getBoxKey(userKey));
    owner.getClient().sendResponse(composer);
    this.updateData(room);
  }

  private boolean isValidBox(Room room) {
    return room == null
        || room.getHabbo(this.getUserId()) == null
        || Main.getMysteryManager().getBox(this.getId()) != null;
  }

  private static MysteryColor getBoxKey(MysteryItem userKey) {
    return userKey == null ? MysteryColor.NONE : userKey.getColor();
  }

  @Override
  public void onClick(GameClient client, Room room, Object[] objects) {
    if (room == null || room.getHabbo(this.getUserId()) == null) return;
    Habbo owner = room.getHabbo(this.getUserId());
    Habbo habbo = client.getHabbo();

    CheckResult result = canTrade(client, room, owner, habbo);
    if (result == null) return;

    if (Main.getMysteryManager().startTrade(result.userKey, result.item)) {
      client.sendResponse(new ShowWaitMessageComposer());
      this.updateData(room);
    }
  }

  @Nullable
  private CheckResult canTrade(GameClient client, Room room, Habbo owner, Habbo habbo) {
    // Cannot trade if the owner is offline
    if (owner == null || habbo == null) return null;

    MysteryItem item = this.getMysteryItem(room);
    if (item == null) return null;

    if (isOwner(client, owner, item)) {
      this.updateData(room);
      return null;
    }

    MysteryItem userKey = Main.getMysteryManager().getKey(habbo.getHabboInfo().getId());
    if (notReady(userKey, item)) return null;

    if (isBeingTraded(item, userKey)) return null;
    return new CheckResult(item, userKey);
  }

  private static class CheckResult {
    public final MysteryItem item;
    public final MysteryItem userKey;

    public CheckResult(MysteryItem item, MysteryItem userKey) {
      this.item = item;
      this.userKey = userKey;
    }
  }

  private static boolean isBeingTraded(MysteryItem item, MysteryItem userKey) {
    return item.getState() != MysteryState.WAITING || userKey.getState() != MysteryState.WAITING;
  }

  private boolean notReady(MysteryItem userKey, MysteryItem item) {
    return userKey == null
        || userKey.getColor() != item.getColor()
        || userKey.getOwnerId() == this.getUserId();
  }

  private static boolean isOwner(GameClient client, Habbo owner, MysteryItem item) {
    return client.getHabbo().getHabboInfo().getId() == owner.getHabboInfo().getId()
        && item.getState() == MysteryState.TRADING
        && Main.getMysteryManager().endTrade(item);
  }

  public MysteryItem getMysteryItem(Room room) {
    if (Main.getMysteryManager().getBox(this.getId()) == null) {
      handleNullBox(room);
      return null;
    }

    return Main.getMysteryManager().getBox(this.getId());
  }

  private void handleNullBox(Room room) {
    if (room != null) {
      room.pickUpItem(this, room.getHabbo(this.getUserId()));
    }

    Main.LOGGER.error("MysteryBox {} is null!", this.getId());
  }

  public void updateData(Room room) {
    MysteryItem item = this.getMysteryItem(room);
    if (item == null) return;

    setExtraData(item);
    this.needsUpdate(true);

    if (room != null) {
      room.updateItem(this);
    }
  }

  private void setExtraData(MysteryItem item) {
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
  }
}
