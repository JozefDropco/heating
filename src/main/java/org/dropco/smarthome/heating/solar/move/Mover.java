package org.dropco.smarthome.heating.solar.move;

import org.dropco.smarthome.heating.dto.Position;

public interface Mover extends Runnable {

    void moveTo(Position position);
    void stop();
}
