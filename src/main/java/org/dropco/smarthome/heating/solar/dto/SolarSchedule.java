package org.dropco.smarthome.heating.solar.dto;

import java.util.Date;
import java.util.List;

public class SolarSchedule {
    private Date asOfDate = new Date();
    private int month;
    private int horizontalTickCountForStep;
    private int verticalTickCountForStep;
    private List<SolarPanelStep> steps;

    /***
     * Gets the asOfDate
     * @return
     */
    public Date getAsOfDate() {
        return asOfDate;
    }

    public SolarSchedule setAsOfDate(Date asOfDate) {
        this.asOfDate = asOfDate;
        return this;
    }

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
     * Gets the steps
     * @return
     */
    public List<SolarPanelStep> getSteps() {
        return steps;
    }

    public void setSteps(List<SolarPanelStep> steps) {
        this.steps = steps;
    }



    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SolarSchedule{");
        sb.append("month=").append(month);
        sb.append(", horizontalTickCountForStep=").append(horizontalTickCountForStep);
        sb.append(", verticalTickCountForStep=").append(verticalTickCountForStep);
        sb.append(", remainingSteps=").append(steps);
        sb.append('}');
        return sb.toString();
    }
}
