package org.dropco.smarthome.heating;

import com.pi4j.io.gpio.GpioFactory;
import org.dropco.smarthome.ServiceMode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

public class Boiler implements Runnable {

    private static final ScheduledExecutorService EXECUTOR_SERVICE = GpioFactory.getExecutorServiceFactory().getScheduledExecutorService();
    protected static final String BOILER_PORT_KEY = "BOILER_PORT_KEY";
    private static final Lock update = new ReentrantLock();
    private BiConsumer<String, Boolean> commandExecutor;
    static AtomicBoolean state = new AtomicBoolean(false);

    public Boiler(BiConsumer<String, Boolean> commandExecutor) {
        this.commandExecutor = commandExecutor;
        CircularPump.addSubscriber((state)->update.unlock());
        ThreeWayValve.addSubscriber((state)->update.unlock());
    }

    @Override
    public void run() {
        SimpleDateFormat parser = new SimpleDateFormat("HH:mm");
        EXECUTOR_SERVICE.schedule(()->update.unlock(), millisRemaining(getNextTime()), TimeUnit.MILLISECONDS);
        try {
            while (true) {
                Calendar instance = Calendar.getInstance();
                Date morning = parser.parse("06:00");
                Date unblockTime = parser.parse("15:30");
                Date night = parser.parse("21:00");
                Date nightWeekend = parser.parse("23:00");
                if (!ServiceMode.isServiceMode()) {
                    int day = instance.get(Calendar.DAY_OF_WEEK);
                    Date time = instance.getTime();
                    if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
                        if (time.after(morning) && time.before(nightWeekend)) {
                            if (CircularPump.getState() && ThreeWayValve.getState() && state.compareAndSet(false, true)) {
                                commandExecutor.accept(BOILER_PORT_KEY, true);
                            } else {
                                if (state.compareAndSet(true, false)) {
                                    commandExecutor.accept(BOILER_PORT_KEY, false);
                                }
                            }
                        } else if (state.compareAndSet(false, true)) {
                            commandExecutor.accept(BOILER_PORT_KEY, true);
                        }
                    } else {
                        if (time.after(unblockTime) && time.before(night) && state.compareAndSet(true, false)) {
                            commandExecutor.accept(BOILER_PORT_KEY, false);
                        } else {
                            if (state.compareAndSet(false, true)){
                                commandExecutor.accept(BOILER_PORT_KEY, true);
                            }
                        }
                    }
                }
                EXECUTOR_SERVICE.schedule(()->update.unlock(), millisRemaining(getNextTime()), TimeUnit.MILLISECONDS);
                update.lock();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private Date getNextTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 1);
        calendar.set(Calendar.MILLISECOND, 0);
        int minute = calendar.get(Calendar.MINUTE);
        calendar.add(Calendar.MINUTE,30-(minute%30));
        return calendar.getTime();
    }

    static long millisRemaining(Date future) {
        Date currentDate = Calendar.getInstance().getTime();
        return future.getTime() - currentDate.getTime();
    }
}
