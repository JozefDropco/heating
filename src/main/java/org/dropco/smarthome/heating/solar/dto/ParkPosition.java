package org.dropco.smarthome.heating.solar.dto;

public class ParkPosition implements Position {
    public static ParkPosition INSTANCE = new ParkPosition();
    private ParkPosition() {
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ParkPosition");
        return sb.toString();
    }

    @Override
    public <T> T invoke(PositionProcessor<T> processor) {
        return processor.process(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
         return true;
    }

}
