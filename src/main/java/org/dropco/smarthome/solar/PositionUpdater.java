package org.dropco.smarthome.solar;

import org.dropco.smarthome.database.SolarSystemDao;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class PositionUpdater implements Runnable {
    private BlockingDeque<SolarPanelPosition> queue = new LinkedBlockingDeque<>();
    SolarSystemDao dao;
    private Object lock = new Object();

    public PositionUpdater(SolarSystemDao dao) {
        this.dao = dao;
    }

    @Override
    public void run() {
        SolarPanelPosition position;
        try {
            synchronized (lock) {
                position = queue.takeLast();
                queue.clear();
                dao.updateLastKnownPosition(position);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void add(SolarPanelPosition position) {
        synchronized (lock) {
            queue.add(position);
        }
    }
}
