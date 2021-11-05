package org.dropco.smarthome.heating.dto;

public interface PositionProcessor<T> {
    T process(AbsolutePosition absPos);
    T process(DeltaPosition deltaPos);
}
