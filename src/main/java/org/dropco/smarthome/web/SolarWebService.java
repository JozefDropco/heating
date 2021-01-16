package org.dropco.smarthome.web;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.dropco.smarthome.Main;
import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.heating.db.HeatingDao;
import org.dropco.smarthome.solar.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Path("/ws/solar")
public class SolarWebService {
    private static final Logger logger = Logger.getLogger(SolarWebService.class.getName());
    public static SettingsDao SETTINGS_DAO;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTimetable(@QueryParam("date") String date, @QueryParam("month") String month) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List<SolarPanelStepRecord> records;
        if (date != null) {
            Date datum = format.parse(date);
            Calendar cal = Calendar.getInstance();
            cal.setTime(datum);
            records = new SolarSystemDao(SETTINGS_DAO).getTodayRecords(cal);
        } else {
            records = new SolarSystemDao(SETTINGS_DAO).getMonthRecords(Integer.parseInt(month));
        }

        return Response.ok(new Gson().toJson(Lists.transform(records, this::toSolarDTO))).build();
    }



    @GET
    @Path("/currentState")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCurrentPosition() throws ParseException {
        SolarPanelPosition lastKnownPosition = new SolarSystemDao(SETTINGS_DAO).getLastKnownPosition();
        Position pos = new Position();
        pos.x=lastKnownPosition.getHorizontalPositionInSeconds();
        pos.y=lastKnownPosition.getVerticalPositionInSeconds();
        State src = new State();
        src.pos=pos;
        src.dayLight=DayLight.inst().enoughLight();
        src.windy=StrongWind.isWindy();
        src.overHeated  = SolarTemperatureWatch.isOverHeated();
        List<SolarPanelStepRecord> todayRecords = new SolarSystemDao(SETTINGS_DAO).getTodayRecords(Calendar.getInstance());

        src.remainingPositions = Lists.transform(todayRecords,this::toSolarDTO);
        if (ServiceMode.getPort(SolarSystemRefCode.NORTH_PIN_REF_CD).isHigh()){
            src.movement.add("NORTH");
        }
        if (ServiceMode.getPort(SolarSystemRefCode.SOUTH_PIN_REF_CD).isHigh()){
            src.movement.add("SOUTH");
        }
        if (ServiceMode.getPort(SolarSystemRefCode.WEST_PIN_REF_CD).isHigh()){
            src.movement.add("WEST");
        }
        if (ServiceMode.getPort(SolarSystemRefCode.EAST_PIN_REF_CD).isHigh()){
            src.movement.add("EAST");
        }
        return Response.ok(new Gson().toJson(src)).build();
    }

    @PUT
    @Path("/cmd/update")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateSolarTimeTable(@QueryParam("date") String date, @QueryParam("month") String month, String entry) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        SolarDTO dto = new Gson().fromJson(entry,SolarDTO.class);
        if (date != null) {
            Date datum = format.parse(date);
            Calendar cal = Calendar.getInstance();
            cal.setTime(datum);
            new SolarSystemDao(SETTINGS_DAO).updateForDate(dto,cal);
        } else {
            new SolarSystemDao(SETTINGS_DAO).updateForMonth(dto,Integer.parseInt(month));
        }
        return Response.ok().build();
    }

    @POST
    @Path("/cmd/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteSolarTimeTable(@QueryParam("date") String date, @QueryParam("month") String month, String entry) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        SolarDTO dto = new Gson().fromJson(entry,SolarDTO.class);
        if (date != null) {
            Date datum = format.parse(date);
            Calendar cal = Calendar.getInstance();
            cal.setTime(datum);
            new SolarSystemDao(SETTINGS_DAO).deleteForDate(dto,cal);
        } else {
            new SolarSystemDao(SETTINGS_DAO).deleteForMonth(dto,Integer.parseInt(month));
        }
        return Response.ok().build();
    }

    private SolarDTO toSolarDTO(SolarPanelStepRecord rec) {
        SolarDTO solarDTO = new SolarDTO();
        solarDTO.hor = rec.getPanelPosition().getHorizontalPositionInSeconds();
        solarDTO.vert = rec.getPanelPosition().getVerticalPositionInSeconds();
        solarDTO.hour = rec.getHour();
        solarDTO.ignore = rec.getIgnoreDaylight();
        solarDTO.minute = rec.getMinute();
        return solarDTO;
    }

    public static class State{
        public List<SolarDTO> remainingPositions;
        private boolean windy;
        private boolean dayLight;
        private boolean overHeated;
        private Position pos;
        private List<String> movement = new ArrayList<>();

    }
    public static class Position{
        int x,y;
    }
    public static class SolarDTO {
        private int hour, minute;
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
