package org.dropco.smarthome.heating.solar.dto;

public interface PositionProcessor<T> {
    default T process(AbsolutePosition absPos) {
        return null;
    }

    default T process(DeltaPosition deltaPos) {
        return null;
    }

    default T process(ParkPosition parkPosition) {return null;}
}
