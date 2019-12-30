package org.dropco.smarthome.dto;

/**
 * Created by Veronika on 29.12.2019.
 */
public class NamedPort {
    private String refCd;
    private String name;

    public NamedPort() {
    }

    public NamedPort(String refCd, String name) {
        this.refCd = refCd;
        this.name = name;
    }

    public String getRefCd() {
        return refCd;
    }

    public void setRefCd(String refCd) {
        this.refCd = refCd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
