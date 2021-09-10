package org.dropco.smarthome.web;

import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import org.dropco.smarthome.database.Db;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/ws/logs")
public class LogsWebservice {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response logs(@QueryParam("date") String dateString, @QueryParam("logLevel") String logLevel, @QueryParam("cnt") int maxCount) throws ParseException {
        //2020-12-21
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date from = format.parse(dateString);
        Calendar instance = Calendar.getInstance();
        instance.setTime(from);
        instance.add(Calendar.DAY_OF_YEAR, 1);
        instance.add(Calendar.SECOND, -1);
        Date to = instance.getTime();
        Level current = Level.parse(logLevel);
        List<String> levels = Lists.newArrayList();
        for (Level lvl : Arrays.asList(Level.ALL,Level.CONFIG,Level.FINE,Level.FINER,Level.FINEST,Level.INFO,Level.SEVERE,Level.WARNING,Level.OFF)){
            if (current.intValue()<=lvl.intValue()) levels.add(lvl.getName());
        }
        List<LogDao.AppMsg> logs = Db.applyDao(new LogDao(), dao-> dao.getLogs(from, to, maxCount, levels));
        return Response.ok(new GsonBuilder().setDateFormat("MM-dd-yyyy HH:mm:ss z").create().toJson(logs)).build();
    }

}
