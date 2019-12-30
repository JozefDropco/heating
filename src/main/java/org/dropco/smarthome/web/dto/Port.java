package org.dropco.smarthome.web.dto;

/**
 * Created by Veronika on 28.12.2019.
 */
public class Port {
    private String refcd;
    private String name;
    private String value;

    public Port() {
    }

    public Port(String key,String name, String value) {
        this.refcd = key;
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRefcd() {
        return refcd;
    }

    public void setRefcd(String refcd) {
        this.refcd = refcd;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
