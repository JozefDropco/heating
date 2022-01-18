package org.dropco.smarthome.heating.solar.move;

import static org.dropco.smarthome.heating.solar.SolarSystemRefCode.*;

public enum Movement {
    SOUTH(SOUTH_PIN_REF_CD, 1, "Juh"),
    NORTH(NORTH_PIN_REF_CD, -1, "Sever"),
    WEST(WEST_PIN_REF_CD, -1, "Západ"),
    EAST(EAST_PIN_REF_CD, 1, "Východ");

    private final String pinRefCd;
    private int tick;
    private final String name;
    private Movement shutdownFirst;

    static {
        SOUTH.shutdownFirst = NORTH;
        NORTH.shutdownFirst = SOUTH;
        WEST.shutdownFirst = EAST;
        EAST.shutdownFirst = WEST;
    }

    Movement(String pinRefCd, int tick, String name) {
        this.pinRefCd = pinRefCd;
        this.tick = tick;
        this.name = name;
    }

    /***
     * Gets the pinRefCd
     * @return
     */
    public String getPinRefCd() {
        return pinRefCd;
    }

    /***
     * Gets the name
     * @return
     */
    public String getName() {
        return name;
    }

    /***
     * Gets the tick
     * @return
     */
    public int getTick() {
        return tick;
    }

    /***
     * Gets the shutdownFirst
     * @return
     */
    public Movement getShutdownFirst() {
        return shutdownFirst;
    }
}
