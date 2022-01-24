package org.dropco.smarthome.heating.solar.move;

import org.dropco.smarthome.PinManager;
import org.dropco.smarthome.heating.solar.ServiceMode;
import org.dropco.smarthome.heating.solar.dto.AbsolutePosition;
import org.dropco.smarthome.heating.solar.dto.DeltaPosition;
import org.dropco.smarthome.heating.solar.dto.Position;
import org.dropco.smarthome.heating.solar.dto.PositionProcessor;
import org.dropco.smarthome.heating.solar.move.horizontal.HorizontalMove;
import org.dropco.smarthome.heating.solar.move.vertical.VerticalMove;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Math.abs;

public class SolarPanelMover implements Mover {

    private static final Logger LOGGER = Logger.getLogger(SolarPanelMover.class.getName());
    private Supplier<AbsolutePosition> currentPositionSupplier;

    private AtomicReference<String> lastMovementRefCd = new AtomicReference<>();
    private final VerticalMove verticalMove;
    private final HorizontalMove horizontalMove;
    private ReentrantLock lock = new ReentrantLock(true);

    public SolarPanelMover(Supplier<AbsolutePosition> currentPositionSupplier, HorizontalMove horizontalMove, VerticalMove verticalMove) {
        this.currentPositionSupplier = currentPositionSupplier;
        this.horizontalMove = horizontalMove;
        this.verticalMove = verticalMove;
    }



    @Override
    public synchronized void moveTo(String movementRefCd, Position position) {
        if (ServiceMode.isServiceMode()) return;
        lock.lock();
        try {
            if (Objects.equals(lastMovementRefCd.get(), movementRefCd)) return;
            lastMovementRefCd.set(movementRefCd);
            PosDiff diff = calculateDifference(position, currentPositionSupplier.get());
            if (diff.getHor() == 0 && diff.getVert() == 0) return;
            int absHorizontal = abs(diff.getHor());
            LOGGER.info("Posun o [hor=" + diff.getHor() + ", vert=" + diff.getVert() + "]");
            if (absHorizontal > 0) {
                Movement horMovement = getHorMovement(diff);
                horizontalMove.moveTo(horMovement, diff.getHor());
            }
            int absVertical = abs(diff.getVert());
            if (absVertical > 0) {
                Movement vertMovement = getVertMovement(diff);
                verticalMove.moveTo(vertMovement, diff.getVert());
            }
        } finally {
            lock.unlock();
        }

    }

    public void stop() {
        lock.lock();
        try {
            horizontalMove.stopMovement();
            verticalMove.stopMovement();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void moveTo(Movement movement, boolean state) {
            if (movement==Movement.EAST || movement==Movement.WEST){
                if (state) {
                    horizontalMove.moveTo(movement);
                } else {
                    horizontalMove.stopMovement();
                }
            }
        if (movement==Movement.NORTH || movement==Movement.SOUTH){
            if (state) {
                verticalMove.moveTo(movement);
            } else {
                verticalMove.stopMovement();
            }
        }
    }

    @Override
    public void moveTo(String movementRefCd, Movement horizontal, Movement vertical) {
        lastMovementRefCd.set(movementRefCd);
        moveTo(horizontal,true);
        moveTo(vertical,true);
    }

    public void waitForEnd() {
        horizontalMove.waitForEnd();
        verticalMove.waitForEnd();
    }


    private PosDiff calculateDifference(Position position, final AbsolutePosition previousPosition) {
        PosDiff diff = position.invoke(new PositionProcessor<PosDiff>() {
            @Override
            public PosDiff process(AbsolutePosition absPos) {
                LOGGER.log(Level.FINE, "Natáčanie kolektorov na hor=" + absPos.getHorizontal() + ", vert=" + absPos.getVertical());
                return new PosDiff()
                        .setHor(absPos.getHorizontal() - previousPosition.getHorizontal())
                        .setVert(absPos.getVertical() - previousPosition.getVertical());
            }

            @Override
            public PosDiff process(DeltaPosition deltaPos) {
                LOGGER.log(Level.FINE, "Natáčanie kolektorov o hor=" + deltaPos.getDeltaHorizontalTicks() + ", vert=" + deltaPos.getDeltaVerticalTicks());
                return new PosDiff()
                        .setHor(deltaPos.getDeltaHorizontalTicks())
                        .setVert(deltaPos.getDeltaVerticalTicks());
            }
        });
        return diff;
    }

    private Movement getVertMovement(PosDiff diff) {
        if (diff.getVert() < 0) return Movement.NORTH;
        return Movement.SOUTH;
    }

    private Movement getHorMovement(PosDiff diff) {
        if (diff.getHor() < 0) return Movement.WEST;
        return Movement.EAST;
    }

}
