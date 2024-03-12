package io.github.brenoepics.core.types

import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

class MysteryItem {
    val id: Int
    var itemId: Int? = null
        private set
    val ownerId: Int
    val color: MysteryColor?
    var state: MysteryState

    constructor(set: ResultSet) {
        this.id = set.getInt("id")
        this.ownerId = set.getInt("user_id")
        try {
            this.itemId = set.getInt("item_id")
        } catch (ignored: SQLException) {
            this.itemId = null
        }
        this.color = MysteryColor.valueOf(set.getString("color").uppercase(Locale.getDefault()))
        this.state = MysteryState.valueOf(set.getString("state").uppercase(Locale.getDefault()))
    }

    constructor(id: Int, color: MysteryColor?, state: MysteryState, ownerId: Int, itemId: Int?) {
        this.id = id
        this.color = color
        this.state = state
        this.ownerId = ownerId
        this.itemId = itemId
    }
}
