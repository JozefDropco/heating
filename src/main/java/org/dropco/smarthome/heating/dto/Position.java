package org.dropco.smarthome.heating.dto;

public interface Position {
    <T> T invoke(PositionProcessor<T> processor);
}
