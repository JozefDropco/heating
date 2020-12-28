package org.dropco.smarthome.web;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.querydsl.core.Tuple;
import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.database.LogDao;
import org.dropco.smarthome.database.querydsl.TemperatureMeasurePlace;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.heating.db.HeatingDao;
import org.dropco.smarthome.web.dto.Port;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/ws/temp")
public class TempWebService {
    private static final Logger logger = Logger.getLogger(TempWebService.class.getName());

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response temperaturesPerPlace(@QueryParam("from") String fromDate, @QueryParam("to") String toDate) throws ParseException {
        //2020-12-21
        SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd");
        Date from =format.parse(fromDate);
        Date to = format.parse(toDate);
        List<Tuple> temperatures = new LogDao().retrieveTemperaturesWithPlaces(from,to);
        Map<String , Series> seriesMap = Maps.newHashMap();
        for (Tuple tuple: temperatures){
            Series currSeries = seriesMap.computeIfAbsent(tuple.get(LogDao._log.placeRefCd), key -> {
                Series series = new Series();
                series.placeRefCd = key;
                series.deviceId = tuple.get(LogDao._log.devideId);
                series.name = new HeatingDao().getMeasurePlaceByRefCd(key).get(TemperatureMeasurePlace.TEMP_MEASURE_PLACE.name);
                return series;
            });
            Data data = new Data();
            data.x=tuple.get(LogDao._log.timestamp);
            data.y=tuple.get(LogDao._log.value);
            currSeries.data.add(data);
        }
        return Response.ok(new GsonBuilder().setDateFormat("MM-dd-yyyy HH:mm:ss z").create().toJson(seriesMap.values())).build();
    }

    @GET
    @Path("/measurePlace")
    @Produces(MediaType.APPLICATION_JSON)
    public Response measurePlace() {
        List<Tuple> measurePlaces = new HeatingDao().listMeasurePlaces();
        List<MeasurePlace> result = Lists.newArrayList();
        for (Tuple tuple: measurePlaces){
            MeasurePlace data = new MeasurePlace();
            data.name=tuple.get(TemperatureMeasurePlace.TEMP_MEASURE_PLACE.name);
            data.refCd=tuple.get(TemperatureMeasurePlace.TEMP_MEASURE_PLACE.placeRefCd);
            data.deviceId=tuple.get(TemperatureMeasurePlace.TEMP_MEASURE_PLACE.devideId);
            result.add(data);
        }
        return Response.ok(new GsonBuilder().setDateFormat("MM-dd-yyyy HH:mm:ss z").create().toJson(result)).build();
    }


    private static class MeasurePlace{
        private String refCd;
        private String name;
        private String deviceId;
    }

    private static class Series{
        String name;
        String placeRefCd;
        String deviceId;
        List<Data> data= Lists.newArrayList();
    }

    private static class Data{
        Date x;
        double y;
    }


}
