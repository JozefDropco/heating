package org.dropco.smarthome.solar.move;

import com.pi4j.io.gpio.GpioFactory;
import org.dropco.smarthome.gpioextension.RemovableGpioPinListenerDigital;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class CountDownWatcher {
    private static final Logger LOGGER = Logger.getLogger(CountDownWatcher.class.getName());
    private AtomicInteger actualTicks = new AtomicInteger(0);
    private AtomicReference<RemovableGpioPinListenerDigital> listener = new AtomicReference<>();

    private final int tickCount;

    public CountDownWatcher(int tickCount) {
        this.tickCount = tickCount;
    }

    public void start(Consumer<Integer> callbackOnceFinished, Function<Consumer<Boolean>, RemovableGpioPinListenerDigital> realTimeTickerGetter, Consumer<BiConsumer<Supplier<Boolean>, Boolean>> movement, Supplier<Boolean> isMoving) {
        Consumer<Consumer<Void>> stopListener = unlinker -> {
            RemovableGpioPinListenerDigital listener = this.listener.get();
            if (listener != null) {
                LOGGER.fine(CountDownWatcher.this + " Unlinking");
                listener.unlink();
            }
            callbackOnceFinished.accept(actualTicks.get());
            LOGGER.fine(CountDownWatcher.this + " Unlinking");
            unlinker.accept(null);
        };
        SolarPanelManager.addStopListener(stopListener);
        listener.set(realTimeTickerGetter.apply(state -> {
            if (state) {
                int currentTickCount = actualTicks.incrementAndGet();
                if (currentTickCount == tickCount) {
                    LOGGER.fine(CountDownWatcher.this + " Unlinking 1");
                    listener.get().unlink();
                    callbackOnceFinished.accept(currentTickCount);
                }
            }
        }));
        movement.accept((unlink, moving) -> {
            if (!moving) {
                listener.get().unlink();
                unlink.get();
                LOGGER.fine(CountDownWatcher.this + " Unlinking 2");
                callbackOnceFinished.accept(actualTicks.get());
            }
        });
        GpioFactory.getExecutorServiceFactory().getScheduledExecutorService().schedule(() -> {
            if (!isMoving.get()) {
                RemovableGpioPinListenerDigital listener = this.listener.get();
                if (listener != null) {
                    listener.unlink();

                    LOGGER.fine(CountDownWatcher.this + " Unlinking 3");
                }
                SolarPanelManager.removeStopListener(stopListener);
                callbackOnceFinished.accept(actualTicks.get());
            }
        }, 5000, TimeUnit.MILLISECONDS);
    }
}
