package org.dropco.smarthome.solar.move;

import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.solar.SolarPanelPosition;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SafetySolarPanel {
    private static final Logger logger = Logger.getLogger(SafetySolarPanel.class.getName());
    private AtomicBoolean overHeated;
    private AtomicBoolean strongWind;
    private final Supplier<SolarPanelPosition> overHeatedPositionProvider;
    private final Supplier<SolarPanelPosition> strongWindProvider;
    private Integer lastHorizontal;
    private Integer lastVertical;

    public SafetySolarPanel(AtomicBoolean overHeated, AtomicBoolean strongWind,
                            Supplier<SolarPanelPosition> overHeatedPositionProvider,
                            Supplier<SolarPanelPosition> strongWindProvider) {
        this.overHeated = overHeated;
        this.strongWind = strongWind;
        this.overHeatedPositionProvider = overHeatedPositionProvider;
        this.strongWindProvider = strongWindProvider;
    }

    public void move(Integer horizontal, Integer vertical) {
        lastHorizontal = horizontal;
        lastVertical = vertical;
        if (ServiceMode.isServiceMode()){
            logger.log(Level.INFO, "New position stored but not moved due service mode activated.");
            return;
        }
        if (!overHeated.get() && !strongWind.get()) {
            SolarPanelThreadManager.move(horizontal, vertical);
        } else {
            logger.log(Level.INFO, "New position stored but not moved due " + (strongWind.get() ? "strong wind." : "overheated."));
        }
    }

    public void moveToStrongWindPosition() {
        SolarPanelPosition solarPanelPosition = strongWindProvider.get();
        logger.log(Level.INFO, "Moving to strong wind position. " + solarPanelPosition);
        SolarPanelThreadManager.move(solarPanelPosition.getHorizontalPositionInSeconds(), solarPanelPosition.getVerticalPositionInSeconds());
    }

    public void moveToOverheatedPosition() {
        SolarPanelPosition solarPanelPosition = overHeatedPositionProvider.get();
        logger.log(Level.INFO, "Moving to overheated position. " + solarPanelPosition);
        SolarPanelThreadManager.move(solarPanelPosition.getHorizontalPositionInSeconds(), solarPanelPosition.getVerticalPositionInSeconds());
    }

    public void backToNormal() {
        if (!overHeated.get() && !strongWind.get()) {
            logger.log(Level.INFO, "Moving back to normal. Horizontal=" + lastHorizontal + ", vertical=" + lastVertical);
            SolarPanelThreadManager.move(lastHorizontal, lastVertical);
        }
    }

}
