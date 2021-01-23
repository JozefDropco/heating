package org.dropco.smarthome.solar;

import org.dropco.smarthome.heating.db.HeatingDao;
import org.dropco.smarthome.solar.move.SafetySolarPanel;
import org.dropco.smarthome.solar.move.SolarPanelThreadManager;
import org.dropco.smarthome.temp.TempService;

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

    private final Supplier<SolarPanelPosition> overHeatedPositionProvider;
    private Supplier<Double> threshold;

    public SolarTemperatureWatch(Supplier<SolarPanelPosition> overHeatedPositionProvider, HeatingDao dao, Supplier<Double> threshold) {
        this.overHeatedPositionProvider = overHeatedPositionProvider;
        this.deviceId = dao.getDeviceId(MEASURE_PLACE_REF_CD);
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
                        if (successFullySet) moveToOverheatedPosition();
                    } else {
                        boolean successFullySet = solarOverHeated.compareAndSet(true, false);
                        if (successFullySet) safetySolarPanel.backToNormal();
                    }
                }
            });
        else
            logger.log(Level.SEVERE, "Meracie miesto prehriatia kolektorov nie je nastavené (REF_CD=" + MEASURE_PLACE_REF_CD + ")");
    }

    private void moveToOverheatedPosition() {
        SolarPanelPosition solarPanelPosition = overHeatedPositionProvider.get();
        logger.log(Level.INFO, "Presun na pozíciu pri prehriatí, hor=" + solarPanelPosition.getHorizontalPositionInSeconds() + ", ver=" + solarPanelPosition.getVerticalPositionInSeconds());
        SolarPanelThreadManager.move(solarPanelPosition.getHorizontalPositionInSeconds(), solarPanelPosition.getVerticalPositionInSeconds());
    }
}
