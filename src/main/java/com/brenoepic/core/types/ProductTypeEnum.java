package com.brenoepic.core.types;

/**
 * WALL class is wallId
 * FLOOR class is floorId
 * BADGE class is badgeId
 * HABBO_CLUB class is ignored
 * EFFECT class is effectId
 * GAME_TOKEN class is not implemented on ms
 * PET class is petId
 * ROBOT class is robotId
 */
public enum ProductTypeEnum
{
    WALL("i"),
    FLOOR("s"),
    EFFECT("e"),
    HABBO_CLUB("h"),
    BADGE("b"),
    GAME_TOKEN("GAME_TOKEN"),
    PET("p"),
    ROBOT("r");
    private final String type;

    ProductTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
