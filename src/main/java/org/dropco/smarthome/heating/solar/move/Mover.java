package org.dropco.smarthome.heating.solar.move;

import org.dropco.smarthome.heating.solar.dto.Position;

public interface Mover {

    void moveTo(String movementRefCd, Position position);
    void stop();
    void moveTo(Movement movement, boolean state);

    void moveTo(String movementRefCd, Movement horizontal, Movement vertical);
}
