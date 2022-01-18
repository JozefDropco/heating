package org.dropco.smarthome.web;

import com.google.gson.Gson;
import org.dropco.smarthome.Main;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/ws/configuration")
public class ConfigurationWebService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response inputs() {
        return Response.ok(new Gson().toJson(Main.INPUTS)).build();
    }

}
