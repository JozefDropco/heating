package org.dropco.smarthome.dto;

public class StringConstant extends Constant{
    private String value;

    /***
     * Gets the value
     * @return
     */
    public String getValue() {
        return value;
    }

    public StringConstant setValue(String value) {
        this.value = value;
        return this;
    }
}
