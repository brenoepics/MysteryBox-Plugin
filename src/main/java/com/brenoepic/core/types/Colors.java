package com.brenoepic.core.types;

public enum Colors {

    NONE(0, "", 0, 0, 0),
    RED(1, "red", 22, 23, 24),
    PURPLE(2, "purple", 1, 2, 3),
    GREEN(3 ,"green", 7, 8, 9),
    LILAC(4, "lilac", 13, 14, 15),
    YELLOW(5, "yellow", 10, 11, 12),
    TURQUOISE(6, "turquoise", 19, 20, 21),
    ORANGE(7, "orange", 16, 17, 18),
    BLUE(8, "blue", 4, 5, 6);

    private final int id;
    private final String name;
    private final int state1;
    private final int state2;
    private final int state3;

    Colors(int id, String name, int waitingState, int tradingState, int tradedState) {
        this.id = id;
        this.name = name;
        this.state1 = waitingState;
        this.state2 = tradingState;
        this.state3 = tradedState;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public int getWaitingState() {
        return this.state1;
    }

    public int getTradingState() {
        return this.state2;
    }

    public int getTradedState() {
        return this.state3;
    }
}
