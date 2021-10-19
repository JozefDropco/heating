package org.dropco.smarthome.heating.dto;

public interface PositionProcessor {
    void process(AbsolutePosition absPos);
    void process(DeltaPosition deltaPos);
}
