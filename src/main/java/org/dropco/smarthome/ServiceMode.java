package org.dropco.smarthome;

import org.dropco.smarthome.solar.move.SolarPanelThreadManager;
import org.dropco.smarthome.watering.WateringThreadManager;

import java.util.concurrent.atomic.AtomicBoolean;

public class ServiceMode {
    private static final AtomicBoolean serviceMode =new AtomicBoolean(false);

    public static void startServiceMode(){
        serviceMode.set(true);
        SolarPanelThreadManager.stop();
        WateringThreadManager.stop();
    }


    public static void stopServiceMode(){
        serviceMode.set(false);
    }

    public static boolean isServiceMode(){
        return serviceMode.get();
    }
}
