package org.dropco.smarthome.heating.db;

public class MeasurePlace {
    private String deviceId;
    private String name;
    private String refCd;
    private double adjustmentTemp;
    private int orderId;

    public String getDeviceId() {
        return deviceId;
    }

    public MeasurePlace setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public String getRefCd() {
        return refCd;
    }

    public MeasurePlace setRefCd(String refCd) {
        this.refCd = refCd;
        return this;
    }

    public String getName() {
        return name;
    }

    public MeasurePlace setName(String name) {
        this.name = name;
        return this;
    }

    public double getAdjustmentTemp() {
        return adjustmentTemp;
    }

    public MeasurePlace setAdjustmentTemp(double adjustmentTemp) {
        this.adjustmentTemp = adjustmentTemp;
        return this;
    }

    public int getOrderId() {
        return orderId;
    }

    public MeasurePlace setOrderId(int orderId) {
        this.orderId = orderId;
        return this;
    }
}
