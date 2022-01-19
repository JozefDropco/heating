package org.dropco.smarthome.web;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.pi4j.io.gpio.PinState;
import org.dropco.smarthome.Main;
import org.dropco.smarthome.TimeUtil;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.heating.db.SolarSystemDao;
import org.dropco.smarthome.heating.solar.DayLight;
import org.dropco.smarthome.heating.solar.ServiceMode;
import org.dropco.smarthome.heating.solar.SolarMain;
import org.dropco.smarthome.heating.solar.StrongWind;
import org.dropco.smarthome.heating.solar.dto.*;
import org.dropco.smarthome.heating.solar.move.Movement;
import org.dropco.smarthome.heating.solar.move.SolarPanelStateManager;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Path("/ws/solar")
public class SolarWebService extends ServiceModeWebService {
    private static final Logger logger = Logger.getLogger(SolarWebService.class.getName());

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTimetable(@QueryParam("month") String month) throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Integer.parseInt(month));
        SolarSchedule schedule = Db.applyDao(new SolarSystemDao(), dao -> dao.getTodaysSchedule(cal));
        return Response.ok(new Gson().toJson(toScheduleDTO(schedule))).build();
    }

    @GET
    @Path("/parkingPosition")
    @Produces(MediaType.APPLICATION_JSON)
    public Response parkingPositionGet() throws ParseException {
        boolean inParkingPosition = SolarMain.panelStateManager.has(SolarPanelStateManager.Event.PARKING_POSITION);
        return Response.ok(String.valueOf(inParkingPosition)).build();
    }

    @POST
    @Path("/parkingPosition")
    @Produces(MediaType.APPLICATION_JSON)
    public Response parkingPositionSet() throws ParseException {
        if (SolarMain.panelStateManager.has(SolarPanelStateManager.Event.PARKING_POSITION))
            SolarMain.panelStateManager.remove(SolarPanelStateManager.Event.PARKING_POSITION);
        else
            SolarMain.panelStateManager.add(SolarPanelStateManager.Event.PARKING_POSITION);
        return Response.ok().build();
    }

    @GET
    @Path("/currentState")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCurrentPosition() throws ParseException {
        CurrentState src = new CurrentState();
        SolarSchedule forMonth = Db.applyDao(new SolarSystemDao(), dao -> {
            AbsolutePosition lastKnownPosition = dao.getLastKnownPosition();
            Position pos = new Position();
            pos.x = lastKnownPosition.getHorizontal();
            pos.y = lastKnownPosition.getVertical();
            src.pos = pos;
            return dao.getTodaysSchedule(Calendar.getInstance());
        });
        src.dayLight = DayLight.inst().enoughLight();
        src.windy = StrongWind.isWindy();

        List<SolarPanelStep> todayRecords = Lists.newArrayList(Iterables.filter(forMonth.getSteps(), step -> TimeUtil.isAfter(Calendar.getInstance(), step.getHour(), step.getMinute())));

        src.remainingPositions = Lists.transform(todayRecords, this::toSolarDTO);
        if (Main.pinManager.getState(Movement.NORTH.getPinRefCd()) == PinState.HIGH) {
            src.movement.add("NORTH");
        }
        if (Main.pinManager.getState(Movement.SOUTH.getPinRefCd()) == PinState.HIGH) {
            src.movement.add("SOUTH");
        }
        if (Main.pinManager.getState(Movement.WEST.getPinRefCd()) == PinState.HIGH) {
            src.movement.add("WEST");
        }
        if (Main.pinManager.getState(Movement.EAST.getPinRefCd()) == PinState.HIGH) {
            src.movement.add("EAST");
        }
        return Response.ok(new Gson().toJson(src)).build();
    }

    private Schedule toScheduleDTO(SolarSchedule schedule) {
        Schedule s = new Schedule();
        s.horizontalStep = schedule.getHorizontalTickCountForStep();
        s.verticalStep = schedule.getVerticalTickCountForStep();
        SolarPanelStep sunRise = schedule.getSteps().get(0);
        s.sunRiseHour = sunRise.getHour();
        s.sunRiseMinute = sunRise.getMinute();
        s.sunRiseAbsVer = ((AbsolutePosition) sunRise.getPosition()).getVertical();
        s.sunRiseAbsHor = ((AbsolutePosition) sunRise.getPosition()).getHorizontal();
        SolarPanelStep sunSet = Iterables.getLast(schedule.getSteps());
        s.sunSetHour = sunSet.getHour();
        s.sunSetMinute = sunSet.getMinute();
        s.sunSetAbsVer = 0;
        s.sunSetAbsHor = 0;
        for (SolarPanelStep r : Iterables.limit(Iterables.skip(schedule.getSteps(), 1), schedule.getSteps().size() - 2)) {
            SolarDTO dto = toSolarDTO(r);
            dto.vert = dto.vert / s.verticalStep;
            dto.hor = dto.hor / s.horizontalStep;
            s.positions.add(dto);
        }
        return s;
    }

    private SolarDTO toSolarDTO(SolarPanelStep rec) {
        SolarDTO solarDTO = new SolarDTO();
        rec.getPosition().invoke(new PositionProcessor<Void>() {
            @Override
            public Void process(AbsolutePosition absPos) {
                solarDTO.moveType = "Absolútna";
                solarDTO.hor = absPos.getHorizontal();
                solarDTO.vert = absPos.getVertical();
                return null;
            }

            @Override
            public Void process(DeltaPosition deltaPos) {
                solarDTO.moveType = "Relatívna";
                solarDTO.hor = deltaPos.getDeltaHorizontalTicks();
                solarDTO.vert = deltaPos.getDeltaVerticalTicks();
                return null;
            }

            @Override
            public Void process(ParkPosition parkPosition) {
                solarDTO.moveType = "Parkovacia";
                solarDTO.hor = 0;
                solarDTO.vert = 0;
                return null;
            }
        });
        solarDTO.hour = rec.getHour();
        solarDTO.minute = rec.getMinute();
        return solarDTO;
    }

    @Override
    protected Set<NamedPort> getInputs() {
        return ServiceMode.getInputs();
    }

    @Override
    protected boolean getInputState(String portRefCd) {
        return ServiceMode.getInputState(portRefCd);
    }

    @Override
    protected Set<NamedPort> getOutputs() {
        return ServiceMode.getOutputs();
    }

    @Override
    protected boolean getOutputState(String portRefCd) {
        return ServiceMode.getOutputState(portRefCd);
    }

    @Override
    protected Set<String> setOutputState(String portRefCd, boolean state) {
        return ServiceMode.setState(portRefCd, state);
    }


    public static class CurrentState {
        public List<SolarDTO> remainingPositions;
        private boolean windy;
        private boolean dayLight;
        private boolean overHeated;
        private Position pos;
        private List<String> movement = new ArrayList<>();

    }

    public static class Schedule {
        private int horizontalStep;
        private int verticalStep;

        private int sunRiseHour;
        private int sunRiseMinute;
        private int sunRiseAbsHor;
        private int sunRiseAbsVer;

        private int sunSetHour;
        private int sunSetMinute;
        private int sunSetAbsHor;
        private int sunSetAbsVer;

        List<SolarDTO> positions = Lists.newArrayList();
    }

    public static class Position {
        int x, y;
    }

    public static class SolarDTO {
        private int hour, minute;
        private String moveType;
        private Integer hor, vert;
        private boolean ignore;

        /***
         * Gets the hour
         * @return
         */
        public int getHour() {
            return hour;
        }

        /***
         * Gets the minute
         * @return
         */
        public int getMinute() {
            return minute;
        }

        /***
         * Gets the hor
         * @return
         */
        public Integer getHor() {
            return hor;
        }

        /***
         * Gets the vert
         * @return
         */
        public Integer getVert() {
            return vert;
        }

        /***
         * Gets the ignore
         * @return
         */
        public boolean getIgnore() {
            return ignore;
        }
    }
}
