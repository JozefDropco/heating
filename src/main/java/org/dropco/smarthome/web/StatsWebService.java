package org.dropco.smarthome.web;

import com.google.gson.GsonBuilder;
import com.querydsl.core.Tuple;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.database.LogDao;
import org.dropco.smarthome.database.querydsl.TemperatureMeasurePlace;
import org.dropco.smarthome.heating.db.HeatingDao;
import org.dropco.smarthome.heating.db.TempSensor;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Path("/ws/stats")
public class StatsWebService {
    private static final Logger logger = Logger.getLogger(StatsWebService.class.getName());

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response stats(@QueryParam("fromDate") String fromString, @QueryParam("toDate") String toString) throws ParseException {
        //2020-12-21
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date from = format.parse(fromString);
        Calendar instance = Calendar.getInstance();
        Date to = format.parse(toString);
        instance.setTime(to);
        instance.add(Calendar.DAY_OF_YEAR, 1);
        instance.add(Calendar.SECOND, -1);
        to = instance.getTime();
        Date date = new Date();
        if (date.before(to)) {
            to = date;
        }
        Date finalTo = to;
        FullStats fullStats = new FullStats();
        Db.acceptDao(new LogDao(), logDao -> {
            HeatingDao heatingDao = new HeatingDao();
            heatingDao.setConnection(logDao.getConnection());
            List<LogDao.AggregateTemp> temperatures = logDao.retrieveAggregatedTemperatures(from, finalTo);
            for (LogDao.AggregateTemp temp : temperatures) {
                TempSensor sensor = heatingDao.getDeviceByPlaceRefCd(temp.measurePlace);
                temp.last = logDao.readLastValue(temp.measurePlace);
                temp.measurePlace = sensor.getName();
                temp.orderId = sensor.getOrderId();

            }
            Collections.sort(temperatures, Comparator.comparing(LogDao.AggregateTemp::getOrderId));
            fullStats.temps = temperatures;

            StatsDao statsDao = new StatsDao();
            statsDao.setConnection(logDao.getConnection());
            List<StatsDao.AggregatedStats> aggregatedStats = statsDao.listAggregatedStats(from, finalTo);
            fullStats.ports = aggregatedStats;
        });
        return Response.ok(new GsonBuilder().setDateFormat("MM-dd-yyyy HH:mm:ss z").create().toJson(fullStats)).build();
    }

    private class FullStats {
        List<LogDao.AggregateTemp> temps;
        List<StatsDao.AggregatedStats> ports;
    }

}
