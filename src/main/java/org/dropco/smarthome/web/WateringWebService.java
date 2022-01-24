package org.dropco.smarthome.web;

import com.google.gson.Gson;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.microservice.RainSensor;
import org.dropco.smarthome.microservice.WaterPumpFeedback;
import org.dropco.smarthome.watering.ServiceMode;
import org.dropco.smarthome.watering.WateringThreadManager;
import org.dropco.smarthome.watering.db.WateringDao;
import org.dropco.smarthome.watering.db.WateringRecord;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Path("/ws/watering")
public class WateringWebService  extends ServiceModeWebService {


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() {
        List<WateringRecord> wateringRecords = Db.applyDao(new WateringDao(), WateringDao::getAllRecords);
        return Response.ok(new Gson().toJson(wateringRecords)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response get(@PathParam("id") Long id) {
        WateringRecord record = Db.applyDao(new WateringDao(), dao -> dao.getRecord(id));
        return Response.ok(new Gson().toJson(record)).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, String wateringRecord) {
        WateringRecord record = new Gson().fromJson(wateringRecord, WateringRecord.class);
        if (id.equals(record.getId())) {
            Db.acceptDao(new WateringDao(),dao->dao.updateRecord(record));
            return Response.ok().build();
        } else
            return Response.notModified().build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(String wateringRecord) {
        WateringRecord record = new Gson().fromJson(wateringRecord, WateringRecord.class);
        long id = Db.applyDao(new WateringDao(),dao->dao.insertRecord(record));
        record.setId(id);
        return Response.ok(new Gson().toJson(record)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("state")
    public Response getState(){
        State state = new State();
        state.isRainy= RainSensor.isRaining();
        state.pumpRunning = WaterPumpFeedback.getRunning();
        state.warmEnough = WateringThreadManager.isWarmEnough();
        return Response.ok(new Gson().toJson(state)).build();
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

    private static class State{
        private boolean warmEnough;
        private boolean isRainy;
        private boolean pumpRunning;
    }

}
