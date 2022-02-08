package org.dropco.smarthome.gpioextension;

import com.pi4j.io.gpio.GpioPinDigital;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public abstract class PulseInputGpioListener implements GpioPinListenerDigital {
    private final String pinName;
    private PinState logicalHighState;
    private long delayedShutdown;
    private AtomicLong counter = new AtomicLong();
    private Watch watch;

    public PulseInputGpioListener(PinState logicalHighState, long delayedShutdown, GpioPinDigital sourcePin) {
        this.logicalHighState = logicalHighState;
        this.delayedShutdown = delayedShutdown;
        this.pinName = sourcePin.getName();
        watch = new Watch(sourcePin.getState() == logicalHighState);
        watch.start();
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        if (event.getState() == logicalHighState) {
            counter.incrementAndGet();
            wakeUpWatchThread();
            handleStateChange(true);
        }
    }

    public void wakeUpWatchThread() {
        if (watch.sleeps.hasQueuedThreads()) {
            watch.sleeps.release();
        }
    }


    public class Watch extends Thread {
        private final Semaphore sleeps = new Semaphore(0);
        private boolean startState;
        private Logger logger = Logger.getLogger(Watch.class.getName());

        public Watch(boolean startState) {
            this.startState = startState;
        }

        @Override
        public void run() {
            if (!startState) sleeps.acquireUninterruptibly();
            while (true) {
                long expected = counter.get();
                try {
                    Thread.sleep(delayedShutdown);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (counter.get() == expected) {
                    handleStateChange(false);
                    sleeps.acquireUninterruptibly();
                }
            }
        }
    }

    public abstract void handleStateChange(boolean state);


}
