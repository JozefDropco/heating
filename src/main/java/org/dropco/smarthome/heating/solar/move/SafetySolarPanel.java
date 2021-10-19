package org.dropco.smarthome.heating.solar.move;

import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.heating.solar.DayLight;
import org.dropco.smarthome.heating.solar.SolarTemperatureWatch;
import org.dropco.smarthome.heating.solar.StrongWind;
import org.dropco.smarthome.heating.solar.dto.*;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SafetySolarPanel {
    private static final Logger logger = Logger.getLogger(SafetySolarPanel.class.getName());
    private Consumer<AbsolutePosition> normalPositionUpdater;
    private final Supplier<DeltaPosition> strongWindProvider;
    private final Supplier<AbsolutePosition> lastKnownPositionProvider;
    private final Supplier<AbsolutePosition> overHeatedPositionProvider;

    private AbsolutePosition lastKnownPosition;

    public SafetySolarPanel(Consumer<AbsolutePosition> normalPositionUpdater, Supplier<DeltaPosition> strongWindProvider, Supplier<AbsolutePosition> lastKnownPosition, Supplier<AbsolutePosition> overHeatedPositionProvider) {
        this.normalPositionUpdater = normalPositionUpdater;
        this.strongWindProvider = strongWindProvider;
        this.lastKnownPositionProvider = lastKnownPosition;
        this.overHeatedPositionProvider = overHeatedPositionProvider;
    }


    public void move(boolean ignoreDaylight, Position position) {
        mergeAndSave(position);
        if (ServiceMode.isServiceMode()) {
            logger.log(Level.INFO, "Servisný mód, posun zastavený.");
            return;
        }
        if (!SolarTemperatureWatch.isOverHeated() && !StrongWind.isWindy()) {
            if (ignoreDaylight || DayLight.inst().enoughLight())
                SolarPanelManager.move(position);
            else
                logger.log(Level.INFO, "Nová pozícia uložená, ale posun zastavený kvôli nedostatku jasu.");
        } else {
            logger.log(Level.INFO, "Nová pozícia uložená, ale posun zastavený kvôli " + (StrongWind.isWindy() ? "silnému vetru." : " prehriatiu kolektorov."));
        }
    }

    public void moveToStrongWindPosition() {
        lastKnownPosition = lastKnownPositionProvider.get();
        DeltaPosition solarPanelPosition = strongWindProvider.get();
        mergeAndSave(solarPanelPosition);
        logger.log(Level.INFO, "Presun na pozíciu pri silnom vetre, hor=" + solarPanelPosition.getDeltaHorizontalTicks() + ", ver=" + solarPanelPosition.getDeltaVerticalTicks());
        SolarPanelManager.move(solarPanelPosition);
    }

    public void moveToOverheatedPosition() {
        lastKnownPosition = lastKnownPositionProvider.get();
        saveNormalPosition(lastKnownPosition);
        AbsolutePosition solarPanelPosition = overHeatedPositionProvider.get();
        logger.log(Level.INFO, "Presun na pozíciu pri prehriatí, hor=" + solarPanelPosition.getHorizontal() + ", ver=" + solarPanelPosition.getVertical());
        SolarPanelManager.move(solarPanelPosition);
    }


    public void backToNormal() {
        if (!SolarTemperatureWatch.isOverHeated() && !StrongWind.isWindy()) {
            SolarPanelManager.move(lastKnownPosition);
        }
    }

    private void mergeAndSave(Position position) {
        if (lastKnownPosition == null) lastKnownPosition = lastKnownPositionProvider.get();
        position.invoke(new PositionProcessor() {
            @Override
            public void process(AbsolutePosition absPos) {
                lastKnownPosition.setVertical(absPos.getVertical());
                lastKnownPosition.setHorizontal(absPos.getHorizontal());
            }

            @Override
            public void process(DeltaPosition deltaPos) {
                lastKnownPosition.setHorizontal(lastKnownPosition.getHorizontal() + deltaPos.getDeltaHorizontalTicks());
                lastKnownPosition.setVertical(lastKnownPosition.getVertical() + deltaPos.getDeltaVerticalTicks());
            }
        });
        saveNormalPosition(lastKnownPosition);
    }

    private void saveNormalPosition(AbsolutePosition position) {
        normalPositionUpdater.accept(position);
    }

}
