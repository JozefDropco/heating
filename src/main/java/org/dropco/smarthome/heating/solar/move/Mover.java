package org.dropco.smarthome.heating.solar.move;

import org.dropco.smarthome.heating.solar.dto.Position;

public interface Mover {

    void moveTo(String movementRefCd, Position position);
    void stop();
}
