package io.github.brenoepics.core;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.users.subscriptions.Subscription;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import gnu.trove.map.hash.THashMap;
import io.github.brenoepics.Main;
import io.github.brenoepics.core.types.MysteryColor;
import io.github.brenoepics.core.types.MysteryItem;
import io.github.brenoepics.core.types.MysteryReward;
import io.github.brenoepics.core.types.MysteryState;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MysteryUtility {
  public static MysteryColor getRandomColor() {
    MysteryColor[] colors = MysteryColor.values();
    List<MysteryColor> mysteryColor2 = getValidColors(colors);
    int color = Emulator.getRandom().nextInt(mysteryColor2.size());

    return mysteryColor2.get(color);
  }

  public static List<MysteryColor> getValidColors(MysteryColor[] colors) {
    return Arrays.stream(colors)
        .filter(MysteryUtility::isValidColor)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public static boolean isValidColor(MysteryColor color) {
    return color != MysteryColor.NONE;
  }

  public static void switchReward(Habbo habbo, MysteryReward reward) {
    switch (reward.getType()) {
      case WALL:
      case FLOOR:
        giveHabboItem(habbo, reward.getReward());
        break;
      case EFFECT:
        habbo.getInventory().getEffectsComponent().createEffect(reward.getReward());
        break;
      case HABBO_CLUB:
        int duration = reward.getReward() * 86400;
        habbo.getHabboStats().createSubscription(Subscription.HABBO_CLUB, duration);
        break;
    }
  }

  public static void giveHabboItem(Habbo habbo, final int itemId) {
    Item rewardItem = Emulator.getGameEnvironment().getItemManager().getItem(itemId);
    if (rewardItem == null) {
      Main.LOGGER.error("Could not find item with id {}", itemId);
      return;
    }

    HabboItem newItem = createItem(habbo.getHabboInfo().getId(), rewardItem);
    if (newItem != null) {
      habbo.getInventory().getItemsComponent().addItem(newItem);
      habbo.getClient().sendResponse(new AddHabboItemComposer(newItem));
      habbo.getClient().sendResponse(new InventoryRefreshComposer());
    }
  }

  public static HabboItem createItem(int ownerId, Item rewardItem) {
    return Emulator.getGameEnvironment().getItemManager().createItem(ownerId, rewardItem, 0, 0, "");
  }

  public static THashMap<Integer, MysteryItem> loadMysteryItem(String table, String idColumn) {
    THashMap<Integer, MysteryItem> items = new THashMap<>();

    try (final Connection connection = Emulator.getDatabase().getDataSource().getConnection();
        final PreparedStatement statement =
            connection.prepareStatement("SELECT * FROM `" + table + "`");
        final ResultSet set = statement.executeQuery()) {
      while (set.next()) {
        final MysteryItem key = new MysteryItem(set);
        items.put(set.getInt(idColumn), key);
      }
    } catch (Exception e) {
      Main.LOGGER.error("[Mystery-Manager]", e);
    }

    return items;
  }

  public static void log(final MysteryItem box, final int keyOwner, final int rewardId) {
    try (final Connection connection = Emulator.getDatabase().getDataSource().getConnection();
        final PreparedStatement statement =
            connection.prepareStatement(
                "INSERT INTO `mystery_items_log` (`item_id`, `box_id`, `box_owner`, `key_owner`, `reward_id`, `timestamp`) VALUES (?, ?, ?, ?, ?, ?)")) {
      statement.setInt(1, box.getItemId());
      statement.setInt(2, box.getId());
      statement.setInt(3, box.getOwnerId());
      statement.setInt(4, keyOwner);
      statement.setInt(5, rewardId);
      statement.setInt(6, Emulator.getIntUnixTimestamp());
      statement.execute();
    } catch (SQLException e) {
      Main.LOGGER.error("[Mystery-Manager]", e);
    }
  }

  public static MysteryItem createBox(int itemId, int userId, MysteryColor color) {
    try (final Connection connection = Emulator.getDatabase().getDataSource().getConnection();
        final PreparedStatement statement =
            connection.prepareStatement(
                "INSERT INTO `mystery_boxes` (`item_id`, user_id, `color`) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
      statement.setInt(1, itemId);
      statement.setInt(2, userId);
      statement.setString(3, color.name());
      statement.execute();

      try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          return new MysteryItem(
              generatedKeys.getInt(1), color, MysteryState.WAITING, userId, itemId);
        } else {
          Main.LOGGER.error("Creating MysteryBox failed, no ID found.");
          return null;
        }
      }

    } catch (SQLException e) {
      Main.LOGGER.error("[Mystery-Manager]", e);
      return null;
    }
  }

  public static MysteryItem createKey(int userId, MysteryColor color) {
    try (final Connection connection = Emulator.getDatabase().getDataSource().getConnection();
        final PreparedStatement statement =
            connection.prepareStatement(
                "INSERT INTO `users_mystery_keys` (`user_id`, `color`, `state`) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
      statement.setInt(1, userId);
      statement.setString(2, color.name());
      statement.setString(3, MysteryState.WAITING.name());
      statement.execute();

      try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          return new MysteryItem(
              generatedKeys.getInt(1), color, MysteryState.WAITING, userId, null);
        } else {
          Main.LOGGER.error("Creating MysteryBox failed, no ID found.");
          return null;
        }
      }
    } catch (SQLException e) {
      Main.LOGGER.error("[Mystery-Manager]", e);
    }

    return null;
  }

  public static boolean habboIsValid(Habbo habbo) {
    return habbo != null
        && habbo.getHabboInfo() != null
        && habbo.getHabboInfo().getCurrentRoom() != null;
  }

  public static int checkLastKeyGen(int userId) {
    try (final Connection connection = Emulator.getDatabase().getDataSource().getConnection();
        final PreparedStatement statement =
            connection.prepareStatement(
                "SELECT `last_mystery_key` FROM `users` WHERE `id` = ? LIMIT 1")) {
      statement.setInt(1, userId);
      final ResultSet set = statement.executeQuery();
      if (set.next()) {
        return set.getInt("last_mystery_key");
      }
    } catch (Exception e) {
      Main.LOGGER.error("[Mystery-Manager]", e);
    }
    return 0;
  }

  public static void updateLastKeyGen(int userId) {
    try (final Connection connection = Emulator.getDatabase().getDataSource().getConnection();
        final PreparedStatement statement =
            connection.prepareStatement(
                "UPDATE `users` SET `last_mystery_key` = ? WHERE `id` = ?")) {
      statement.setInt(1, Emulator.getIntUnixTimestamp());
      statement.setInt(2, userId);
      statement.execute();
    } catch (Exception e) {
      Main.LOGGER.error("[Mystery-Manager]", e);
    }
  }
}
