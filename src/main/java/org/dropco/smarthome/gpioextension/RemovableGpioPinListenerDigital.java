package org.dropco.smarthome.gpioextension;

import com.pi4j.io.gpio.GpioPinDigital;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public abstract class RemovableGpioPinListenerDigital implements GpioPinListenerDigital {
    private GpioPinDigital pinDigital;

    public RemovableGpioPinListenerDigital(GpioPinDigital pinDigital) {
        this.pinDigital = pinDigital;
    }

    public void unlink(){
        pinDigital.removeListener(this);
    }
}
