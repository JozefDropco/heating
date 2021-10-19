package org.dropco.smarthome.heating.solar.dto;

public interface PositionProcessor {
    void process(AbsolutePosition absPos);
    void process(DeltaPosition deltaPos);
}
