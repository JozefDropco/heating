package org.dropco.smarthome.gpioextension;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigital;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public abstract class PulseInputGpioListener implements GpioPinListenerDigital {
    private PinState logicalHighState;
    private long delayedShutdown;
    private AtomicReference<ScheduledFuture> inWaitMode = new AtomicReference<>();
    private GpioPinDigital sourcePin;

    public PulseInputGpioListener(PinState logicalHighState, long delayedShutdown, GpioPinDigital sourcePin) {
        this.logicalHighState = logicalHighState;
        this.delayedShutdown = delayedShutdown;
        this.sourcePin = sourcePin;
        if (sourcePin.getState() == logicalHighState) {
            delayedShutdown();
        }
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        if (event.getState() == logicalHighState) {
            ScheduledFuture future = inWaitMode.getAndSet(null);
            if (future != null && !future.isDone()) {
                future.cancel(false);
            }
            handleStateChange(true);
            delayedShutdown();
        }
    }

    public void delayedShutdown() {
        inWaitMode.set(GpioFactory.getExecutorServiceFactory().getScheduledExecutorService().schedule(() -> {
            inWaitMode.set(null);
            handleStateChange(false);
        }, delayedShutdown, TimeUnit.MILLISECONDS));
    }

    public abstract void handleStateChange(boolean state);
}
