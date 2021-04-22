package org.dropco.smarthome.solar.dto;

public interface PositionProcessor {
    void process(AbsolutePosition absPos);
    void process(DeltaPosition deltaPos);
}
