# Xmas Mystery Box Plugin

This plugin adds the Xmas Mystery Box feature to Arcturus Morningstar 3.5.x.

> [!TIP]
> Portuguese version [here](README.pt.md)

- [How to install](#how-to-install)
- [FAQ](#faq)
  * [General](#general)
  * [Mystery Box item](#mystery-box-item)
  * [Mystery Box keys](#mystery-box-keys)
  * [Mystery Box rewards](#mystery-box-rewards)
    + [`mystery_rewards` Example](#mystery_rewards-example)
- [Support](#support)

## How to install

1. Download the latest release from the [release page](https://github.com/brenoepics/MysteryBox-Plugin/releases)
2. Run the sql file in your database
3. Add the interaction type `mysterybox` to the `items_base` table.
4. Paste the MysteryBoxPlugin.jar file into your emulator's plugins folder and start/restart the emulator.

## FAQ

### General

- `mysterybox.challenge.enabled` - Is the xmas challenge enabled? Default: true

### Mystery Box item

- Interaction type: `mysterybox`

If you do not have the `items_base` to Mystery Box furniture (maybe sprite_id 4797), here is an example.

```sql
INSERT INTO `items_base` (`sprite_id`, `public_name`, `item_name`, `type`, `width`, `length`, `stack_height`,
                          `allow_stack`, `allow_sit`, `allow_lay`, `allow_walk`, `allow_gift`, `allow_trade`,
                          `allow_recycle`, `allow_marketplace_sell`, `allow_inventory_stack`, `interaction_type`,
                          `interaction_modes_count`, `vending_ids`, `multiheight`, `customparams`, `effect_id_male`,
                          `effect_id_female`, `clothing_on_walk`)
VALUES (4797, 'Xmas Mystery Box', 'mystery_box', 's', 1, 1, 1.00, 1, 0, 0, 0, 1, 1, 0, 0, 1, 'mysterybox', 2, '0', '',
        '', 0, 0, '');
```

then, add to `catalog_items` using the generated id from the previous query or make a reward wired, achievement...
to distribute to users.

### Mystery Box keys

- `mysterybox.key.generate.time` - Time in seconds to generate a new key for the user. Default: 86400 (24h)
- `mysterybox.key.onlogin` - If true, the user will receive a new key on login.
  Default: true

The keys are auto generated on user login every 24h (`mysterybox.key.generate.time`)

### Mystery Box rewards

- You can update the rewards with the command `:mysterybox_update` (permission `cmd_mystery_update`)
- Available reward types:
  - WALL = i
  - FLOOR = s
  - EFFECT = e
  - HABBO_CLUB h ,

#### `mystery_rewards` Example

| id | type | reward |
|----|------|--------|
| 1  | s    | 100    |
| 2  | i    | 101    |
| 3  | e    | 102    |
| 4  | h    | 1      |


## Support

If you need help, open an issue [here](https://github.com/brenoepics/MysteryBox-Plugin/issues/new)

if you find this plugin useful, a ⭐ is appreciated.