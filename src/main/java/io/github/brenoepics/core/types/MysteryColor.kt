package io.github.brenoepics.core.types

enum class MysteryColor(
    val id: Int,
    val code: String,
    val waitingState: Int,
    val tradingState: Int,
    val tradedState: Int
) {
    NONE(0, "", 0, 0, 0),
    RED(1, "red", 22, 23, 24),
    PURPLE(2, "purple", 1, 2, 3),
    GREEN(3, "green", 7, 8, 9),
    LILAC(4, "lilac", 13, 14, 15),
    YELLOW(5, "yellow", 10, 11, 12),
    TURQUOISE(6, "turquoise", 19, 20, 21),
    ORANGE(7, "orange", 16, 17, 18),
    BLUE(8, "blue", 4, 5, 6)
}
