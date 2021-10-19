package org.dropco.smarthome.heating.solar;

import org.dropco.smarthome.heating.solar.dto.DeltaPosition;
import org.dropco.smarthome.heating.solar.dto.Position;
import org.dropco.smarthome.heating.solar.move.SafetySolarPanel;

import java.util.logging.Logger;

public class SolarSystemScheduledWork implements Runnable {

    private static final Logger logger = Logger.getLogger(SolarSystemScheduledWork.class.getName());
    private SafetySolarPanel solarPanel;
    private boolean ignoreDaylight;
    private Position position;


    public SolarSystemScheduledWork(SafetySolarPanel solarPanel, boolean ignoreDaylight, Position position) {
        this.solarPanel = solarPanel;
        this.ignoreDaylight = ignoreDaylight;
        this.position = position;
    }

    public void run() {
        solarPanel.move(ignoreDaylight,position);
    }
}
