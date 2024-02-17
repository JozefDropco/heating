package org.dropco.smarthome.heating.heater;

import org.dropco.smarthome.TimerService;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.heating.ServiceMode;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class HolidayMode implements Runnable {

    private static final HolidayMode INSTANCE = new HolidayMode();
    public static final String HOLIDAY_FROM_DATE = "HOLIDAY_FROM_DATE";
    public static final String HOLIDAY_TO_DATE = "HOLIDAY_TO_DATE";
    static AtomicBoolean state = new AtomicBoolean(false);
    static AtomicReference<Date> from = new AtomicReference<>();
    static AtomicReference<Date> to = new AtomicReference<>();
    static final Semaphore update = new Semaphore(0);


    @Override
    public void run() {
        load();
        while (true) {
            if (!ServiceMode.isServiceMode()) {
                if (from.get() != null) {
                    Date currentDate = new Date();
                    if (from.get().before(currentDate)) {
                        if (to.get().after(currentDate) && state.compareAndSet(false, true)) {
                            BoilerBlocker.setHolidayMode(true);
                            TimerService.scheduleFor("HolidayMode", to.get(), () -> update.release());
                        } else {
                            state.set(false);
                            from.set(null);
                            to.set(null);
                            update(null, null);
                            BoilerBlocker.setHolidayMode(false);
                        }
                    } else
                        TimerService.scheduleFor("HolidayMode", from.get(), () -> update.release());
                } else {
                    state.set(false);
                    to.set(null);
                    update(null, null);
                    BoilerBlocker.setHolidayMode(false);
                }
            }
            update.acquireUninterruptibly();
        }
    }

    void load() {
        Db.acceptDao(new SettingsDao(), dao -> {
            Long holiday_from_date = dao.getLong(HOLIDAY_FROM_DATE);
            Long holiday_to_date = dao.getLong(HOLIDAY_TO_DATE);
            if (holiday_from_date != null) from.set(new Date(holiday_from_date));
            if (holiday_to_date != null) to.set(new Date(holiday_to_date));
        });
    }

    public void setMode(Date from, Date to) {
        update(from, to);
        HolidayMode.from.set(from);
        HolidayMode.to.set(to);
        update.release();
    }

    private void update(Date from, Date to) {
        Db.acceptDao(new SettingsDao(), dao -> {
            dao.setLong(HOLIDAY_FROM_DATE, Optional.ofNullable(from).map(Date::getTime).orElse(null));
            dao.setLong(HOLIDAY_TO_DATE, Optional.ofNullable(to).map(Date::getTime).orElse(null));
        });
    }

    /***
     * Gets the INSTANCE
     * @return
     */
    public static HolidayMode instance() {
        return INSTANCE;
    }

    /***
     * Gets the state
     * @return
     */
    public boolean getState() {
        return state.get();
    }

    public Date getFrom() {
        return from.get();
    }

    public Date getTo() {
        return to.get();
    }
}
