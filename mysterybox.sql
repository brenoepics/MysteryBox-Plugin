CREATE TABLE IF NOT EXISTS `mystery_boxes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `item_id` int(255) NOT NULL,
  `user_id` int(255) NOT NULL,
  `color` enum('RED','PURPLE','GREEN','LILAC','YELLOW','TURQUOISE','ORANGE','BLUE') COLLATE armscii8_bin NOT NULL,
  `state` enum('WAITING','TRADED') COLLATE armscii8_bin NOT NULL DEFAULT 'WAITING',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `user_id` (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=armscii8 COLLATE=armscii8_bin ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `mystery_items_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `item_id` int(11) NOT NULL DEFAULT 0,
  `box_id` int(11) NOT NULL DEFAULT 0,
  `box_owner` int(11) NOT NULL DEFAULT 0,
  `key_owner` int(11) NOT NULL DEFAULT 0,
  `reward_id` int(11) NOT NULL DEFAULT 0,
  `timestamp` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=armscii8 COLLATE=armscii8_bin;

CREATE TABLE IF NOT EXISTS `mystery_rewards` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` enum('WALL','FLOOR','EFFECT','HABBO_CLUB') COLLATE armscii8_bin NOT NULL DEFAULT 'FLOOR',
  `reward` int(11) NOT NULL DEFAULT 0 COMMENT 'RewardId, ClubDays or EnableId',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=armscii8 COLLATE=armscii8_bin;


CREATE TABLE IF NOT EXISTS `users_mystery_keys` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `color` enum('RED','PURPLE','GREEN','LILAC','YELLOW','TURQUOISE','ORANGE','BLUE') COLLATE armscii8_bin NOT NULL,
  `state` enum('WAITING','TRADED') COLLATE armscii8_bin NOT NULL DEFAULT 'WAITING',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=armscii8 COLLATE=armscii8_bin;

ALTER TABLE `users`
    ADD COLUMN `last_mystery_key` INT(30) NOT NULL DEFAULT '0';

ALTER TABLE `permissions`
    ADD COLUMN `cmd_mystery_update`  enum('1','0') NOT NULL DEFAULT '0';