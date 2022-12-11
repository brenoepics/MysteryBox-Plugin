package com.brenoepic.core.types;

import java.sql.ResultSet;

public class MysteryReward {
    private final int id;
    private final ProductTypeEnum type;
    private final int reward;

    public MysteryReward(ResultSet set) throws Exception {
        this.id = set.getInt("id");
        this.type = ProductTypeEnum.valueOf(set.getString("type").toUpperCase());
        this.reward = set.getInt("reward");
    }

    public int getId() {
        return this.id;
    }

    public ProductTypeEnum getType() {
        return type;
    }

    public int getReward() {
        return this.reward;
    }
}
