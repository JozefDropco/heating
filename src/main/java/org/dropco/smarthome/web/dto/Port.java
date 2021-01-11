package org.dropco.smarthome.web.dto;

/**
 * Created by Veronika on 28.12.2019.
 */
public class Port {
    private String refcd;
    private String url;
    private String name;
    private String value;

    public Port() {
    }

    public Port(String key, String url, String name, String value) {
        this.refcd = key;
        this.url = url;
        this.name = name;
        this.value = value;
    }

    /***
     * Gets the url
     * @return
     */
    public String getUrl() {
        return url;
    }

    public Port setUrl(String url) {
        this.url = url;
        return this;
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
