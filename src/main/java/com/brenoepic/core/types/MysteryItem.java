package com.brenoepic.core.types;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MysteryItem {
    private final int id;
    private Integer itemId;
    private Integer ownerId;
    private final Colors color;
    private MysteryState state;

    public MysteryItem(ResultSet set) throws Exception {
        this.id = set.getInt("id");
        this.ownerId = set.getInt("user_id");
        try {
            this.itemId = set.getInt("item_id");
        } catch (SQLException ignored) {
            this.itemId = null;
        }
        this.color = Colors.valueOf(set.getString("color").toUpperCase());
        this.state = MysteryState.valueOf(set.getString("state").toUpperCase());
    }

    public MysteryItem(int id, Colors color, MysteryState state, Integer ownerId, Integer itemId) {
        this.id = id;
        this.color = color;
        this.state = state;
        this.ownerId = ownerId;
        this.itemId = itemId;
    }

    public int getId() {
        return this.id;
    }

    public Integer getItemId() {
        return this.itemId;
    }

    public Colors getColor() {
        return this.color;
    }

    public MysteryState getState() {
        return this.state;
    }

    public Integer getOwnerId() {
        return this.ownerId;
    }

    public void setState(MysteryState state) {
        this.state = state;
    }
}
