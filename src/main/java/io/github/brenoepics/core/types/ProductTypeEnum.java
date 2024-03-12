package io.github.brenoepics.core.types;

import io.github.brenoepics.Main;

/**
 * WALL class is wallId FLOOR class is floorId BADGE class is badgeId HABBO_CLUB class is ignored
 * EFFECT class is effectId GAME_TOKEN class is not implemented on ms PET class is petId ROBOT class
 * is robotId
 */
public enum ProductTypeEnum {
		WALL("i"), FLOOR("s"), EFFECT("e"), HABBO_CLUB("h"), BADGE("b"), GAME_TOKEN("GAME_TOKEN"), PET("p"), ROBOT("r");
		private final String type;

		ProductTypeEnum(String type) {
				this.type = type;
		}

		public static ProductTypeEnum fromChar(String find) {
				for (ProductTypeEnum product : values()) {
						if (product.getType().equalsIgnoreCase(find)) {
								return product;
						}
				}
				Main.LOGGER.error("[MysteryBox] Reward not found for type: {}", find);
				return null;
		}

		public String getType() {
				return this.type;
		}
}
