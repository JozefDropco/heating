package org.dropco.smarthome.solar;

import com.google.common.collect.Lists;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.heating.db.HeatingDao;
import org.dropco.smarthome.solar.dto.AbsolutePosition;
import org.dropco.smarthome.solar.dto.DeltaPosition;
import org.dropco.smarthome.solar.move.SafetySolarPanel;
import org.dropco.smarthome.solar.move.SolarPanelManager;
import org.dropco.smarthome.temp.TempService;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SolarTemperatureWatch {
    private static Logger logger = Logger.getLogger(SolarTemperatureWatch.class.getName());
    private static final String MEASURE_PLACE_REF_CD = "SOLAR";
    private final String deviceId;
    private static final AtomicBoolean solarOverHeated = new AtomicBoolean(false);
    private static List<Consumer<Boolean>> subscribers = Collections.synchronizedList(Lists.newArrayList());

    private Supplier<Double> threshold;

    public SolarTemperatureWatch(Supplier<Double> threshold) {
        this.deviceId = Db.applyDao(new HeatingDao(), dao->dao.getDeviceId(MEASURE_PLACE_REF_CD));
        this.threshold = threshold;
    }

    public static boolean isOverHeated() {
        return solarOverHeated.get();
    }

    public void attach(SafetySolarPanel safetySolarPanel) {
        if (deviceId != null)
            TempService.subscribe(deviceId, new Consumer<Double>() {
                @Override
                public void accept(Double temperature) {
                    if (temperature > threshold.get()) {
                        boolean successFullySet = solarOverHeated.compareAndSet(false, true);
                        if (successFullySet) safetySolarPanel.moveToOverheatedPosition();
                    } else {
                        boolean successFullySet = solarOverHeated.compareAndSet(true, false);
                        if (successFullySet) safetySolarPanel.backToNormal();
                    }
                }
            });
        else
            logger.log(Level.SEVERE, "Meracie miesto prehriatia kolektorov nie je nastavené (REF_CD=" + MEASURE_PLACE_REF_CD + ")");
    }


    public static void addSubscriber(Consumer<Boolean> subscriber) {
        subscribers.add(subscriber);
    }

}
