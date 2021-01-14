package org.dropco.smarthome.dto;

public class DoubleConstant extends Constant {
    private Double value;

    /***
     * Gets the value
     * @return
     */
    public Double getValue() {
        return value;
    }

    public DoubleConstant setValue(Double value) {
        this.value = value;
        return this;
    }
}
