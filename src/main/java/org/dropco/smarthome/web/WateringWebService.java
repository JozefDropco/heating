package org.dropco.smarthome.web;

import com.google.gson.Gson;
import org.dropco.smarthome.watering.db.WateringDao;
import org.dropco.smarthome.watering.db.WateringRecord;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/ws/watering")
public class WateringWebService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() {
        return Response.ok(new Gson().toJson(new WateringDao().getAllRecords())).build();
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
            new WateringDao().updateRecord(record);
            return Response.ok().build();
        } else
            return Response.notModified().build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(String wateringRecord) {
        WateringRecord record = new Gson().fromJson(wateringRecord, WateringRecord.class);
        long id = new WateringDao().insertRecord(record);
        record.setId(id);
        return Response.ok(new Gson().toJson(record)).build();
    }
}