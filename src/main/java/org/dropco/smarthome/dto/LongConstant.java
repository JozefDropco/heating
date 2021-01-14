package org.dropco.smarthome.dto;

public class LongConstant extends Constant{
    private Long value;

    /***
     * Gets the value
     * @return
     */
    public Long getValue() {
        return value;
    }

    public LongConstant setValue(Long value) {
        this.value = value;
        return this;
    }
}
