package org.dropco.smarthome.web;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import org.dropco.smarthome.Main;
import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.web.dto.Port;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/ws/configuration")
public class ConfigurationWebService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response inputs() {
        return Response.ok(new Gson().toJson(Main.INPUTS)).build();
    }

}
