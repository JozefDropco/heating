package org.dropco.smarthome.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.dropco.smarthome.database.LogDao;
import org.dropco.smarthome.database.querydsl.TemperatureMeasurePlace;
import org.dropco.smarthome.heating.Boiler;
import org.dropco.smarthome.heating.CircularPump;
import org.dropco.smarthome.heating.ThreeWayValve;
import org.dropco.smarthome.heating.db.HeatingDao;
import org.dropco.smarthome.stats.StatsDao;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Path("/ws/heating")
public class HeatingWebService {
    private static final Logger logger = Logger.getLogger(HeatingWebService.class.getName());

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response stats(@QueryParam("fromDate") String fromString, @QueryParam("toDate") String toString) throws ParseException {
        FullStats fullStats = new FullStats();
        fullStats.solarCircularPump = CircularPump.getState();
        fullStats.heatingBoiler = Boiler.getState();
        fullStats.threeWayBypass = ThreeWayValve.getState()==false;
        fullStats.threeWayOpened = ThreeWayValve.getState();
        return Response.ok(new Gson().toJson(fullStats)).build();
    }

    private class FullStats {
        boolean solarCircularPump;
        boolean heatingBoiler;
        boolean threeWayBypass;
        boolean threeWayOpened;
    }

}
