package org.dropco.smarthome.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.dropco.smarthome.heating.db.HeatingDao;
import org.dropco.smarthome.heating.solar.BoilerBlocker;
import org.dropco.smarthome.heating.solar.SolarCircularPump;
import org.dropco.smarthome.heating.solar.ThreeWayValve;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalQueries;
import java.util.logging.Logger;

@Path("/ws/heating")
public class HeatingWebService {
    private static final Logger logger = Logger.getLogger(HeatingWebService.class.getName());

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response stats(@QueryParam("fromDate") String fromString, @QueryParam("toDate") String toString) throws ParseException {
        FullStats fullStats = new FullStats();
        fullStats.solarCircularPump = SolarCircularPump.getState();
        fullStats.heatingBoilerBlock = BoilerBlocker.getState();
        fullStats.threeWayBypass = ThreeWayValve.getState()==false;
        fullStats.threeWayOpened = ThreeWayValve.getState();
        return Response.ok(new Gson().toJson(fullStats)).build();
    }

    private class FullStats {
        boolean solarCircularPump;
        boolean heatingBoilerBlock;
        boolean threeWayBypass;
        boolean threeWayOpened;
    }
    @GET
    @Path("/query/forDay/{ID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response forDay(@PathParam("ID") int day) throws ParseException {

        return Response.ok(new GsonBuilder().registerTypeAdapter(LocalTime.class, new TypeAdapter<LocalTime>() {
            DateTimeFormatter simpleDateFormat = DateTimeFormatter.ISO_LOCAL_TIME;
            @Override
            public void write(JsonWriter out, LocalTime value) throws IOException {
                out.value(value.format(simpleDateFormat));
            }

            @Override
            public LocalTime read(JsonReader in) throws IOException {
                return simpleDateFormat.parse(in.nextString(), TemporalQueries.localTime());
            }
        }).create().toJson(new HeatingDao().getScheduleForDay(day))).build();
    }

}
