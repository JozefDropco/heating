package org.dropco.smarthome.gpioextension;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public abstract class DelayedGpioPinListener implements GpioPinListenerDigital {
    private PinState triggerState;
    private long waitTimeInMillis;
    private AtomicReference<ScheduledFuture> inWaitMode = new AtomicReference<>();
    private GpioPinDigitalInput sourcePin;

    public DelayedGpioPinListener(PinState triggerState, long waitTimeInMillis, GpioPinDigitalInput sourcePin) {
        this.triggerState = triggerState;
        this.waitTimeInMillis = waitTimeInMillis;
        this.sourcePin = sourcePin;
        if (sourcePin.getState() == triggerState) {
            delayedCheck();
        }
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        if (event.getState() == triggerState) {
            delayedCheck();
        } else {
            ScheduledFuture future = inWaitMode.getAndSet(null);
            if (future!=null && !future.isDone())
                future.cancel(false);
            handleStateChange(false);
        }
    }

    public void delayedCheck() {
        inWaitMode.set(GpioFactory.getExecutorServiceFactory().getScheduledExecutorService().schedule(() -> {
            inWaitMode.set(null);
            if (sourcePin.getState() == triggerState) {
                handleStateChange(true);
            }
        }, waitTimeInMillis, TimeUnit.MILLISECONDS));
    }

    public abstract void handleStateChange(boolean state);
}
