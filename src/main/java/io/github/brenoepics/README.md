



#### Mystery Box item

- Interaction type: `mysterybox`

If you do not have the `items_base` to Mystery Box furniture (maybe ID:4692), here is an example.
````sql
INSERT INTO `items_base` (`id`, `sprite_id`, `public_name`, `item_name`, `type`, `width`, `length`, `stack_height`, `allow_stack`, `allow_sit`, `allow_lay`, `allow_walk`, `allow_gift`, `allow_trade`, `allow_recycle`, `allow_marketplace_sell`, `allow_inventory_stack`, `interaction_type`, `interaction_modes_count`, `vending_ids`, `multiheight`, `customparams`, `effect_id_male`, `effect_id_female`, `clothing_on_walk`) VALUES (4692, 4692, 'Mystery Box', 'matic_box', 's', 1, 1, 1.00, 1, 0, 0, 0, 1, 1, 0, 0, 1, 'mysterybox', 2, '0', '', '', 0, 0, '');
```