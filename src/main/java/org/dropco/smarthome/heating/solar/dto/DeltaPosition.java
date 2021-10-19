package org.dropco.smarthome.heating.solar.dto;

public class DeltaPosition implements Position {

    private Integer deltaVerticalTicks;
    private Integer deltaHorizontalTicks;

    public DeltaPosition() {
    }


    public DeltaPosition(Integer deltaHorizontalTicks, Integer deltaVerticalTicks) {
        this.deltaHorizontalTicks = deltaHorizontalTicks;
        this.deltaVerticalTicks = deltaVerticalTicks;
    }

    public Integer getDeltaVerticalTicks() {
        return deltaVerticalTicks;
    }

    public void setDeltaVerticalTicks(Integer deltaVerticalTicks) {
        this.deltaVerticalTicks = deltaVerticalTicks;
    }

    public Integer getDeltaHorizontalTicks() {
        return deltaHorizontalTicks;
    }

    public void setDeltaHorizontalTicks(Integer deltaHorizontalTicks) {
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
    public void invoke(PositionProcessor processor) {
        processor.process(this);
    }
}
