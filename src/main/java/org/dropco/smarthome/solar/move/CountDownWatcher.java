package org.dropco.smarthome.solar.move;

import org.dropco.smarthome.gpioextension.RemovableGpioPinListenerDigital;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public class CountDownWatcher {
    private AtomicInteger actualTicks = new AtomicInteger(0);
    private AtomicReference<RemovableGpioPinListenerDigital> listener = new AtomicReference<>();

    private final int tickCount;

    public CountDownWatcher(int tickCount) {
        this.tickCount = tickCount;
    }

    public void start(Consumer<Integer> callbackOnceFinished, Function<Consumer<Boolean>, RemovableGpioPinListenerDigital> realTimeTickerGetter, Consumer<Consumer<Boolean>> movement) {
        SolarPanelManager.addStopListener(unlinker -> {
            RemovableGpioPinListenerDigital listener = this.listener.get();
            if (listener!=null) listener.unlink();
            callbackOnceFinished.accept(actualTicks.get());
            unlinker.accept(null);
        });
        listener.set(realTimeTickerGetter.apply(state -> {
            if (state) {
                int currentTickCount = actualTicks.incrementAndGet();
                if (currentTickCount == tickCount) {
                    listener.get().unlink();
                    callbackOnceFinished.accept(currentTickCount);
                }
            }
        }));
        movement.accept(moving -> {
            if (!moving) {
                listener.get().unlink();
                callbackOnceFinished.accept(actualTicks.get());
            }
        });
    }
}
