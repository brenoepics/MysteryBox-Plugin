package io.github.brenoepics.core;

import static io.github.brenoepics.core.MysteryUtility.*;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.MessageComposer;
import gnu.trove.map.hash.THashMap;
import io.github.brenoepics.Main;
import io.github.brenoepics.core.types.MysteryColor;
import io.github.brenoepics.core.types.MysteryItem;
import io.github.brenoepics.core.types.MysteryReward;
import io.github.brenoepics.core.types.MysteryState;
import io.github.brenoepics.interactions.InteractionMysteryBox;
import io.github.brenoepics.messages.outgoing.CancelWaitComposer;
import io.github.brenoepics.messages.outgoing.GotPrizeComposer;
import io.github.brenoepics.messages.outgoing.KeysComposer;
import java.sql.*;
import java.util.*;

public class MysteryManager {
  private final THashMap<Integer, MysteryItem> keys;
  private final THashMap<Integer, MysteryItem> boxes;
  private final THashMap<MysteryItem, Integer> trade = new THashMap<>();
  private final List<MysteryReward> rewards = new ArrayList<>();

  public MysteryManager() {
    this.keys = loadMysteryItem("users_mystery_keys", "user_id");
    this.boxes = loadMysteryItem("mystery_boxes", "item_id");
    loadRewards();
    Main.LOGGER.info(
        "[Mystery-Manager] Loaded {} keys and {} rewards successfully!",
        this.keys.size(),
        this.rewards.size());
  }

  public void loadRewards() {
    rewards.clear();
    try (final Connection connection = Emulator.getDatabase().getDataSource().getConnection();
        final PreparedStatement statement =
            connection.prepareStatement("SELECT * FROM `mystery_rewards`");
        final ResultSet set = statement.executeQuery()) {
      while (set.next()) {
        this.rewards.add(new MysteryReward(set));
      }
    } catch (Exception e) {
      Main.LOGGER.error("[Mystery-Manager]", e);
    }
  }

  public MysteryItem getKey(final int userId) {
    return this.keys.get(userId);
  }

  public MysteryItem getBox(final int itemId) {
    return this.boxes.get(itemId);
  }

  public MysteryItem getBoxByUserId(final int userId) {
    Optional<MysteryItem> box =
        this.boxes.values().stream()
            .filter(mysteryItem -> mysteryItem.getOwnerId() == userId)
            .findFirst();
    return box.orElse(null);
  }

  public Map.Entry<MysteryItem, Integer> getTradeByKeyOwner(final int ownerId) {
    return this.trade.entrySet().stream()
        .filter(trading -> trading.getValue() == ownerId)
        .findFirst()
        .orElse(null);
  }

  public MysteryItem getTradingBoxByUserId(final int boxOwnerId, final int keyUserId) {
    return this.trade.entrySet().stream()
        .filter(mysteryItem -> findTrade(boxOwnerId, keyUserId, mysteryItem))
        .map(Map.Entry::getKey)
        .findFirst()
        .orElse(null);
  }

  private static boolean findTrade(
      int boxOwnerId, int keyUserId, Map.Entry<MysteryItem, Integer> mysteryItem) {
    return mysteryItem.getKey().getOwnerId() == boxOwnerId && mysteryItem.getValue() == keyUserId;
  }

  public boolean isTrading(final int keyOwner) {
    return this.trade.containsValue(keyOwner);
  }

  public boolean areTrading(final int keyOwner, final int boxOwner) {
    return this.trade.entrySet().stream().anyMatch(entry -> findTrade(boxOwner, keyOwner, entry));
  }

  public MysteryItem generateBoxColor(final int itemId, final int userId) {
    if (this.boxes.containsKey(itemId)) {
      return this.boxes.get(itemId);
    }

    MysteryItem newBox = createBox(itemId, userId, getRandomColor());
    if (newBox == null) return null;

    this.boxes.put(itemId, newBox);
    return newBox;
  }

  public void createKey(final int userId) {
    if (this.keys.containsKey(userId)) {
      return;
    }

    MysteryItem newKey = MysteryUtility.createKey(userId, getRandomColor());
    if (newKey == null) return;

    this.keys.put(userId, newKey);
  }

  public void removeTrade(final int itemId) {
    this.trade.keySet().removeIf(mysteryItem -> mysteryItem.getItemId() == itemId);
  }

  public void deleteKey(final int userId) {
    if (!this.keys.containsKey(userId)) {
      return;
    }

    try (final Connection connection = Emulator.getDatabase().getDataSource().getConnection();
        final PreparedStatement statement =
            connection.prepareStatement("DELETE FROM `users_mystery_keys` WHERE `user_id` = ?")) {
      statement.setInt(1, userId);
      statement.execute();
      this.keys.remove(userId);
    } catch (SQLException e) {
      Main.LOGGER.error("[Mystery-Manager]", e);
    }
  }

  public void closeBox(final int itemId) {
    if (!this.boxes.containsKey(itemId)) {
      return;
    }
    this.boxes.get(itemId).setState(MysteryState.TRADED);

    try (final Connection connection = Emulator.getDatabase().getDataSource().getConnection();
        final PreparedStatement statement =
            connection.prepareStatement(
                "UPDATE `mystery_boxes` SET `state` = ? WHERE `item_id` = ?")) {
      statement.setString(1, "TRADED");
      statement.setInt(2, itemId);
      statement.execute();
    } catch (SQLException e) {
      Main.LOGGER.error("[Mystery-Manager]", e);
    }
  }

  public boolean startTrade(MysteryItem key, MysteryItem box) {
    if (this.trade.containsKey(box) || this.trade.containsValue(key.getOwnerId())) return false;

    key.setState(MysteryState.TRADING);
    box.setState(MysteryState.TRADING);
    this.trade.put(box, key.getOwnerId());
    return true;
  }

  public void cancelTrade(int keyOwner) {
    if (!this.keys.containsKey(keyOwner)) return;
    Map.Entry<MysteryItem, Integer> entry = getTradeByKeyOwner(keyOwner);
    if (entry == null) return;

    MysteryItem box = entry.getKey();
    MysteryItem key = this.getKey(keyOwner);
    key.setState(MysteryState.WAITING);
    box.setState(MysteryState.WAITING);

    Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(box.getOwnerId());
    if (!habboIsValid(habbo) || !updateItem(habbo.getHabboInfo().getCurrentRoom(), box)) {
      this.removeTrade(box.getItemId());
    }
  }

  public void cancelTrade(Habbo habbo, Integer boxOwnerId) {
    if (!this.areTrading(habbo.getHabboInfo().getId(), boxOwnerId)) return;
    Room room = habbo.getRoomUnit().getRoom();
    if (room == null) return;

    MysteryItem key = this.getKey(habbo.getHabboInfo().getId());
    MysteryItem box = this.getTradingBoxByUserId(boxOwnerId, habbo.getHabboInfo().getId());
    if (key == null || box == null) return;

    key.setState(MysteryState.WAITING);
    box.setState(MysteryState.WAITING);

    if (!updateItem(room, box)) {
      this.removeTrade(box.getItemId());
    }
  }

  private boolean updateItem(Room room, MysteryItem box) {
    HabboItem item = room.getHabboItem(box.getItemId());
    if (item == null) {
      return false;
    }
    ((InteractionMysteryBox) item).updateData(room);
    this.removeTrade(box.getItemId());
    return true;
  }

  public boolean endTrade(MysteryItem box) {
    if (!this.trade.containsKey(box)) return false;

    MysteryItem key = this.getKey(this.trade.get(box));
    if (key == null || !key.getState().equals(MysteryState.TRADING)) {
      box.setState(MysteryState.WAITING);
      return false;
    }

    Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(key.getOwnerId());
    Habbo boxOwner = Emulator.getGameEnvironment().getHabboManager().getHabbo(box.getOwnerId());
    if (habbo == null || boxOwner == null) {
      box.setState(MysteryState.WAITING);
      key.setState(MysteryState.WAITING);
      return false;
    }
    if (!habbo.getHabboInfo().getCurrentRoom().equals(boxOwner.getHabboInfo().getCurrentRoom())) {
      this.cancelTrade(habbo, boxOwner.getHabboInfo().getId());
      return false;
    }

    habbo.getClient().sendResponse(new CancelWaitComposer());
    this.closeBox(box.getItemId());
    this.deleteKey(key.getOwnerId());
    this.giveReward(box, key.getOwnerId(), boxOwner);
    this.removeTrade(box.getItemId());
    updateChallenge(habbo);
    updateChallenge(boxOwner);
    return true;
  }

  public void giveReward(MysteryItem box, int keyOwner, Habbo habbo) {
    if (!Emulator.getConfig().getBoolean("mysterybox.challenge.enabled")
        || this.getRewards().isEmpty()
        || habbo == null) return;

    MysteryReward reward = this.getRandReward();
    if (reward == null) return;

    switchReward(habbo, reward);
    log(box, keyOwner, reward.getId());

    MessageComposer composer = new GotPrizeComposer(reward.getType(), reward.getReward());
    habbo.getClient().sendResponse(composer);
  }

  public void updateChallenge(Habbo habbo) {
    if (habbo == null) return;
    MysteryItem userKey = this.getKey(habbo.getHabboInfo().getId());
    MysteryItem userBox = this.getBoxByUserId(habbo.getHabboInfo().getId());
    MysteryColor boxColor = MysteryColor.NONE;
    MysteryColor boxKey = MysteryColor.NONE;

    if (userBox != null) {
      boxColor = userBox.getColor();
    }

    if (userKey != null) {
      boxKey = userKey.getColor();
    }

    habbo.getClient().sendResponse(new KeysComposer(boxColor, boxKey));
  }

  public MysteryReward getRandReward() {
    int reward = Emulator.getRandom().nextInt(this.rewards.size());
    return this.rewards.get(reward);
  }

  public List<MysteryReward> getRewards() {
    return this.rewards;
  }

  public void dispose() {
    this.keys.clear();
    this.rewards.clear();
  }
}
