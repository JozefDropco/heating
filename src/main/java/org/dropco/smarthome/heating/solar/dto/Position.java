package org.dropco.smarthome.heating.solar.dto;

public interface Position {
    <T> T invoke(PositionProcessor<T> processor);
}
