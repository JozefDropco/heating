package org.dropco.smarthome.heating.solar.dto;

public class AbsolutePosition implements Position {
    private int horizontal;
    private int vertical;

    public AbsolutePosition() {
    }

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
    public <T> T invoke(PositionProcessor<T> processor) {
        return processor.process(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbsolutePosition that = (AbsolutePosition) o;

        if (horizontal != that.horizontal) return false;
        return vertical == that.vertical;
    }

    @Override
    public int hashCode() {
        int result = horizontal;
        result = 31 * result + vertical;
        return result;
    }
}
