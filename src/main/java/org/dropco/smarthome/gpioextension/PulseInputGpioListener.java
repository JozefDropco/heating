package org.dropco.smarthome.gpioextension;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigital;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class PulseInputGpioListener implements GpioPinListenerDigital {
    private PinState logicalHighState;
    private long delayedShutdown;
    private AtomicReference<ScheduledFuture> inWaitMode = new AtomicReference<>();
    private AtomicLong counter = new AtomicLong();
    private final String pinName;

    public PulseInputGpioListener(PinState logicalHighState, long delayedShutdown, GpioPinDigital sourcePin) {
        this.logicalHighState = logicalHighState;
        this.delayedShutdown = delayedShutdown;
        pinName = sourcePin.getName();
        if (sourcePin.getState() == logicalHighState) {
            delayedShutdown(counter.incrementAndGet());
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
        }
        delayedShutdown(counter.incrementAndGet());
    }

    public void delayedShutdown(long expected) {
        inWaitMode.set(GpioFactory.getExecutorServiceFactory().getScheduledExecutorService().schedule(() -> {
            inWaitMode.set(null);
            long current = counter.get();
            if (current == expected)
                handleStateChange(false);
            else
                Logger.getLogger(pinName).log(Level.FINE, "Counter has changed in meanwhile ignoring it. Expected:" + expected + ", current:" + current);
        }, delayedShutdown, TimeUnit.MILLISECONDS));
    }

    public abstract void handleStateChange(boolean state);
}
