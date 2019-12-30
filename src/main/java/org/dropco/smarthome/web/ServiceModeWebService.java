package org.dropco.smarthome.web;

import com.google.gson.Gson;
import org.dropco.smarthome.ServiceMode;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/ws/serviceMode")
public class ServiceModeWebService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServiceModeState() {
        return Response.ok(getServiceModeJson(ServiceMode.isServiceMode())).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response setServiceMode(@QueryParam("state") boolean state) {
        if (state) {
            ServiceMode.startServiceMode();
        } else
            ServiceMode.stopServiceMode();
        return Response.ok(new org.dropco.smarthome.web.dto.ServiceMode(ServiceMode.isServiceMode())).build();
    }

    String getServiceModeJson(boolean state) {
        return new Gson().toJson(new org.dropco.smarthome.web.dto.ServiceMode(state));
    }
}