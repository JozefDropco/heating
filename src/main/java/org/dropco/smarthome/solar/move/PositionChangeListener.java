package org.dropco.smarthome.solar.move;

import org.dropco.smarthome.solar.SolarPanelPosition;

public interface PositionChangeListener {

    void onUpdate(SolarPanelPosition position);
}
