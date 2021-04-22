package org.dropco.smarthome.solar.move;

import org.dropco.smarthome.solar.dto.AbsolutePosition;

public interface PositionChangeListener {

    void onUpdate(AbsolutePosition position);
}
