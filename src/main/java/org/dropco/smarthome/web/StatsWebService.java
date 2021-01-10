package org.dropco.smarthome.web;

import com.google.gson.GsonBuilder;
import org.dropco.smarthome.database.LogDao;
import org.dropco.smarthome.database.querydsl.TemperatureMeasurePlace;
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

@Path("/ws/stats")
public class StatsWebService {
    private static final Logger logger = Logger.getLogger(StatsWebService.class.getName());

    @GET
    @Path("/temp")
    @Produces(MediaType.APPLICATION_JSON)
    public Response temperaturesPerPlace(@QueryParam("fromDate") String fromString, @QueryParam("toDate") String toString) throws ParseException {
        //2020-12-21
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date from = format.parse(fromString);
        Calendar instance = Calendar.getInstance();
        Date to = format.parse(toString);
        instance.setTime(to);
        instance.add(Calendar.DAY_OF_YEAR, 1);
        instance.add(Calendar.SECOND, -1);
        to = instance.getTime();
        List<LogDao.AggregateTemp> temperatures = new LogDao().retrieveAggregatedTemperatures(from, to);
        for (LogDao.AggregateTemp temp : temperatures) {
            temp.measurePlace = new HeatingDao().getMeasurePlaceByRefCd(temp.measurePlace).get(TemperatureMeasurePlace.TEMP_MEASURE_PLACE.name);
        }
        List<StatsDao.AggregatedStats> aggregatedStats = new StatsDao().listAggregatedStats(from, to);
        FullStats fullStats = new FullStats();
        fullStats.ports = aggregatedStats;
        fullStats.temps = temperatures;
        return Response.ok(new GsonBuilder().setDateFormat("MM-dd-yyyy HH:mm:ss z").create().toJson(fullStats)).build();
    }

    private class FullStats {
        List<LogDao.AggregateTemp> temps;
        List<StatsDao.AggregatedStats> ports;
    }

}
