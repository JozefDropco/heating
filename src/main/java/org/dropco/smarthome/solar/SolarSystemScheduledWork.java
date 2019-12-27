package org.dropco.smarthome.solar;

import org.dropco.smarthome.solar.move.SafetySolarPanel;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SolarSystemScheduledWork implements Runnable {

    private static final Logger logger = Logger.getLogger(SolarSystemScheduledWork.class.getName());
    private SafetySolarPanel solarPanel;
    private SolarPanelPosition position;


    public SolarSystemScheduledWork(SafetySolarPanel solarPanel, SolarPanelPosition position) {
        this.solarPanel = solarPanel;
        this.position = position;
    }

    public void run() {
        logger.log(Level.INFO,"Solárny panel presun na novú pozíciu. Nová pozícia: hor=" + position.getHorizontalPositionInSeconds()+", ver="+position.getVerticalPositionInSeconds());
        solarPanel.move(position.getHorizontalPositionInSeconds(), position.getVerticalPositionInSeconds());
    }
}
