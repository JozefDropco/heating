package org.dropco.smarthome.heating.solar.move;

import java.util.concurrent.atomic.AtomicInteger;

class PosDiff {
    private AtomicInteger hor =new AtomicInteger();
    private AtomicInteger vert = new AtomicInteger();

    public PosDiff setHor(int hor) {
        this.hor.set(hor);
        return this;
    }

    public PosDiff setVert(int vert) {
        this.vert.set(vert);
        return this;
    }

    /***
     * Gets the hor
     * @return
     */
    public int getHor() {
        return hor.get();
    }

    /***
     * Gets the vert
     * @return
     */
    public int getVert() {
        return vert.get();
    }

    public boolean decHor(int tick) {
        return hor.addAndGet(-tick) == 0;
    }

    public boolean decVert(int tick) {
        return vert.addAndGet(-tick) == 0;
    }

}
