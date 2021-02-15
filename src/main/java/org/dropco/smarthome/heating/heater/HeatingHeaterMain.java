package org.dropco.smarthome.heating.heater;

import org.dropco.smarthome.Main;
import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.stats.StatsCollector;

import java.util.function.Consumer;


public class HeatingHeaterMain {

private static final String HEATER_BLINK_STOP = "HEATER_BLINK_STOP";
    public static void start(SettingsDao dao) {
        new Flame(Main.getInput(Flame.HEATER_FLAME_REF_CD)).start();
        long blinkStop = dao.getLong(HEATER_BLINK_STOP);
        new HeaterCircularPump(Main.getInput(HeaterCircularPump.HEATER_CIRCULAR_REF_CD)).start(blinkStop);
        new Boiler(Main.getInput(Boiler.HEATER_BOILER_FEC_CD)).start(blinkStop);
        configureServiceMode();
        addToStats();
    }

    private static void addToStats() {
        StatsCollector.getInstance().collect("Horák plynového kotla", Flame.getState(), new Consumer<Consumer<Boolean>>() {
            @Override
            public void accept(Consumer<Boolean> countStats) {
                Flame.addSubscriber(countStats);
            }
        });
        StatsCollector.getInstance().collect("Kúrenie chod čerpadla", HeaterCircularPump.getState(), new Consumer<Consumer<Boolean>>() {
            @Override
            public void accept(Consumer<Boolean> countStats) {
                HeaterCircularPump.addSubscriber(countStats);
            }
        });
        StatsCollector.getInstance().collect("Ohrev TA3 plynovým kotlom", Boiler.getState(), new Consumer<Consumer<Boolean>>() {
            @Override
            public void accept(Consumer<Boolean> countStats) {
                Boiler.addSubscriber(countStats);
            }
        });
    }

    private static void configureServiceMode() {
        ServiceMode.addInput(new NamedPort(Flame.HEATER_FLAME_REF_CD, "Horák plynového kotla"), () -> Flame.getState());
        ServiceMode.addInput(new NamedPort(HeaterCircularPump.HEATER_CIRCULAR_REF_CD, "Kúrenie chod čerpadla"), () -> HeaterCircularPump.getState());
        ServiceMode.addInput(new NamedPort(Boiler.HEATER_BOILER_FEC_CD, "Ohrev TA3 plynovým kotlom"), () -> Boiler.getState());
    }

}
