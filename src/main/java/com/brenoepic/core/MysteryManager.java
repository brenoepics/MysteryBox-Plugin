package com.brenoepic.core;

import com.brenoepic.Main;
import com.brenoepic.core.messages.outgoing.CancelMysteryBoxWaitMessageComposer;
import com.brenoepic.core.messages.outgoing.GotMysteryBoxPrizeMessageComposer;
import com.brenoepic.core.messages.outgoing.MysteryBoxKeysMessageComposer;
import com.brenoepic.core.types.*;
import com.brenoepic.interactions.InteractionMysteryBox;
import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.users.subscriptions.SubscriptionHabboClub;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import gnu.trove.map.hash.THashMap;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class MysteryManager {
    private final THashMap<Integer, MysteryItem> keys;
    private final THashMap<Integer, MysteryItem> boxes;
    private final THashMap<MysteryItem, Integer> trade = new THashMap<>();
    private final List<MysteryReward> rewards = new ArrayList<>();

    public MysteryManager() {
        this.keys = loadMysteryItem("users_mystery_keys", "user_id");
        this.boxes = loadMysteryItem("mystery_boxes", "item_id");
        loadRewards();
        Main.LOGGER.info("[Mystery-Manager] Loaded {} keys and {} rewards successfully!", this.keys.size(), this.rewards.size());
    }

    public THashMap<Integer, MysteryItem> loadMysteryItem(String table, String idColumn) {
        THashMap<Integer, MysteryItem> items = new THashMap<>();

        try (final Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement("SELECT * FROM `" + table + "`");
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

    public void loadRewards() {
        rewards.clear();
        try (final Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement("SELECT * FROM `mystery_rewards`");
             final ResultSet set = statement.executeQuery()) {
            while (set.next()) {
                this.rewards.add(new MysteryReward(set));
            }
        } catch (Exception e) {
            Main.LOGGER.error("[Mystery-Manager]", e);
        }
    }

    public void log(final MysteryItem box, final int keyOwner, final int rewardId) {
        try (final Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement("INSERT INTO `mystery_items_log` (`item_id`, `box_id`, `box_owner`, `key_owner`, `reward_id`, `timestamp`) VALUES (?, ?, ?, ?, ?, ?)")) {
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

    public MysteryItem getKey(final int userId) {
        return this.keys.get(userId);
    }

    public MysteryItem getBox(final int itemId) {
        return this.boxes.get(itemId);
    }

    public MysteryItem getBoxByUserId(final int userId) {
        Optional<MysteryItem> box = this.boxes.values().stream().filter(mysteryItem -> mysteryItem.getOwnerId() == userId).findFirst();
        return box.orElse(null);
    }

    public MysteryItem getTradingBoxByUserId(final int boxOwnerId, final int keyUserId) {
        return this.trade.entrySet().stream().filter(mysteryItem -> mysteryItem.getKey().getOwnerId() == boxOwnerId && mysteryItem.getValue() == keyUserId).map(Map.Entry::getKey).findFirst().orElse(null);
    }

    public boolean areTrading(final int keyOwner, final int boxOwner) {
        return this.trade.entrySet().stream().anyMatch(entry -> entry.getKey().getOwnerId() == boxOwner && entry.getValue() == keyOwner);
    }

    public MysteryItem generateBoxColor(final int itemId, final int userId) {
        if (this.boxes.containsKey(itemId)) {
            return this.boxes.get(itemId);
        }

        final Colors color = this.getRandomColor();

        try (final Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement("INSERT INTO `mystery_boxes` (`item_id`, user_id, `color`) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, itemId);
            statement.setInt(2, userId);
            statement.setString(3, color.name());
            statement.execute();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    final MysteryItem box = new MysteryItem(generatedKeys.getInt(1), color, MysteryState.WAITING, userId, itemId);
                    this.boxes.put(itemId, box);
                    return box;
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

    public void removeTrade(final int itemId) {
        this.trade.keySet().removeIf(mysteryItem -> mysteryItem.getItemId() == itemId);
    }

    public void deleteKey(final int userId) {
        if (!this.keys.containsKey(userId)) {
            return;
        }

        try (final Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             final PreparedStatement statement = connection.prepareStatement("DELETE FROM `users_mystery_keys` WHERE `user_id` = ?")) {
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
             final PreparedStatement statement = connection.prepareStatement("UPDATE `mystery_boxes` SET `state` = ? WHERE `item_id` = ?")) {
            statement.setString(1, "TRADED");
            statement.setInt(2, itemId);
            statement.execute();
        } catch (SQLException e) {
            Main.LOGGER.error("[Mystery-Manager]", e);
        }
    }

    public Colors getRandomColor() {
        Colors[] colors = Colors.values();
        ArrayList<Colors> colors2 = Arrays.stream(colors).filter(color -> color != Colors.NONE).collect(Collectors.toCollection(ArrayList::new));
        int color = Emulator.getRandom().nextInt(colors2.size());

        return colors2.get(color);
    }

    public boolean startTrade(MysteryItem key, MysteryItem box) {
        if (this.trade.containsKey(box) || this.trade.containsValue(key.getOwnerId())) return false;

        key.setState(MysteryState.TRADING);
        box.setState(MysteryState.TRADING);
        this.trade.put(box, key.getOwnerId());
        return true;
    }

    public void cancelTrade(Habbo habbo, Integer boxOwnerId) {
        if (!this.areTrading(habbo.getHabboInfo().getId(), boxOwnerId))
            return;
        Room room = habbo.getRoomUnit().getRoom();
        if (room == null) return;

        MysteryItem key = this.getKey(habbo.getHabboInfo().getId());
        MysteryItem box = this.getTradingBoxByUserId(boxOwnerId, habbo.getHabboInfo().getId());
        if (key == null || box == null) return;

        key.setState(MysteryState.WAITING);
        box.setState(MysteryState.WAITING);

        HabboItem item = room.getHabboItem(box.getItemId());
        if (item == null) {
            return;
        }
        ((InteractionMysteryBox) item).updateData(room);
        this.removeTrade(box.getItemId());
    }

    public boolean endTrade(MysteryItem box) {
        if (!this.trade.containsKey(box)) return false;

        MysteryItem key = this.getKey(this.trade.get(box));
        if (key == null || !key.getState().equals(MysteryState.TRADING)) return false;

        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(key.getOwnerId());
        Habbo boxOwner = Emulator.getGameEnvironment().getHabboManager().getHabbo(box.getOwnerId());
        if (habbo == null || boxOwner == null) return false;

        habbo.getClient().sendResponse(new CancelMysteryBoxWaitMessageComposer());
        this.closeBox(box.getItemId());
        this.deleteKey(key.getOwnerId());
        this.giveReward(box, key.getOwnerId(), boxOwner);
        this.removeTrade(box.getItemId());
        updateChallenge(habbo);
        updateChallenge(boxOwner);
        return true;
    }

    public void giveReward(MysteryItem box, int keyOwner, Habbo habbo) {
        if (!Emulator.getConfig().getBoolean("mysterybox.challenge.enabled") || this.getRewards().isEmpty() || habbo == null) return;

        MysteryReward reward = this.getRandReward();
        if (reward == null) return;

        switch (reward.getType()) {
            case WALL:
            case FLOOR:
                giveHabboItem(habbo, reward.getReward());
                break;
            case EFFECT:
                habbo.getInventory().getEffectsComponent().createEffect(reward.getReward());
                break;
            case HABBO_CLUB:
                habbo.getHabboStats().createSubscription(SubscriptionHabboClub.HABBO_CLUB, reward.getReward() * 86400);
                break;
        }
        this.log(box, keyOwner,reward.getId());
        habbo.getClient().sendResponse(new GotMysteryBoxPrizeMessageComposer(reward.getType(), reward.getReward()));

    }
    public void giveHabboItem(Habbo habbo, final int itemId){
        Item rewardItem = Emulator.getGameEnvironment().getItemManager().getItem(itemId);
        if (rewardItem == null) {
            Main.LOGGER.error("Could not find item with id {}", itemId);
            return;
        }

        HabboItem newItem = Emulator.getGameEnvironment().getItemManager().createItem(habbo.getHabboInfo().getId(), rewardItem, 0, 0, "");
        if (newItem != null) {
            habbo.getInventory().getItemsComponent().addItem(newItem);
            habbo.getClient().sendResponse(new AddHabboItemComposer(newItem));
            habbo.getClient().sendResponse(new InventoryRefreshComposer());
        }
    }


    public void updateChallenge(Habbo habbo) {
        if (habbo == null) return;
        MysteryItem userKey = this.getKey(habbo.getHabboInfo().getId());
        MysteryItem userBox = this.getBoxByUserId(habbo.getHabboInfo().getId());
        Colors boxColor = Colors.NONE;
        Colors boxKey = Colors.NONE;

        if (userBox != null) {
            boxColor = userBox.getColor();
        }
        if (userKey != null) {
            boxKey = userKey.getColor();
        }

        habbo.getClient().sendResponse(new MysteryBoxKeysMessageComposer(boxColor, boxKey));
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
        //todo dispose;
    }
}
