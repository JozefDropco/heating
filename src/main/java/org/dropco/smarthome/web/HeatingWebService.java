package org.dropco.smarthome.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.heating.pump.FireplaceCircularPump;
import org.dropco.smarthome.heating.db.HeatingDao;
import org.dropco.smarthome.heating.dto.SolarHeatingSchedule;
import org.dropco.smarthome.heating.heater.Boiler;
import org.dropco.smarthome.heating.pump.HeaterCircularPump;
import org.dropco.smarthome.heating.heater.Flame;
import org.dropco.smarthome.heating.heater.BoilerBlocker;
import org.dropco.smarthome.heating.pump.SolarCircularPump;
import org.dropco.smarthome.heating.ThreeWayValve;
import org.dropco.smarthome.heating.ServiceMode;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalQueries;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Path("/ws/heating")
public class HeatingWebService extends ServiceModeWebService{
    private static final Logger logger = Logger.getLogger(HeatingWebService.class.getName());
    public static final Gson GSON = new GsonBuilder().registerTypeAdapter(LocalTime.class, new TypeAdapter<LocalTime>() {
        DateTimeFormatter simpleDateFormat = DateTimeFormatter.ISO_DATE_TIME;

        @Override
        public void write(JsonWriter out, LocalTime value) throws IOException {
            out.value(value.atDate(LocalDate.now()).format(simpleDateFormat));
        }

        @Override
        public LocalTime read(JsonReader in) throws IOException {
            return simpleDateFormat.parse(in.nextString(), TemporalQueries.localTime());
        }
    }).create();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response stats(@QueryParam("fromDate") String fromString, @QueryParam("toDate") String toString) throws ParseException {
        FullStats fullStats = new FullStats();
        fullStats.solarCircularPump = SolarCircularPump.getState();
        fullStats.heatingBoilerBlock = BoilerBlocker.getState();
        fullStats.threeWayBypass = ThreeWayValve.getState() == false;
        fullStats.threeWayOpened = ThreeWayValve.getState();
        fullStats.heaterFlame = Flame.getState();
        fullStats.heaterBoiler = Boiler.getState();
        fullStats.fireplaceCircularPump = FireplaceCircularPump.getState();
        fullStats.heaterCircularPump = HeaterCircularPump.getState();
        return Response.ok(new Gson().toJson(fullStats)).build();
    }

    @GET
    @Path("/manualWaterBoilerHeat")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getManualWaterBoilerHeat() throws ParseException {
        return  Response.ok(String.valueOf(BoilerBlocker.getManualOverride())).build();
    }

    @POST
    @Path("/manualWaterBoilerHeat")
    @Produces(MediaType.APPLICATION_JSON)
    public Response manualWaterBoilerHeat() throws ParseException {
        BoilerBlocker.manualOverride();
        return Response.ok().build();
    }
    @Override
    protected boolean getServiceMode() {
        return ServiceMode.isServiceMode();
    }

    @Override
    public void setServiceMode(boolean state) {
        if (state) {
           ServiceMode.startServiceMode();
        } else
            ServiceMode.stopServiceMode();
    }

    @Override
    protected Set<NamedPort> getInputs() {
        return ServiceMode.getInputs();
    }

    @Override
    protected boolean getInputState(String portRefCd) {
        return ServiceMode.getInputState(portRefCd);
    }

    @Override
    protected Set<NamedPort> getOutputs() {
        return ServiceMode.getOutputs();
    }

    @Override
    protected boolean getOutputState(String portRefCd) {
        return ServiceMode.getPort(portRefCd).getState().isHigh();
    }

    @Override
    protected Set<String> setOutputState(String portRefCd, boolean state) {
        ServiceMode.getPort(portRefCd).setState(state);
        return Collections.emptySet();
    }


    private class FullStats {
        boolean solarCircularPump;
        boolean heatingBoilerBlock;
        boolean threeWayBypass;
        boolean threeWayOpened;
        boolean fireplaceCircularPump;
        boolean heaterFlame;
        boolean heaterBoiler;
        boolean heaterCircularPump;
    }

    @GET
    @Path("/query/forDay/{ID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response forDay(@PathParam("ID") int day) throws ParseException {
        List<SolarHeatingSchedule> heatingSchedules = Db.applyDao(new HeatingDao(), dao -> dao.getScheduleForDay(day));
        return Response.ok(GSON.toJson(heatingSchedules)).build();
    }

    @PUT
    @Path("/cmd/update/{ID}")
    public Response create(@PathParam("ID")int id, String payload){
        SolarHeatingSchedule solarHeatingSchedule = GSON.fromJson(payload, SolarHeatingSchedule.class);
        Db.acceptDao(new HeatingDao(),dao->dao.updateHeatingSchedule(solarHeatingSchedule));
        return Response.ok().build();
    }
    @POST
    @Path("/cmd/create")
    public Response create(String payload){
        SolarHeatingSchedule solarHeatingSchedule = GSON.fromJson(payload, SolarHeatingSchedule.class);
        Db.acceptDao(new HeatingDao(),dao->dao.saveHeatingSchedule(solarHeatingSchedule));
        return Response.ok().build();
    }
}
