package org.dropco.smarthome.heating.solar.dto;

public class DeltaPosition implements Position {

    private int deltaVerticalTicks;
    private int deltaHorizontalTicks;

    public DeltaPosition() {
    }


    public DeltaPosition(int deltaHorizontalTicks, int deltaVerticalTicks) {
        this.deltaHorizontalTicks = deltaHorizontalTicks;
        this.deltaVerticalTicks = deltaVerticalTicks;
    }

    public int getDeltaVerticalTicks() {
        return deltaVerticalTicks;
    }

    public void setDeltaVerticalTicks(int deltaVerticalTicks) {
        this.deltaVerticalTicks = deltaVerticalTicks;
    }

    public int getDeltaHorizontalTicks() {
        return deltaHorizontalTicks;
    }

    public void setDeltaHorizontalTicks(int deltaHorizontalTicks) {
        this.deltaHorizontalTicks = deltaHorizontalTicks;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeltaPosition{");
        sb.append("deltaVerticalTicks=").append(deltaVerticalTicks);
        sb.append(", deltaHorizontalTicks=").append(deltaHorizontalTicks);
        sb.append('}');
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

        DeltaPosition that = (DeltaPosition) o;

        if (deltaVerticalTicks != that.deltaVerticalTicks) return false;
        return deltaHorizontalTicks == that.deltaHorizontalTicks;
    }

    @Override
    public int hashCode() {
        int result = deltaVerticalTicks;
        result = 31 * result + deltaHorizontalTicks;
        return result;
    }
}
