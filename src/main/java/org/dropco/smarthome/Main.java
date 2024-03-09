package org.dropco.smarthome;

import com.google.common.collect.Sets;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.heating.HeatingMain;
import org.dropco.smarthome.stats.StatsCollector;
import org.dropco.smarthome.temp.PeriodicCleanup;
import org.dropco.smarthome.temp.TempService;
import org.dropco.smarthome.watering.WateringMain;
import org.dropco.smarthome.web.WebServer;

import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Main {
    public static final PinManager pinManager = new PinManagerImpl();
    public static final Set<String> INPUTS = Sets.newHashSet();

    public static void main(String[] args) throws Exception {
        Db.acceptDao(new SettingsDao(), settingsDao -> {
            // Create JAX-RS application.
            new Thread(new TempService()).start();
            StatsCollector.getInstance().start(settingsDao);
            INPUTS.addAll(Arrays.asList(args));
            if (!INPUTS.contains("--noWatering")) {
                WateringMain.main(settingsDao);
            }
            if (INPUTS.contains("--heating")) {
                HeatingMain.start(settingsDao);
            }
        });
        new PeriodicCleanup().start();
   //     new org.dropco.smarthome.stats.PeriodicCleanup().start();
        WebServer webServer = new WebServer();
        webServer.start();
        Logger logger = Logger.getLogger("");
        logger.setLevel(Level.ALL);
        LogHandler handler = new LogHandler();
        logger.addHandler(handler);
        handler.setLevel(Level.ALL);
        webServer.join();
        new LogCleanup().start();
    }

}
