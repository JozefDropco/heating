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
import org.dropco.smarthome.heating.solar.dto.AbsolutePosition;
import org.dropco.smarthome.heating.solar.dto.DeltaPosition;
import org.dropco.smarthome.heating.solar.dto.ParkPosition;
import org.dropco.smarthome.heating.solar.dto.PositionProcessor;
import org.dropco.smarthome.heating.solar.dto.SolarPanelStep;
import org.dropco.smarthome.heating.solar.dto.SolarSchedule;
import org.dropco.smarthome.heating.solar.move.Movement;
import org.dropco.smarthome.heating.solar.move.SolarPanelStateManager;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
        SolarSchedule schedule = Db.applyDao(new SolarSystemDao(), dao -> dao.getTodaysSchedule(Integer.parseInt(month)));
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
    @Path("/cmd/update")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@QueryParam("month") int month, String json) throws ParseException {
        Schedule schedule = new Gson().fromJson(json, Schedule.class);
        SolarSchedule solarSchedule = toSolarSchedule(schedule,month);
         Db.acceptDao(new SolarSystemDao(), dao -> dao.update(solarSchedule));
        return Response.ok().build();
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
            return dao.getTodaysSchedule(Calendar.getInstance().get(Calendar.MONTH)+1);
        });
        src.dayLight = DayLight.inst().enoughLight();
        src.windy = StrongWind.isWindy();

        List<SolarPanelStep> todayRecords = Lists.newArrayList(Iterables.filter(forMonth.getSteps(), step -> TimeUtil.isAfter(Calendar.getInstance(), step.getHour(), step.getMinute())));

        src.remainingPositions = Lists.transform(Lists.transform(todayRecords, this::toSolarDTO),step->{
            if (step.moveType.equals("Relatívna")) {
                step.hor*=forMonth.getHorizontalTickCountForStep();
                step.vert*=forMonth.getVerticalTickCountForStep();
            }
            return step;
        });
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
        SolarPanelStep sunSet = Iterables.getLast(schedule.getSteps());
        s.sunSetHour = sunSet.getHour();
        s.sunSetMinute = sunSet.getMinute();
        for (SolarPanelStep r : Iterables.limit(Iterables.skip(schedule.getSteps(), 1), schedule.getSteps().size() - 2)) {
            SolarDTO dto = toSolarDTO(r);
            s.positions.add(dto);
        }
        return s;
    }


    private SolarSchedule toSolarSchedule(Schedule schedule, int month) {
        SolarSchedule solarSchedule =new SolarSchedule();
        solarSchedule.setMonth(month);
        solarSchedule.setHorizontalTickCountForStep(schedule.horizontalStep);
        solarSchedule.setVerticalTickCountForStep(schedule.verticalStep);
        List<SolarPanelStep> steps = Lists.newArrayList(Lists.transform(schedule.positions, pos -> {
            SolarPanelStep step = new SolarPanelStep();
            step.setHour(pos.hour);
            step.setMinute(pos.minute);
            step.setPosition(toPosition(pos.moveType, pos.hor, pos.vert));
            return step;
        }));
        SolarPanelStep sunRise = new SolarPanelStep();
        sunRise.setHour(schedule.sunRiseHour);
        sunRise.setMinute(schedule.sunRiseMinute);
        steps.add(0, sunRise);
        SolarPanelStep sunset = new SolarPanelStep();
        sunset.setHour(schedule.sunSetHour);
        sunset.setMinute(schedule.sunSetMinute);
        steps.add(sunset);
        solarSchedule.setSteps(steps);
        return solarSchedule;
    }

    private org.dropco.smarthome.heating.solar.dto.Position toPosition(String moveType, Integer hor, Integer vert) {
        switch (moveType){
            case "Absolútna": return new AbsolutePosition(hor,vert);
            case "Relatívna": return new DeltaPosition(hor,vert);
            case "Parkovacia": return ParkPosition.INSTANCE;
            default: throw new UnsupportedOperationException("Not supported case");
        }
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
                solarDTO.hor = deltaPos.getHorizontalCount();
                solarDTO.vert = deltaPos.getVerticalCount();
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
    protected boolean getServiceMode() {
        return ServiceMode.isServiceMode();
    }

    @Override
    public void setServiceMode(boolean state) {
        if (state) {
            ServiceMode.startServiceMode();
        } else
            ServiceMode.stopServiceMode();
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

        private int sunSetHour;
        private int sunSetMinute;

        List<SolarDTO> positions = Lists.newArrayList();
    }

    public static class Position {
        int x, y;
    }

    public static class SolarDTO {
        private int hour, minute;
        private String moveType;
        private Integer hor, vert;

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

    }
}
