package org.dropco.smarthome.solar.move;

import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.solar.DayLight;
import org.dropco.smarthome.solar.SolarPanelPosition;
import org.dropco.smarthome.solar.SolarTemperatureWatch;
import org.dropco.smarthome.solar.StrongWind;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SafetySolarPanel {
    private static final Logger logger = Logger.getLogger(SafetySolarPanel.class.getName());
    private final Supplier<SolarPanelPosition> strongWindProvider;
    private Integer lastHorizontal;
    private Integer lastVertical;

    public SafetySolarPanel(Supplier<SolarPanelPosition> strongWindProvider) {
        this.strongWindProvider = strongWindProvider;
    }

    public void move(boolean ignoreDaylight, Integer horizontal, Integer vertical) {
        lastHorizontal = horizontal;
        lastVertical = vertical;
        if (ServiceMode.isServiceMode()) {
            logger.log(Level.INFO, "Servisný mód, posun zastavený.");
            return;
        }
        if (!SolarTemperatureWatch.isOverHeated() && !StrongWind.isWindy()) {
            if (ignoreDaylight || DayLight.inst().enoughLight())
                SolarPanelManager.move(horizontal, vertical);
            else
                logger.log(Level.INFO, "Nová pozícia uložená, ale posun zastavený kvôli nedostatku jasu.");
        } else {
            logger.log(Level.INFO, "Nová pozícia uložená, ale posun zastavený kvôli " + (StrongWind.isWindy() ? "silnému vetru." : " prehriatiu kolektorov."));
        }
    }

    public void moveToStrongWindPosition() {
        SolarPanelPosition solarPanelPosition = strongWindProvider.get();
        logger.log(Level.INFO, "Presun na pozíciu pri silnom vetre, hor=" + solarPanelPosition.getHorizontalPositionInSeconds() + ", ver=" + solarPanelPosition.getVerticalPositionInSeconds());
        SolarPanelManager.move(solarPanelPosition.getHorizontalPositionInSeconds(), solarPanelPosition.getVerticalPositionInSeconds());
    }



    public void backToNormal() {
        if (!SolarTemperatureWatch.isOverHeated() && !StrongWind.isWindy()) {
            logger.log(Level.INFO, "Návrat do normálu, hor=" + lastHorizontal + ", ver=" + lastVertical);
            SolarPanelManager.move(lastHorizontal, lastVertical);
        }
    }

}
