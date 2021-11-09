package org.dropco.smarthome.heating.solar.move;

import org.dropco.smarthome.heating.solar.dto.AbsolutePosition;

public interface PositionChangeListener {

    void onUpdate(AbsolutePosition position);
}
