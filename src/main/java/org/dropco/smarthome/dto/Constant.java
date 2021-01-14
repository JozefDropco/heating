package org.dropco.smarthome.dto;

import java.util.Date;

public abstract class Constant {
    private String refCd;
    private String description;
    private String group;
    private String constantType;
    private String valueType;
    private Date lastModification;

    /***
     * Gets the refCd
     * @return
     */
    public String getRefCd() {
        return refCd;
    }

    public Constant setRefCd(String refCd) {
        this.refCd = refCd;
        return this;
    }

    /***
     * Gets the description
     * @return
     */
    public String getDescription() {
        return description;
    }

    public Constant setDescription(String description) {
        this.description = description;
        return this;
    }

    /***
     * Gets the group
     * @return
     */
    public String getGroup() {
        return group;
    }

    public Constant setGroup(String group) {
        this.group = group;
        return this;
    }

    /***
     * Gets the contantType
     * @return
     */
    public String getConstantType() {
        return constantType;
    }

    public Constant setConstantType(String constantType) {
        this.constantType = constantType;
        return this;
    }

    /***
     * Gets the valueType
     * @return
     */
    public String getValueType() {
        return valueType;
    }

    public Constant setValueType(String valueType) {
        this.valueType = valueType;
        return this;
    }

    /***
     * Gets the lastModification
     * @return
     */
    public Date getLastModification() {
        return lastModification;
    }

    public Constant setLastModification(Date lastModification) {
        this.lastModification = lastModification;
        return this;
    }
}
