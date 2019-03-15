package org.dropco.smarthome.gpioextension;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.util.concurrent.TimeUnit;

public abstract class DelayedGpioPinListener implements GpioPinListenerDigital {
    private PinState triggerState;
    private long waitTimeInMilis;
    private GpioPinDigitalOutput sourcePin;

    public DelayedGpioPinListener(PinState triggerState, long waitTimeInMilis, GpioPinDigitalOutput sourcePin) {
        this.triggerState = triggerState;
        this.waitTimeInMilis = waitTimeInMilis;
        this.sourcePin = sourcePin;
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        if (event.getState()==triggerState) {
            GpioFactory.getExecutorServiceFactory().getScheduledExecutorService().schedule(() -> {
                if (sourcePin.getState()==triggerState){
                    handleStateChange(true);
                }
            },waitTimeInMilis, TimeUnit.MILLISECONDS);
        } else{
            handleStateChange(false);
        }
    }

    public abstract void handleStateChange(boolean state);
}
