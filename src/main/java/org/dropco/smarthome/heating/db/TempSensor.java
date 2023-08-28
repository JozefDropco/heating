package org.dropco.smarthome.heating.db;

public class TempSensor {
    private String id;
    private String name;
    private String placeRefCd;
    private double adjustmentTemp;
    private int orderId;

    public String getId() {
        return id;
    }

    public TempSensor setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public TempSensor setName(String name) {
        this.name = name;
        return this;
    }

    public double getAdjustmentTemp() {
        return adjustmentTemp;
    }

    public TempSensor setAdjustmentTemp(double adjustmentTemp) {
        this.adjustmentTemp = adjustmentTemp;
        return this;
    }

    public String getPlaceRefCd() {
        return placeRefCd;
    }

    public TempSensor setPlaceRefCd(String placeRefCd) {
        this.placeRefCd = placeRefCd;
        return this;
    }

    public int getOrderId() {
        return orderId;
    }

    public TempSensor setOrderId(int orderId) {
        this.orderId = orderId;
        return this;
    }
}
