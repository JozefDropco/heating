package org.dropco.smarthome.heating;

import org.dropco.smarthome.Main;
import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.heating.heater.HeatingHeaterMain;
import org.dropco.smarthome.heating.solar.SolarHeatingMain;
import org.dropco.smarthome.stats.StatsCollector;

import java.util.function.Consumer;

public class HeatingMain {

    public static void start(SettingsDao settingsDao) {
        SolarHeatingMain.start((key, value) -> {
            Main.getOutput(key).setState(value);
        });
        HeatingHeaterMain.start(settingsDao);
        addFireplace();
    }

    private static void addFireplace() {
        new FireplaceCircularPump(Main.getInput(FireplaceCircularPump.FIREPLACE_CIRCULAR_PUMP_REF_CD)).start();
        ServiceMode.addInput(new NamedPort(FireplaceCircularPump.FIREPLACE_CIRCULAR_PUMP_REF_CD, "Krb chod čerpadla"), () -> FireplaceCircularPump.getState());
        StatsCollector.getInstance().collect("Krb chod čerpadla", FireplaceCircularPump.getState(), new Consumer<Consumer<Boolean>>() {
            @Override
            public void accept(Consumer<Boolean> countStats) {
                FireplaceCircularPump.addSubscriber(countStats);
            }
        });
    }
}
