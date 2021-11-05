package org.dropco.smarthome.heating.solar.move;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public enum SolarPanelState {
    NORMAL,
    NORMAL_AFTER_WIND,
    STRONG_WIND,
    PARK_POSITION,

    PANEL_OVERHEATED,
    WATER_OVERHEATED,
    PANEL_WATER_OVERHEATED,
    PANEL_OVERHEATED_STRONG_WIND,
    WATER_OVERHEATED_STRONG_WIND,
    PANEL_WATER_OVERHEATED_STRONG_WIND,

    PANEL_OVERHEATED_AFTER_WIND,
    WATER_OVERHEATED_AFTER_WIND,
    PANEL_WATER_OVERHEATED_AFTER_WIND,
    PANEL_OVERHEATED_STRONG_WIND_AFTER_WIND,
    WATER_OVERHEATED_STRONG_WIND_AFTER_WIND,
    PANEL_WATER_OVERHEATED_STRONG_WIND_AFTER_WIND;

    public enum SolarPanelEvent {
        NO_WIND,
        STRONG_WIND,
        WATER_OVERHEATED,
        WATER_COOLED_DOWN,
        PANEL_OVERHEATED,
        PANEL_COOLED_DOWN,
        DAY_LIGHT_REACHED;
    }

    public static boolean isStrongWind(SolarPanelState state) {
        return state == PANEL_OVERHEATED_STRONG_WIND || state == WATER_OVERHEATED_STRONG_WIND || state == PANEL_WATER_OVERHEATED_STRONG_WIND || state == STRONG_WIND;
    }

    private Map<SolarPanelEvent, SolarPanelState> nextStates;

    public SolarPanelState getNextState(SolarPanelEvent event) {
        return nextStates.get(event);
    }

    static {
        NORMAL.nextStates = ImmutableMap.<SolarPanelEvent, SolarPanelState>builder()
                .put(SolarPanelEvent.DAY_LIGHT_REACHED, NORMAL)
                .put(SolarPanelEvent.PANEL_OVERHEATED, PANEL_OVERHEATED)
                .put(SolarPanelEvent.PANEL_COOLED_DOWN, NORMAL)
                .put(SolarPanelEvent.STRONG_WIND, STRONG_WIND)
                .put(SolarPanelEvent.NO_WIND, NORMAL)
                .put(SolarPanelEvent.WATER_OVERHEATED, WATER_OVERHEATED)
                .put(SolarPanelEvent.WATER_COOLED_DOWN, NORMAL).build();
        NORMAL_AFTER_WIND.nextStates = ImmutableMap.<SolarPanelEvent, SolarPanelState>builder()
                .put(SolarPanelEvent.DAY_LIGHT_REACHED, NORMAL_AFTER_WIND)
                .put(SolarPanelEvent.PANEL_OVERHEATED, PANEL_OVERHEATED_AFTER_WIND)
                .put(SolarPanelEvent.PANEL_COOLED_DOWN, NORMAL_AFTER_WIND)
                .put(SolarPanelEvent.STRONG_WIND, STRONG_WIND)
                .put(SolarPanelEvent.NO_WIND, NORMAL_AFTER_WIND)
                .put(SolarPanelEvent.WATER_OVERHEATED, WATER_OVERHEATED_AFTER_WIND)
                .put(SolarPanelEvent.WATER_COOLED_DOWN, NORMAL_AFTER_WIND).build();
        STRONG_WIND.nextStates = ImmutableMap.<SolarPanelEvent, SolarPanelState>builder()
                .put(SolarPanelEvent.DAY_LIGHT_REACHED, STRONG_WIND)
                .put(SolarPanelEvent.PANEL_OVERHEATED, PANEL_OVERHEATED_STRONG_WIND)
                .put(SolarPanelEvent.PANEL_COOLED_DOWN, STRONG_WIND)
                .put(SolarPanelEvent.STRONG_WIND, STRONG_WIND)
                .put(SolarPanelEvent.NO_WIND, NORMAL_AFTER_WIND)
                .put(SolarPanelEvent.WATER_OVERHEATED, WATER_OVERHEATED_STRONG_WIND)
                .put(SolarPanelEvent.WATER_COOLED_DOWN, STRONG_WIND).build();
        PARK_POSITION.nextStates = ImmutableMap.<SolarPanelEvent, SolarPanelState>builder()
                .put(SolarPanelEvent.DAY_LIGHT_REACHED, NORMAL)
                .put(SolarPanelEvent.PANEL_OVERHEATED, PARK_POSITION)
                .put(SolarPanelEvent.PANEL_COOLED_DOWN, PARK_POSITION)
                .put(SolarPanelEvent.STRONG_WIND, PARK_POSITION)
                .put(SolarPanelEvent.NO_WIND, PARK_POSITION)
                .put(SolarPanelEvent.WATER_OVERHEATED, PARK_POSITION)
                .put(SolarPanelEvent.WATER_COOLED_DOWN, PARK_POSITION).build();

        PANEL_OVERHEATED.nextStates = ImmutableMap.<SolarPanelEvent, SolarPanelState>builder()
                .put(SolarPanelEvent.DAY_LIGHT_REACHED, PANEL_OVERHEATED)
                .put(SolarPanelEvent.PANEL_OVERHEATED, PANEL_OVERHEATED)
                .put(SolarPanelEvent.PANEL_COOLED_DOWN, NORMAL)
                .put(SolarPanelEvent.STRONG_WIND, PANEL_OVERHEATED_STRONG_WIND)
                .put(SolarPanelEvent.NO_WIND, PANEL_OVERHEATED)
                .put(SolarPanelEvent.WATER_OVERHEATED, PANEL_WATER_OVERHEATED)
                .put(SolarPanelEvent.WATER_COOLED_DOWN, PANEL_OVERHEATED).build();
        WATER_OVERHEATED.nextStates = ImmutableMap.<SolarPanelEvent, SolarPanelState>builder()
                .put(SolarPanelEvent.DAY_LIGHT_REACHED, WATER_OVERHEATED)
                .put(SolarPanelEvent.PANEL_OVERHEATED, PANEL_WATER_OVERHEATED)
                .put(SolarPanelEvent.PANEL_COOLED_DOWN, WATER_OVERHEATED)
                .put(SolarPanelEvent.STRONG_WIND, WATER_OVERHEATED_STRONG_WIND)
                .put(SolarPanelEvent.NO_WIND, WATER_OVERHEATED)
                .put(SolarPanelEvent.WATER_OVERHEATED, WATER_OVERHEATED)
                .put(SolarPanelEvent.WATER_COOLED_DOWN, NORMAL).build();
        PANEL_WATER_OVERHEATED.nextStates = ImmutableMap.<SolarPanelEvent, SolarPanelState>builder()
                .put(SolarPanelEvent.DAY_LIGHT_REACHED, PANEL_WATER_OVERHEATED)
                .put(SolarPanelEvent.PANEL_OVERHEATED, PANEL_WATER_OVERHEATED)
                .put(SolarPanelEvent.PANEL_COOLED_DOWN, WATER_OVERHEATED)
                .put(SolarPanelEvent.STRONG_WIND, PANEL_WATER_OVERHEATED_STRONG_WIND)
                .put(SolarPanelEvent.NO_WIND, PANEL_WATER_OVERHEATED)
                .put(SolarPanelEvent.WATER_OVERHEATED, PANEL_WATER_OVERHEATED)
                .put(SolarPanelEvent.WATER_COOLED_DOWN, PANEL_OVERHEATED).build();

        PANEL_OVERHEATED_STRONG_WIND.nextStates = ImmutableMap.<SolarPanelEvent, SolarPanelState>builder()
                .put(SolarPanelEvent.DAY_LIGHT_REACHED, PANEL_OVERHEATED_STRONG_WIND)
                .put(SolarPanelEvent.PANEL_OVERHEATED, PANEL_OVERHEATED_STRONG_WIND)
                .put(SolarPanelEvent.PANEL_COOLED_DOWN, STRONG_WIND)
                .put(SolarPanelEvent.STRONG_WIND, PANEL_OVERHEATED_STRONG_WIND)
                .put(SolarPanelEvent.NO_WIND, PANEL_OVERHEATED)
                .put(SolarPanelEvent.WATER_OVERHEATED, PANEL_WATER_OVERHEATED_STRONG_WIND)
                .put(SolarPanelEvent.WATER_COOLED_DOWN, PANEL_OVERHEATED_STRONG_WIND).build();
        WATER_OVERHEATED_STRONG_WIND.nextStates = ImmutableMap.<SolarPanelEvent, SolarPanelState>builder()
                .put(SolarPanelEvent.DAY_LIGHT_REACHED, WATER_OVERHEATED_STRONG_WIND)
                .put(SolarPanelEvent.PANEL_OVERHEATED, PANEL_WATER_OVERHEATED_STRONG_WIND)
                .put(SolarPanelEvent.PANEL_COOLED_DOWN, WATER_OVERHEATED_STRONG_WIND)
                .put(SolarPanelEvent.STRONG_WIND, WATER_OVERHEATED_STRONG_WIND)
                .put(SolarPanelEvent.NO_WIND, WATER_OVERHEATED)
                .put(SolarPanelEvent.WATER_OVERHEATED, WATER_OVERHEATED_STRONG_WIND)
                .put(SolarPanelEvent.WATER_COOLED_DOWN, STRONG_WIND).build();
        PANEL_WATER_OVERHEATED_STRONG_WIND.nextStates = ImmutableMap.<SolarPanelEvent, SolarPanelState>builder()
                .put(SolarPanelEvent.DAY_LIGHT_REACHED, PANEL_WATER_OVERHEATED_STRONG_WIND)
                .put(SolarPanelEvent.PANEL_OVERHEATED, PANEL_WATER_OVERHEATED_STRONG_WIND)
                .put(SolarPanelEvent.PANEL_COOLED_DOWN, WATER_OVERHEATED_STRONG_WIND)
                .put(SolarPanelEvent.STRONG_WIND, PANEL_WATER_OVERHEATED_STRONG_WIND)
                .put(SolarPanelEvent.NO_WIND, PANEL_WATER_OVERHEATED)
                .put(SolarPanelEvent.WATER_OVERHEATED, PANEL_WATER_OVERHEATED_STRONG_WIND)
                .put(SolarPanelEvent.WATER_COOLED_DOWN, PANEL_OVERHEATED_STRONG_WIND).build();

        PANEL_OVERHEATED_AFTER_WIND.nextStates = ImmutableMap.<SolarPanelEvent, SolarPanelState>builder()
                .put(SolarPanelEvent.DAY_LIGHT_REACHED, PANEL_OVERHEATED_AFTER_WIND)
                .put(SolarPanelEvent.PANEL_OVERHEATED, PANEL_OVERHEATED_AFTER_WIND)
                .put(SolarPanelEvent.PANEL_COOLED_DOWN, NORMAL_AFTER_WIND)
                .put(SolarPanelEvent.STRONG_WIND, PANEL_OVERHEATED_STRONG_WIND_AFTER_WIND)
                .put(SolarPanelEvent.NO_WIND, PANEL_OVERHEATED_AFTER_WIND)
                .put(SolarPanelEvent.WATER_OVERHEATED, PANEL_WATER_OVERHEATED_AFTER_WIND)
                .put(SolarPanelEvent.WATER_COOLED_DOWN, PANEL_OVERHEATED_AFTER_WIND).build();
        WATER_OVERHEATED_AFTER_WIND.nextStates = ImmutableMap.<SolarPanelEvent, SolarPanelState>builder()
                .put(SolarPanelEvent.DAY_LIGHT_REACHED, WATER_OVERHEATED_AFTER_WIND)
                .put(SolarPanelEvent.PANEL_OVERHEATED, PANEL_WATER_OVERHEATED_AFTER_WIND)
                .put(SolarPanelEvent.PANEL_COOLED_DOWN, WATER_OVERHEATED_AFTER_WIND)
                .put(SolarPanelEvent.STRONG_WIND, WATER_OVERHEATED_STRONG_WIND_AFTER_WIND)
                .put(SolarPanelEvent.NO_WIND, WATER_OVERHEATED_AFTER_WIND)
                .put(SolarPanelEvent.WATER_OVERHEATED, WATER_OVERHEATED_AFTER_WIND)
                .put(SolarPanelEvent.WATER_COOLED_DOWN, NORMAL).build();
        PANEL_WATER_OVERHEATED_AFTER_WIND.nextStates = ImmutableMap.<SolarPanelEvent, SolarPanelState>builder()
                .put(SolarPanelEvent.DAY_LIGHT_REACHED, PANEL_WATER_OVERHEATED_AFTER_WIND)
                .put(SolarPanelEvent.PANEL_OVERHEATED, PANEL_WATER_OVERHEATED_AFTER_WIND)
                .put(SolarPanelEvent.PANEL_COOLED_DOWN, WATER_OVERHEATED_AFTER_WIND)
                .put(SolarPanelEvent.STRONG_WIND, PANEL_WATER_OVERHEATED_STRONG_WIND_AFTER_WIND)
                .put(SolarPanelEvent.NO_WIND, PANEL_WATER_OVERHEATED_AFTER_WIND)
                .put(SolarPanelEvent.WATER_OVERHEATED, PANEL_WATER_OVERHEATED_AFTER_WIND)
                .put(SolarPanelEvent.WATER_COOLED_DOWN, PANEL_OVERHEATED_AFTER_WIND).build();

        PANEL_OVERHEATED_STRONG_WIND_AFTER_WIND.nextStates = ImmutableMap.<SolarPanelEvent, SolarPanelState>builder()
                .put(SolarPanelEvent.DAY_LIGHT_REACHED, PANEL_OVERHEATED_STRONG_WIND_AFTER_WIND)
                .put(SolarPanelEvent.PANEL_OVERHEATED, PANEL_OVERHEATED_STRONG_WIND_AFTER_WIND)
                .put(SolarPanelEvent.PANEL_COOLED_DOWN, STRONG_WIND)
                .put(SolarPanelEvent.STRONG_WIND, PANEL_OVERHEATED_STRONG_WIND_AFTER_WIND)
                .put(SolarPanelEvent.NO_WIND, PANEL_OVERHEATED_AFTER_WIND)
                .put(SolarPanelEvent.WATER_OVERHEATED, PANEL_WATER_OVERHEATED_STRONG_WIND_AFTER_WIND)
                .put(SolarPanelEvent.WATER_COOLED_DOWN, PANEL_OVERHEATED_STRONG_WIND_AFTER_WIND).build();
        WATER_OVERHEATED_STRONG_WIND_AFTER_WIND.nextStates = ImmutableMap.<SolarPanelEvent, SolarPanelState>builder()
                .put(SolarPanelEvent.DAY_LIGHT_REACHED, WATER_OVERHEATED_STRONG_WIND_AFTER_WIND)
                .put(SolarPanelEvent.PANEL_OVERHEATED, PANEL_WATER_OVERHEATED_STRONG_WIND_AFTER_WIND)
                .put(SolarPanelEvent.PANEL_COOLED_DOWN, WATER_OVERHEATED_STRONG_WIND_AFTER_WIND)
                .put(SolarPanelEvent.STRONG_WIND, WATER_OVERHEATED_STRONG_WIND_AFTER_WIND)
                .put(SolarPanelEvent.NO_WIND, WATER_OVERHEATED_AFTER_WIND)
                .put(SolarPanelEvent.WATER_OVERHEATED, WATER_OVERHEATED_STRONG_WIND_AFTER_WIND)
                .put(SolarPanelEvent.WATER_COOLED_DOWN, STRONG_WIND).build();
        PANEL_WATER_OVERHEATED_STRONG_WIND_AFTER_WIND.nextStates = ImmutableMap.<SolarPanelEvent, SolarPanelState>builder()
                .put(SolarPanelEvent.DAY_LIGHT_REACHED, PANEL_WATER_OVERHEATED_STRONG_WIND_AFTER_WIND)
                .put(SolarPanelEvent.PANEL_OVERHEATED, PANEL_WATER_OVERHEATED_STRONG_WIND_AFTER_WIND)
                .put(SolarPanelEvent.PANEL_COOLED_DOWN, WATER_OVERHEATED_STRONG_WIND_AFTER_WIND)
                .put(SolarPanelEvent.STRONG_WIND, PANEL_WATER_OVERHEATED_STRONG_WIND_AFTER_WIND)
                .put(SolarPanelEvent.NO_WIND, PANEL_WATER_OVERHEATED_AFTER_WIND)
                .put(SolarPanelEvent.WATER_OVERHEATED, PANEL_WATER_OVERHEATED_STRONG_WIND_AFTER_WIND)
                .put(SolarPanelEvent.WATER_COOLED_DOWN, PANEL_OVERHEATED_STRONG_WIND_AFTER_WIND).build();
    }
}
