package org.dropco.smarthome.web;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.solar.SolarPanelStepRecord;
import org.dropco.smarthome.solar.SolarSystemDao;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Path("/ws/solar")
public class SolarWebService {
    private static final Logger logger = Logger.getLogger(SolarWebService.class.getName());

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTimetable(@QueryParam("date") String date, @QueryParam("month") String month) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List<SolarPanelStepRecord> records;
        if (date != null) {
            Date datum = format.parse(date);
            Calendar cal = Calendar.getInstance();
            cal.setTime(datum);
            records = new SolarSystemDao(new SettingsDao()).getTodayRecords(cal);
        } else {
            records = new SolarSystemDao(new SettingsDao()).getMonthRecords(Integer.parseInt(month));
        }

        return Response.ok(new Gson().toJson(Lists.transform(records, this::toSolarDTO))).build();
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
            new SolarSystemDao(new SettingsDao()).updateForDate(dto,cal);
        } else {
            new SolarSystemDao(new SettingsDao()).updateForMonth(dto,Integer.parseInt(month));
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
            new SolarSystemDao(new SettingsDao()).deleteForDate(dto,cal);
        } else {
            new SolarSystemDao(new SettingsDao()).deleteForMonth(dto,Integer.parseInt(month));
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
