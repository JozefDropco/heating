package org.dropco.smarthome.heating;

import com.pi4j.io.gpio.GpioFactory;
import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.database.SettingsDao;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class Boiler implements Runnable {

    private static final ScheduledExecutorService EXECUTOR_SERVICE = GpioFactory.getExecutorServiceFactory().getScheduledExecutorService();
    static final String BOILER_PORT_KEY = "BOILER_PORT_KEY";
    static final Semaphore update = new Semaphore(0);
    private BiConsumer<String, Boolean> commandExecutor;
    static AtomicBoolean state = new AtomicBoolean(false);

    public Boiler(BiConsumer<String, Boolean> commandExecutor) {
        this.commandExecutor = commandExecutor;
        CircularPump.addSubscriber((state) -> update.release());
        ThreeWayValve.addSubscriber((state) -> update.release());
    }

    @Override
    public void run() {
        EXECUTOR_SERVICE.schedule(() -> update.release(), millisRemaining(getNextTime()), TimeUnit.MILLISECONDS);
        while (true) {
            Calendar instance = getCurrentDate();
            Date morning = getTime(6, 0);
            Date unblockTime = getTime(15, 30);
            Date night = getTime(21, 0);
            Date nightWeekend = getTime(23, 0);
            if (!ServiceMode.isServiceMode()) {
                int day = instance.get(Calendar.DAY_OF_WEEK);
                Date time = instance.getTime();
                if (isWeekend(day)) {
                    if (time.after(morning) && time.before(nightWeekend)) {
                        if (CircularPump.getState() && ThreeWayValve.getState() && state.compareAndSet(false, true)) {
                            commandExecutor.accept(getDeviceId(), true);
                        } else {
                            if (state.compareAndSet(true, false)) {
                                commandExecutor.accept(getDeviceId(), false);
                            }
                        }
                    } else if (state.compareAndSet(false, true)) {
                        commandExecutor.accept(getDeviceId(), true);
                    }
                } else {
                    if (time.after(unblockTime) && time.before(night) && state.compareAndSet(true, false)) {
                        commandExecutor.accept(getDeviceId(), false);
                    } else {
                        if (state.compareAndSet(false, true)) {
                            commandExecutor.accept(getDeviceId(), true);
                        }
                    }
                }
            }
            EXECUTOR_SERVICE.schedule(() -> update.release(), millisRemaining(getNextTime()), TimeUnit.MILLISECONDS);
            update.acquireUninterruptibly();
        }
    }

    String getDeviceId() {
        return new SettingsDao().getString(BOILER_PORT_KEY);
    }

    Date getTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        return calendar.getTime();
    }

    boolean isWeekend(int day) {
        return day == Calendar.SATURDAY || day == Calendar.SUNDAY;
    }

    Calendar getCurrentDate() {
        return Calendar.getInstance();
    }

    public Date getNextTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 1);
        calendar.set(Calendar.MILLISECOND, 0);
        int minute = calendar.get(Calendar.MINUTE);
        calendar.add(Calendar.MINUTE, 30 - (minute % 30));
        return calendar.getTime();
    }

    static long millisRemaining(Date future) {
        Date currentDate = Calendar.getInstance().getTime();
        return future.getTime() - currentDate.getTime();
    }
}