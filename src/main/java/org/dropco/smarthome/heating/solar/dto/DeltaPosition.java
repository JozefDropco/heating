package org.dropco.smarthome.heating.solar.dto;

public class DeltaPosition implements Position {

    private int verticalCount;
    private int horizontalCount;

    public DeltaPosition() {
    }


    public DeltaPosition(int deltaHorizontalTicks, int verticalCount) {
        this.horizontalCount = deltaHorizontalTicks;
        this.verticalCount = verticalCount;
    }

    public int getVerticalCount() {
        return verticalCount;
    }

    public void setVerticalCount(int verticalCount) {
        this.verticalCount = verticalCount;
    }

    public int getHorizontalCount() {
        return horizontalCount;
    }

    public void setHorizontalCount(int horizontalCount) {
        this.horizontalCount = horizontalCount;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeltaPosition{");
        sb.append("verticalCount=").append(verticalCount);
        sb.append(", horizontalCount=").append(horizontalCount);
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

        if (verticalCount != that.verticalCount) return false;
        return horizontalCount == that.horizontalCount;
    }

    @Override
    public int hashCode() {
        int result = verticalCount;
        result = 31 * result + horizontalCount;
        return result;
    }
}
