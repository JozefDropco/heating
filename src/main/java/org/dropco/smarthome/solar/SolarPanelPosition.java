package org.dropco.smarthome.solar;

public class SolarPanelPosition {

    private Integer verticalPositionInSeconds;
    private Integer horizontalPositionInSeconds;

    public SolarPanelPosition() {
    }

    public SolarPanelPosition(Integer verticalPositionInSeconds, Integer horizontalPositionInSeconds) {
        this.verticalPositionInSeconds = verticalPositionInSeconds;
        this.horizontalPositionInSeconds = horizontalPositionInSeconds;
    }

    public Integer getVerticalPositionInSeconds() {
        return verticalPositionInSeconds;
    }

    public void setVerticalPositionInSeconds(Integer verticalPositionInSeconds) {
        this.verticalPositionInSeconds = verticalPositionInSeconds;
    }

    public Integer getHorizontalPositionInSeconds() {
        return horizontalPositionInSeconds;
    }

    public void setHorizontalPositionInSeconds(Integer horizontalPositionInSeconds) {
        this.horizontalPositionInSeconds = horizontalPositionInSeconds;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SolarPanelPosition{");
        sb.append("verticalPositionInSeconds=").append(verticalPositionInSeconds);
        sb.append(", horizontalPositionInSeconds=").append(horizontalPositionInSeconds);
        sb.append('}');
        return sb.toString();
    }
}