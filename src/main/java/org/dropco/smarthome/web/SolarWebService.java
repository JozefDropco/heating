package org.dropco.smarthome.web;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.heating.solar.*;
import org.dropco.smarthome.heating.solar.dto.*;
import org.dropco.smarthome.heating.solar.move.HorizontalMoveFeedback;
import org.dropco.smarthome.heating.solar.move.VerticalMoveFeedback;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

@Path("/ws/solar")
public class SolarWebService {
    private static final Logger logger = Logger.getLogger(SolarWebService.class.getName());

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTimetable(@QueryParam("month") String month) throws ParseException {
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.DAY_OF_MONTH, 1);
        instance.set(Calendar.MONTH, Integer.parseInt(month) - 1);
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);
        SolarSchedule schedule = Db.applyDao(new SolarSystemDao(), dao -> dao.getForMonth(instance));

        return Response.ok(new Gson().toJson(toScheduleDTO(schedule))).build();
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
            src.dayLight = DayLight.inst().enoughLight();
            src.windy = StrongWind.isWindy();
            src.overHeated = SolarTemperatureWatch.isOverHeated();
            return dao.getForMonth(Calendar.getInstance());
        });
        List<SolarPanelStepRecord> todayRecords = forMonth.getRemainingSteps();

        src.remainingPositions = Lists.transform(todayRecords, this::toSolarDTO);
        if (ServiceMode.getPort(SolarSystemRefCode.NORTH_PIN_REF_CD).isHigh() && VerticalMoveFeedback.getMoving()) {
            src.movement.add("NORTH");
        }
        if (ServiceMode.getPort(SolarSystemRefCode.SOUTH_PIN_REF_CD).isHigh() && VerticalMoveFeedback.getMoving()) {
            src.movement.add("SOUTH");
        }
        if (ServiceMode.getPort(SolarSystemRefCode.WEST_PIN_REF_CD).isHigh() && HorizontalMoveFeedback.getMoving()) {
            src.movement.add("WEST");
        }
        if (ServiceMode.getPort(SolarSystemRefCode.EAST_PIN_REF_CD).isHigh() && HorizontalMoveFeedback.getMoving()) {
            src.movement.add("EAST");
        }
        return Response.ok(new Gson().toJson(src)).build();
    }

    private Schedule toScheduleDTO(SolarSchedule schedule) {
        Schedule s = new Schedule();
        s.horizontalStep = schedule.getHorizontalTickCountForStep();
        s.verticalStep = schedule.getVerticalTickCountForStep();

        s.sunRiseHour = schedule.getSunRise().getHour();
        s.sunRiseMinute = schedule.getSunRise().getMinute();
        s.sunRiseAbsVer = ((AbsolutePosition) schedule.getSunRise().getPosition()).getVertical();
        s.sunRiseAbsHor = ((AbsolutePosition) schedule.getSunRise().getPosition()).getHorizontal();

        s.sunSetHour = schedule.getSunSet().getHour();
        s.sunSetMinute = schedule.getSunSet().getMinute();
        s.sunSetAbsVer = ((AbsolutePosition) schedule.getSunSet().getPosition()).getVertical();
        s.sunSetAbsHor = ((AbsolutePosition) schedule.getSunSet().getPosition()).getHorizontal();
        for (SolarPanelStepRecord r : Iterables.limit(Iterables.skip(schedule.getRemainingSteps(), 1), schedule.getRemainingSteps().size() - 2)) {
            SolarDTO dto = toSolarDTO(r);
            dto.vert = dto.vert / s.verticalStep;
            dto.hor = dto.hor / s.horizontalStep;
            s.positions.add(dto);
        }
        return s;
    }

    private SolarDTO toSolarDTO(SolarPanelStepRecord rec) {
        SolarDTO solarDTO = new SolarDTO();
        rec.getPosition().invoke(new PositionProcessor() {
            @Override
            public void process(AbsolutePosition absPos) {
                solarDTO.moveType = "Absolútna";
                solarDTO.hor = absPos.getHorizontal();
                solarDTO.vert = absPos.getVertical();
            }

            @Override
            public void process(DeltaPosition deltaPos) {
                solarDTO.moveType = "Relatívna";
                solarDTO.hor = deltaPos.getDeltaHorizontalTicks();
                solarDTO.vert = deltaPos.getDeltaVerticalTicks();
            }
        });
        solarDTO.hour = rec.getHour();
        solarDTO.minute = rec.getMinute();
        return solarDTO;
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
