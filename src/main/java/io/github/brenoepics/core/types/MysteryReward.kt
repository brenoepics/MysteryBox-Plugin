package io.github.brenoepics.core.types

import java.sql.ResultSet
import java.util.*

class MysteryReward(set: ResultSet) {
    val id: Int = set.getInt("id")
    val type: ProductTypeEnum? =
        ProductTypeEnum.Companion.fromChar(set.getString("type").uppercase(Locale.getDefault()))
    val reward: Int = set.getInt("reward")
}
