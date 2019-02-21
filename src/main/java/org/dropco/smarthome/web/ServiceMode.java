package org.dropco.smarthome.web;

public class ServiceMode {
    private boolean state;

    public ServiceMode() {
    }

    public ServiceMode(boolean state) {
        this.state = state;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
