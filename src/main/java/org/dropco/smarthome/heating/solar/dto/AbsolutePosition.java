package org.dropco.smarthome.heating.solar.dto;

public class AbsolutePosition implements Position {
    private int horizontal;
    private int vertical;

    public AbsolutePosition(int horizontal, int vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    /***
     * Gets the horizontal
     * @return
     */
    public int getHorizontal() {
        return horizontal;
    }

    public void setHorizontal(int horizontal) {
        this.horizontal = horizontal;
    }

    /***
     * Gets the vertical
     * @return
     */
    public int getVertical() {
        return vertical;
    }

    public void setVertical(int vertical) {
        this.vertical = vertical;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AbsolutePosition{");
        sb.append("horizontal=").append(horizontal);
        sb.append(", vertical=").append(vertical);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public void invoke(PositionProcessor processor) {
        processor.process(this);
    }
}
