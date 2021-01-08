package org.dropco.smarthome.solar.move;

import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.solar.DayLight;
import org.dropco.smarthome.solar.SolarPanelPosition;
import org.dropco.smarthome.solar.StrongWind;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SafetySolarPanel {
    private static final Logger logger = Logger.getLogger(SafetySolarPanel.class.getName());
    private AtomicBoolean overHeated;
    private final Supplier<SolarPanelPosition> overHeatedPositionProvider;
    private final Supplier<SolarPanelPosition> strongWindProvider;
    private Integer lastHorizontal;
    private Integer lastVertical;

    public SafetySolarPanel(AtomicBoolean overHeated, Supplier<SolarPanelPosition> overHeatedPositionProvider,
                            Supplier<SolarPanelPosition> strongWindProvider) {
        this.overHeated = overHeated;
        this.overHeatedPositionProvider = overHeatedPositionProvider;
        this.strongWindProvider = strongWindProvider;
    }

    public void move(boolean ignoreDaylight, Integer horizontal, Integer vertical) {
        lastHorizontal = horizontal;
        lastVertical = vertical;
        if (ServiceMode.isServiceMode()) {
            logger.log(Level.INFO, "Servisný mód, posun zastavený.");
            return;
        }
        if (!overHeated.get() && !StrongWind.isWindy()) {
            if (ignoreDaylight || DayLight.inst().enoughLight())
                SolarPanelThreadManager.move(horizontal, vertical);
            else
                logger.log(Level.INFO, "Nová pozícia uložená, ale posun zastavený kvôli nedostatku jasu.");
        } else {
            logger.log(Level.INFO, "Nová pozícia uložená, ale posun zastavený kvôli " + (StrongWind.isWindy() ? "silnému vetru." : " prehriatiu kolektorov."));
        }
    }

    public void moveToStrongWindPosition() {
        SolarPanelPosition solarPanelPosition = strongWindProvider.get();
        logger.log(Level.INFO, "Presun na pozíciu pri silnom vetre, hor=" + solarPanelPosition.getHorizontalPositionInSeconds() + ", ver=" + solarPanelPosition.getVerticalPositionInSeconds());
        SolarPanelThreadManager.move(solarPanelPosition.getHorizontalPositionInSeconds(), solarPanelPosition.getVerticalPositionInSeconds());
    }

    public void moveToOverheatedPosition() {
        SolarPanelPosition solarPanelPosition = overHeatedPositionProvider.get();
        logger.log(Level.INFO, "Presun na pozíciu pri prehriatí, hor=" + solarPanelPosition.getHorizontalPositionInSeconds() + ", ver=" + solarPanelPosition.getVerticalPositionInSeconds());
        SolarPanelThreadManager.move(solarPanelPosition.getHorizontalPositionInSeconds(), solarPanelPosition.getVerticalPositionInSeconds());
    }

    public void backToNormal() {
        if (!overHeated.get() && !StrongWind.isWindy()) {
            logger.log(Level.INFO, "Presun späť do normálu, hor=" + lastHorizontal + ", ver=" + lastVertical);
            SolarPanelThreadManager.move(lastHorizontal, lastVertical);
        }
    }

}
