package io.github.brenoepics.core.types

import io.github.brenoepics.Main

/**
 * WALL class is wallId FLOOR class is floorId BADGE class is badgeId HABBO_CLUB class is ignored
 * EFFECT class is effectId GAME_TOKEN class is not implemented on ms PET class is petId ROBOT class
 *  robotId
 */
enum class ProductTypeEnum(val type: String) {
    WALL("i"), FLOOR("s"), EFFECT("e"), HABBO_CLUB("h"), BADGE("b"), GAME_TOKEN("GAME_TOKEN"), PET("p"), ROBOT("r");

    companion object {
        fun fromChar(find: String?): ProductTypeEnum? {
            for (product in entries) {
                if (product.type.equals(find, ignoreCase = true)) {
                    return product
                }
            }
            Main.LOGGER.error("[MysteryBox] Reward not found for type: {}", find)
            return null
        }
    }
}
