package org.dropco.smarthome.solar.dto;

import java.util.List;

public class SolarSchedule {
    private int month;
    private int horizontalTickCountForStep;
    private int verticalTickCountForStep;
    private SolarPanelStepRecord sunRise;
    private SolarPanelStepRecord sunSet;
    private List<SolarPanelStepRecord> remainingSteps;
    private AbsolutePosition currentNormalPosition;

    /***
     * Gets the month
     * @return
     */
    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    /***
     * Gets the horizontalTickCountForStep
     * @return
     */
    public int getHorizontalTickCountForStep() {
        return horizontalTickCountForStep;
    }

    public void setHorizontalTickCountForStep(int horizontalTickCountForStep) {
        this.horizontalTickCountForStep = horizontalTickCountForStep;
    }

    /***
     * Gets the verticalTickCountForStep
     * @return
     */
    public int getVerticalTickCountForStep() {
        return verticalTickCountForStep;
    }

    public void setVerticalTickCountForStep(int verticalTickCountForStep) {
        this.verticalTickCountForStep = verticalTickCountForStep;
    }

    /***
     * Gets the sunRise
     * @return
     */
    public SolarPanelStepRecord getSunRise() {
        return sunRise;
    }

    public void setSunRise(SolarPanelStepRecord sunRise) {
        this.sunRise = sunRise;
    }

    /***
     * Gets the sunSet
     * @return
     */
    public SolarPanelStepRecord getSunSet() {
        return sunSet;
    }

    public void setSunSet(SolarPanelStepRecord sunSet) {
        this.sunSet = sunSet;
    }

    /***
     * Gets the steps
     * @return
     */
    public List<SolarPanelStepRecord> getRemainingSteps() {
        return remainingSteps;
    }

    public void setRemainingSteps(List<SolarPanelStepRecord> remainingSteps) {
        this.remainingSteps = remainingSteps;
    }

    /***
     * Gets the currentNormalPosition
     * @return
     */
    public AbsolutePosition getCurrentNormalPosition() {
        return currentNormalPosition;
    }

    public void setCurrentNormalPosition(AbsolutePosition currentNormalPosition) {
        this.currentNormalPosition = currentNormalPosition;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SolarSchedule{");
        sb.append("month=").append(month);
        sb.append(", horizontalTickCountForStep=").append(horizontalTickCountForStep);
        sb.append(", verticalTickCountForStep=").append(verticalTickCountForStep);
        sb.append(", sunRise=").append(sunRise);
        sb.append(", sunSet=").append(sunSet);
        sb.append(", remainingSteps=").append(remainingSteps);
        sb.append('}');
        return sb.toString();
    }
}
