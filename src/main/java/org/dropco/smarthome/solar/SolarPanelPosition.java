package org.dropco.smarthome.solar;

public class SolarPanelPosition {

    private Integer verticalPositionInSeconds;
    private Integer horizontalPositionInSeconds;

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
}