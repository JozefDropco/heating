package org.dropco.smarthome.web;

import com.google.common.collect.FluentIterable;
import com.google.gson.Gson;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import org.dropco.smarthome.Main;
import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.web.dto.Port;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/ws/port")
public class PortWebService {
    private static final Logger logger = Logger.getLogger(PortWebService.class.getName());

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/inputs")
    public Response inputs() {
        Set<NamedPort> inputs = ServiceMode.getInputs();
        return Response.ok(new Gson().toJson(FluentIterable.from(inputs).transform(port -> {
            PinState state = Main.getInput(port.getRefCd()).getState();
            boolean isHigh = false;
            if (state != null) isHigh = state.isHigh();
            return new Port(port.getRefCd(), port.getName(), Boolean.toString(isHigh));
        }).toList())).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/outputs")
    public Response outputs() {
        Set<NamedPort> outputs = ServiceMode.getOutputs();
        return Response.ok(new Gson().toJson(FluentIterable.from(outputs).transform(port -> {
            boolean isHigh = ServiceMode.getPort(port.getRefCd()).getState().isHigh();
            return new Port(port.getRefCd(), port.getName(), Boolean.toString(isHigh));
        }).toList())).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/output/{refCd}")
    public Response setValue(@PathParam("refCd") String refCd, String value) {
        //1.check if its output
        boolean match = FluentIterable.from(ServiceMode.getOutputs()).anyMatch(port -> port.getRefCd().equals(refCd));
        if (!match) return Response.status(Response.Status.BAD_REQUEST).build();
        //2. shutdown exclussions
        Collection<String> mutualExclussion = ServiceMode.getExclusions().get(refCd);
        mutualExclussion.forEach(exclussion -> {
            ServiceMode.getPort(exclussion).setState(false);
            logger.log(Level.INFO, "Port " + exclussion + " vypnuty");
        });
        //3. Now we can enable it
        GpioPinDigitalOutput port = ServiceMode.getPort(refCd);
        port.setState(new Gson().fromJson(value, Boolean.class));
        logger.log(Level.INFO, "Port " + refCd + (port.isHigh()?" zapnuty": " vypnuty"));
        return Response.ok(new Gson().toJson(mutualExclussion)).build();
    }


}
