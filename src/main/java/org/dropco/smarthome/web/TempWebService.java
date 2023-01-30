package org.dropco.smarthome.web;

import com.google.common.collect.Comparators;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.querydsl.core.Tuple;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.database.LogDao;
import org.dropco.smarthome.database.querydsl.TemperatureMeasurePlace;
import org.dropco.smarthome.heating.db.HeatingDao;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

@Path("/ws/temp")
public class TempWebService {
    private static final Logger logger = Logger.getLogger(TempWebService.class.getName());

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response temperaturesPerPlace(@QueryParam("from") String fromDate, @QueryParam("to") String toDate) throws ParseException {
        //2020-12-21
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date from = format.parse(fromDate);
        Date to = format.parse(toDate);
        return Db.applyDao(new LogDao(), logDao -> {
            HeatingDao heatingDao = new HeatingDao();
            heatingDao.setConnection(logDao.getConnection());
            List<Tuple> temperatures = logDao.retrieveTemperaturesWithPlaces(from, to);
            Set<String> measurePlaces = Sets.newHashSet(FluentIterable.from(temperatures).transform(t -> t.get(LogDao._tlog.placeRefCd)));

            Map<String, Series> seriesMap = Maps.newHashMap();
            Date dataLastDate = new Date(from.getTime());
            Date currDT = null;
            Set<String> tmpPlaces = Sets.newHashSet(measurePlaces);
            for (Tuple tuple : temperatures) {
                Series currSeries = seriesMap.computeIfAbsent(tuple.get(LogDao._tlog.placeRefCd), key -> {
                    Series series = new Series();
                    series.placeRefCd = key;
                    Tuple measurePlaceByRefCd = heatingDao.getMeasurePlaceByRefCd(key);
                    series.name = measurePlaceByRefCd.get(TemperatureMeasurePlace.TEMP_MEASURE_PLACE.name);
                    series.orderId = measurePlaceByRefCd.get(TemperatureMeasurePlace.TEMP_MEASURE_PLACE.orderId);
                    return series;
                });
                Data data = new Data();
                data.x = tuple.get(LogDao._tlog.timestamp);
                if (dataLastDate.before(data.x)) {
                    dataLastDate = data.x;
                }
                data.y = new BigDecimal(tuple.get(LogDao._tlog.value).toString()).setScale(1, RoundingMode.HALF_UP).doubleValue();
                if (currDT == null) {
                    currDT = data.x;
                    tmpPlaces.remove(currSeries.placeRefCd);
                } else {
                    if (currDT.before(data.x)) {
                        addMissingEntries(logDao, seriesMap, currDT, tmpPlaces, heatingDao);
                        currDT = data.x;
                        tmpPlaces = Sets.newHashSet(measurePlaces);
                        tmpPlaces.remove(currSeries.placeRefCd);

                    } else {
                        tmpPlaces.remove(currSeries.placeRefCd);
                    }
                }
                currSeries.data.add(data);
            }
            addMissingEntries(logDao, seriesMap, currDT, tmpPlaces, heatingDao);
            TempResult tempResult = new TempResult();
            tempResult.lastDate = dataLastDate;
            List<Series> values = Lists.newArrayList(seriesMap.values());
            Collections.sort(values,Comparator.comparing(Series::getOrderId));
            tempResult.series = values;
            return Response.ok(new GsonBuilder().setDateFormat("MM-dd-yyyy HH:mm:ss z").create().toJson(tempResult)).build();
        });
    }

    void addMissingEntries(LogDao logDao, Map<String, Series> seriesMap, Date currDT, Set<String> tmpPlaces, HeatingDao heatingDao) {
        for (String series : tmpPlaces) {
            Series tmpSeries = seriesMap.computeIfAbsent(series, key -> {
                Series tmp = new Series();
                tmp.placeRefCd = key;
                Tuple measurePlaceByRefCd = heatingDao.getMeasurePlaceByRefCd(key);
                tmp.name = measurePlaceByRefCd.get(TemperatureMeasurePlace.TEMP_MEASURE_PLACE.name);
                tmp.orderId = measurePlaceByRefCd.get(TemperatureMeasurePlace.TEMP_MEASURE_PLACE.orderId);
                return tmp;
            });
            Data dt = new Data();
            dt.x = currDT;
            if (tmpSeries.data.isEmpty()) {
                Double prevValue = logDao.readPreviousValue(series, currDT);
                dt.y = new BigDecimal(prevValue == null ? 0.0 : prevValue).setScale(1, RoundingMode.HALF_UP).doubleValue();
            } else
                dt.y = tmpSeries.data.get(tmpSeries.data.size() - 1).y;
            tmpSeries.data.add(dt);
        }
    }

    @GET
    @Path("/delta")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delta(@QueryParam("last") String lastDate) throws ParseException {
        //2020-12-21
        SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss z");
        Date last = format.parse(lastDate);

        Calendar instance = Calendar.getInstance();
        instance.setTime(last);
        instance.add(Calendar.HOUR_OF_DAY, 1);
        last = instance.getTime();
        instance.add(Calendar.DAY_OF_YEAR, 1);
        Date to = instance.getTime();
        Date finalLast = last;

        TempResult tempResult = new TempResult();
        Db.acceptDao(new LogDao(), logDao -> {
            HeatingDao heatingDao = new HeatingDao();
            heatingDao.setConnection(logDao.getConnection());
            List<Tuple> temperatures = logDao.retrieveTemperaturesWithPlaces(finalLast, to);
            Map<String, Series> seriesMap = Maps.newHashMap();
            Date dataLastDate = new Date(finalLast.getTime());
            for (Tuple tuple : temperatures) {
                Series currSeries = seriesMap.computeIfAbsent(tuple.get(LogDao._tlog.placeRefCd), key -> {
                    Series series = new Series();
                    series.placeRefCd = key;
                    Tuple measurePlaceByRefCd = heatingDao.getMeasurePlaceByRefCd(key);
                    series.name = measurePlaceByRefCd.get(TemperatureMeasurePlace.TEMP_MEASURE_PLACE.name);
                    series.orderId = measurePlaceByRefCd.get(TemperatureMeasurePlace.TEMP_MEASURE_PLACE.orderId);
                    return series;
                });
                Data data = new Data();
                data.x = tuple.get(LogDao._tlog.timestamp);
                if (dataLastDate.before(data.x)) {
                    dataLastDate = data.x;
                }
                data.y = new BigDecimal(tuple.get(LogDao._tlog.value).toString()).setScale(1, RoundingMode.HALF_UP).doubleValue();
                currSeries.data.add(data);
            }
            tempResult.lastDate = dataLastDate;
            List<Series> values = Lists.newArrayList(seriesMap.values());
            Collections.sort(values,Comparator.comparing(Series::getOrderId));
            tempResult.series = values;
        });
        return Response.ok(new GsonBuilder().setDateFormat("MM-dd-yyyy HH:mm:ss z").create().toJson(tempResult)).build();
    }

    @GET
    @Path("/measurePlace")
    @Produces(MediaType.APPLICATION_JSON)
    public Response measurePlace() {
        List<Tuple> measurePlaces = Db.applyDao(new HeatingDao(), HeatingDao::listMeasurePlaces);
        List<MeasurePlace> result = Lists.newArrayList();
        for (Tuple tuple : measurePlaces) {
            MeasurePlace data = new MeasurePlace();
            data.name = tuple.get(TemperatureMeasurePlace.TEMP_MEASURE_PLACE.name);
            data.refCd = tuple.get(TemperatureMeasurePlace.TEMP_MEASURE_PLACE.placeRefCd);
            data.deviceId = tuple.get(TemperatureMeasurePlace.TEMP_MEASURE_PLACE.devideId);
            data.orderId = tuple.get(TemperatureMeasurePlace.TEMP_MEASURE_PLACE.orderId);
            result.add(data);
        }
        return Response.ok(new GsonBuilder().setDateFormat("MM-dd-yyyy HH:mm:ss z").create().toJson(result)).build();
    }

    @GET
    @Path("/freeDeviceIds")
    @Produces(MediaType.APPLICATION_JSON)
    public Response freeDeviceIds() {
        List<String> unassignedDeviceIds = Db.applyDao(new LogDao(), LogDao::listUnassignedDeviceIds);
        return Response.ok(new Gson().toJson(unassignedDeviceIds)).build();
    }

    @POST
    @Path("/measurePlace")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveMeasurePlace(String payload) {
        MeasurePlace measurePlace = new Gson().fromJson(payload, MeasurePlace.class);
        String refCd = getRefCd(measurePlace.name);
        Db.acceptDao(new HeatingDao(), heatingDao -> {
            heatingDao.saveMeasurePlace(measurePlace.name, refCd, measurePlace.deviceId, measurePlace.orderId);
            LogDao logDao = new LogDao();
            logDao.setConnection(heatingDao.getConnection());
            logDao.updateLogs(measurePlace.deviceId, refCd);
        });
        return Response.ok().build();
    }

    @PUT
    @Path("/measurePlace/{refCd}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response editMeasurePlace(@PathParam("refCd") String refCd, String payload) {
        MeasurePlace measurePlace = new Gson().fromJson(payload, MeasurePlace.class);
        Db.acceptDao(new HeatingDao(), heatingDao -> {
            heatingDao.updateMeasurePlace(measurePlace.name, measurePlace.deviceId, refCd, measurePlace.orderId);
            LogDao logDao = new LogDao();
            logDao.setConnection(heatingDao.getConnection());
            logDao.updateLogs(measurePlace.deviceId, refCd);
        });
        return Response.ok().build();
    }

    @DELETE
    @Path("/measurePlace/{refCd}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteMeasurePlace(@PathParam("refCd") String refCd) {
        Db.acceptDao(new HeatingDao(), dao -> dao.deleteMeasurePlace(refCd));
        return Response.ok().build();
    }

    private String getRefCd(String refCode) {
        refCode = refCode.toUpperCase().replace(' ', '_').replace('-', '_');
        refCode = refCode.replaceAll("A", "");
        refCode = refCode.replaceAll("E", "");
        refCode = refCode.replaceAll("I", "");
        refCode = refCode.replaceAll("O", "");
        refCode = refCode.replaceAll("U", "");
        refCode = refCode.replaceAll("Y", "");
        refCode = refCode.replaceAll("_{2,}", "");
        if (refCode.endsWith("_")) return refCode.substring(0, refCode.length() - 1);
        return refCode;
    }

    private static class MeasurePlace {
        private String refCd;
        private String name;
        private String deviceId;
        private int orderId;
    }


    private static class TempResult {
        Date lastDate;
        Collection<Series> series;
    }

    private static class Series {
        public int orderId;
        String name;
        String placeRefCd;
        List<Data> data = Lists.newArrayList();

        /***
         * Gets the name
         * @return
         */
        public String getName() {
            return name;
        }

        public int getOrderId() {
            return orderId;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Series{");
            sb.append("name='").append(name).append('\'');
            sb.append(", placeRefCd='").append(placeRefCd).append('\'');
            sb.append(", data=").append(data);
            sb.append('}');
            return sb.toString();
        }
    }

    private static class Data {
        Date x;
        double y;

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Data{");
            sb.append("x=").append(x);
            sb.append(", y=").append(y);
            sb.append('}');
            return sb.toString();
        }
    }


}
