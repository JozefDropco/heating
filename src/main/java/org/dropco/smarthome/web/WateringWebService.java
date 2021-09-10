package org.dropco.smarthome.web;

import com.google.gson.Gson;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.microservice.RainSensor;
import org.dropco.smarthome.microservice.WaterPumpFeedback;
import org.dropco.smarthome.watering.WateringThreadManager;
import org.dropco.smarthome.watering.db.WateringDao;
import org.dropco.smarthome.watering.db.WateringRecord;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/ws/watering")
public class WateringWebService {

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
        return Response.ok(new Gson().toJson(new WateringDao().getRecord(id))).build();
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

    private static class State{
        private boolean warmEnough;
        private boolean isRainy;
        private boolean pumpRunning;
    }

}
