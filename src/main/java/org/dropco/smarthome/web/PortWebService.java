package org.dropco.smarthome.web;

import com.google.common.collect.FluentIterable;
import com.google.gson.Gson;
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

@Path("/ws/port")
public class PortWebService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/inputs")
    public Response inputs() {
        Set<NamedPort> inputs = ServiceMode.getInputs();
        return Response.ok(new Gson().toJson(FluentIterable.from(inputs).transform(port-> {
            PinState state = Main.getInput(port.getRefCd()).getState();
            boolean isHigh = false;
            if (state!=null) isHigh=state.isHigh();
            return new Port(port.getRefCd(),port.getName(),Boolean.toString(isHigh));
        }).toList())).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/outputs")
    public Response outputs() {
        Set<NamedPort> outputs = ServiceMode.getOutputs();
        return Response.ok(new Gson().toJson(FluentIterable.from(outputs).transform(port-> {
            boolean isHigh = Main.getOutput(port.getRefCd()).getState().isHigh();
            return new Port(port.getRefCd(),port.getName(),Boolean.toString(isHigh));
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
        mutualExclussion.forEach(exclussion-> Main.getOutput(exclussion).setState(false));
        //3. Now we can enable it
        Main.getOutput(refCd).setState(new Gson().fromJson(value,Boolean.class));
        return Response.ok(mutualExclussion).build();
    }


    String getServiceModeJson(boolean state) {
        return new Gson().toJson(new org.dropco.smarthome.web.dto.ServiceMode(state));
    }
}